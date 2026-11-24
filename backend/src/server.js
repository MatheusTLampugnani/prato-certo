const express = require('express');
require('dotenv').config();
const jwt = require('jsonwebtoken');
const bcrypt = require('bcryptjs');
const supabase = require('./config/db');
const TacoService = require('./services/tacoService');
const OpenFoodFactsService = require('./services/openFoodFactsService');
const precosConab = require('./data/precos_conab.json');
const verificarAutenticacao = require('./middleware/auth');

const app = express();
app.use(express.json());

const JWT_SECRET = process.env.JWT_SECRET;

// ── Teste Banco ──────────────────────────────────────────────────────────
app.get('/api/teste-banco', async (req, res) => {
    try {
        const { data, error } = await supabase.from('alimentos').select('*').limit(1);
        if (error) throw error;
        res.json({ sucesso: true, dados: data });
    } catch (error) {
        res.status(500).json({ sucesso: false, erro: error.message });
    }
});

// ── Popular Banco ────────────────────────────────────────────────────────
function obterPrecoConab(descricao) {
    const d = (descricao || '').toLowerCase();
    const encontrado = precosConab.find(p => d.includes(p.termo));
    return encontrado ? encontrado.preco_medio : 12.50;
}

app.post('/api/popular-banco', verificarAutenticacao, async (req, res) => {
    try {
        const alimentosTaco = TacoService.alimentos;
        for (const alimento of alimentosTaco) {
            const { data: novoAlimento, error: erroAlimento } = await supabase
                .from('alimentos')
                .insert([{
                    nome_descricao: alimento.description,
                    calorias: alimento.attributes.energy?.kcal || 0,
                    proteinas: alimento.attributes.protein?.qty || 0,
                    carboidratos: alimento.attributes.carbohydrate?.qty || 0,
                    gorduras: alimento.attributes.lipid?.qty || 0,
                    porcao_base: `${alimento.base_qty}${alimento.base_unit}`
                }])
                .select('id')
                .single();

            if (erroAlimento) throw erroAlimento;

            const precoRealConab = obterPrecoConab(alimento.description);
            const { error: erroPreco } = await supabase
                .from('precos')
                .insert([{ alimento_id: novoAlimento.id, preco_medio: precoRealConab, fonte_dado: 'CONAB' }]);

            if (erroPreco) throw erroPreco;
        }
        res.json({ sucesso: true, mensagem: 'Banco populado com dados da TACO e preços reais da CONAB!' });
    } catch (error) {
        res.status(500).json({ sucesso: false, erro: error.message });
    }
});

// ── Alimentos ────────────────────────────────────────────────────────────
app.get('/api/alimentos', async (req, res) => {
    const { pesquisa } = req.query;
    if (!pesquisa) return res.status(400).json({ sucesso: false, erro: 'Envie um termo de pesquisa.' });

    try {
        const { data, error } = await supabase
            .from('alimentos')
            .select(`id, nome_descricao, calorias, proteinas, carboidratos, gorduras, precos ( preco_medio, fonte_dado )`)
            .ilike('nome_descricao', `%${pesquisa}%`)
            .limit(10);

        if (error) throw error;
        res.json({ sucesso: true, total: data.length, dados: data });
    } catch (error) {
        res.status(500).json({ sucesso: false, erro: error.message });
    }
});

// ── Open Food Facts / Código de Barras ──────────────────────────────────
app.get('/api/produtos/barcode/:codigo', async (req, res) => {
    const { codigo } = req.params;
    if (!codigo) return res.status(400).json({ sucesso: false, erro: 'Informe o código de barras.' });

    try {
        const produto = await OpenFoodFactsService.buscarPorCodigoBarras(codigo);
        if (!produto) {
            return res.json({ sucesso: false, erro: 'Produto não encontrado na base do Open Food Facts.' });
        }
        res.json({ sucesso: true, dado: produto });
    } catch (error) {
        console.error('Erro na consulta de código de barras:', error);
        res.status(500).json({ sucesso: false, erro: error.message });
    }
});

// ── Autenticação ─────────────────────────────────────────────────────────
app.post('/api/auth/registrar', async (req, res) => {
    const { nome, email, senha } = req.body;
    if (!nome || !email || !senha) return res.status(400).json({ sucesso: false, erro: 'Preencha todos os campos.' });

    try {
        const { data: usuarioExistente } = await supabase
            .from('usuarios')
            .select('id')
            .eq('email', email)
            .maybeSingle();

        if (usuarioExistente) return res.status(400).json({ sucesso: false, erro: 'Este e-mail já está cadastrado.' });

        const salt = await bcrypt.genSalt(10);
        const senha_hash = await bcrypt.hash(senha, salt);

        const { data, error } = await supabase
            .from('usuarios')
            .insert([{ nome, email, senha_hash }])
            .select('id, nome, email')
            .single();

        if (error) throw error;
        res.status(201).json({ sucesso: true, mensagem: 'Usuário cadastrado com sucesso!', usuario: data });
    } catch (error) {
        console.error("Erro no registo:", error);
        res.status(500).json({ sucesso: false, erro: error.message });
    }
});

app.post('/api/auth/login', async (req, res) => {
    const { email, senha } = req.body;
    if (!email || !senha) return res.status(400).json({ sucesso: false, erro: 'Informe e-mail e senha.' });

    try {
        const { data: usuario, error } = await supabase
            .from('usuarios')
            .select('*')
            .eq('email', email)
            .maybeSingle();

        if (error || !usuario) return res.status(401).json({ sucesso: false, erro: 'E-mail ou senha inválidos.' });

        const senhaValida = await bcrypt.compare(senha, usuario.senha_hash);
        if (!senhaValida) return res.status(401).json({ sucesso: false, erro: 'E-mail ou senha inválidos.' });

        const token = jwt.sign({ id: usuario.id, email: usuario.email }, JWT_SECRET, { expiresIn: '7d' });

        res.json({
            sucesso: true,
            mensagem: 'Login realizado com sucesso!',
            token,
            usuario: { id: usuario.id, nome: usuario.nome, email: usuario.email }
        });
    } catch (error) {
        res.status(500).json({ sucesso: false, erro: error.message });
    }
});

// ── Orçamento ────────────────────────────────────────────────────────────
app.post('/api/orcamento', verificarAutenticacao, async (req, res) => {
    const usuario_id = req.usuarioId;
    const { valor, periodo } = req.body;
    const numValor = Number(valor);

    if (valor === undefined || valor === null || isNaN(numValor) || numValor <= 0) {
        return res.status(400).json({ sucesso: false, erro: 'Informe um valor de orçamento válido e maior que zero.' });
    }

    const periodoNormalizado = (periodo || 'mensal').toString().trim().toLowerCase();
    const periodosValidos = ['semanal', 'quinzenal', 'mensal'];
    if (!periodosValidos.includes(periodoNormalizado)) {
        return res.status(400).json({ sucesso: false, erro: 'Período inválido. Escolha entre semanal, quinzenal ou mensal.' });
    }

    try {
        // 1. Verifica se o usuário já tem um orçamento salvo
        const { data: existente } = await supabase
            .from('orcamentos')
            .select('id')
            .eq('usuario_id', usuario_id)
            .maybeSingle();

        let orcamentoFinal;
        if (existente) {
            // 2. Se já existir, atualiza o valor
            const { data, error: erroUpdate } = await supabase
                .from('orcamentos')
                .update({ valor: numValor, periodo: periodoNormalizado })
                .eq('usuario_id', usuario_id)
                .select().single();

            if (erroUpdate) throw erroUpdate;
            orcamentoFinal = data;
        } else {
            // 3. Se não existir, cria um novo registro
            const { data, error: erroInsert } = await supabase
                .from('orcamentos')
                .insert([{ usuario_id, valor: numValor, periodo: periodoNormalizado }])
                .select().single();

            if (erroInsert) throw erroInsert;
            orcamentoFinal = data;
        }

        res.json({ sucesso: true, mensagem: "Orçamento salvo com sucesso!", orcamento: orcamentoFinal });
    } catch (error) {
        console.error("Erro ao salvar orçamento:", error);
        res.status(500).json({ sucesso: false, erro: error.message });
    }
});

// ── Cardápio / Listas ────────────────────────────────────────────────────
app.get('/api/cardapio', verificarAutenticacao, async (req, res) => {
    const usuario_id = req.usuarioId;

    try {
        const { data, error } = await supabase
            .from('listas')
            .select(`
                id,
                titulo_lista,
                tipo,
                criado_em,
                lista_itens (
                    id,
                    quantidade_gramas,
                    preco_calculado,
                    comprado,
                    alimentos (
                        id,
                        nome_descricao,
                        calorias,
                        proteinas,
                        carboidratos,
                        gorduras
                    )
                )
            `)
            .eq('usuario_id', usuario_id)
            .order('criado_em', { ascending: false });

        if (error) throw error;

        const formatado = (data || []).map(lista => {
            const itens = lista.lista_itens || [];
            const valorTotal = itens.reduce((acc, it) => acc + Number(it.preco_calculado || 0), 0);
            return {
                id: lista.id,
                titulo_lista: lista.titulo_lista,
                tipo: lista.tipo,
                criado_em: lista.criado_em,
                lista_itens: itens.map(item => ({
                    id: item.id,
                    quantidade_gramas: item.quantidade_gramas,
                    preco_calculado: item.preco_calculado,
                    comprado: item.comprado || false,
                    alimentos: item.alimentos
                })),
                totalItens: itens.length,
                valorTotal: parseFloat(valorTotal.toFixed(2))
            };
        });

        res.json({ sucesso: true, dados: formatado });
    } catch (error) {
        res.status(500).json({ sucesso: false, erro: error.message });
    }
});

// ── Listas ───────────────────────────────────────────────────────────────
app.post('/api/listas', verificarAutenticacao, async (req, res) => {
    const { titulo_lista, tipo } = req.body;
    const usuario_id = req.usuarioId;

    if (!titulo_lista) {
        return res.status(400).json({ sucesso: false, erro: 'Informe o título da lista.' });
    }

    try {
        const { data, error } = await supabase
            .from('listas')
            .insert([{
                usuario_id,
                titulo_lista,
                tipo: tipo || 'mercado'
            }])
            .select()
            .single();

        if (error) throw error;
        res.status(201).json({ sucesso: true, mensagem: 'Lista criada com sucesso!', lista: data });
    } catch (error) {
        res.status(500).json({ sucesso: false, erro: error.message });
    }
});

app.post('/api/listas/:id/itens', verificarAutenticacao, async (req, res) => {
    const lista_id = req.params.id;
    const usuario_id = req.usuarioId;
    const { alimento_id, quantidade_gramas } = req.body;
    const numGramas = Number(quantidade_gramas);

    if (!alimento_id || isNaN(numGramas) || numGramas <= 0) {
        return res.status(400).json({ sucesso: false, erro: 'Informe o alimento_id e uma quantidade_gramas maior que zero.' });
    }

    try {
        const { data: lista, error: erroLista } = await supabase
            .from('listas')
            .select('id')
            .eq('id', lista_id)
            .eq('usuario_id', usuario_id)
            .maybeSingle();

        if (erroLista || !lista) {
            return res.status(404).json({ sucesso: false, erro: 'Lista não encontrada ou acesso não autorizado.' });
        }

        const { data: precoData, error: precoError } = await supabase
            .from('precos')
            .select('preco_medio')
            .eq('alimento_id', alimento_id)
            .maybeSingle();

        let precoMedio = (precoData && !precoError) ? precoData.preco_medio : 0;
        // precoMedio é em R$/kg (1000g), logo o cálculo por grama divide por 1000
        const preco_calculado = (precoMedio / 1000) * numGramas;

        const { data, error } = await supabase
            .from('lista_itens')
            .insert([{
                lista_id,
                alimento_id,
                quantidade_gramas: numGramas,
                preco_calculado: parseFloat(preco_calculado.toFixed(2))
            }])
            .select()
            .single();

        if (error) throw error;
        res.status(201).json({ sucesso: true, mensagem: 'Item adicionado à lista!', item: data });
    } catch (error) {
        res.status(500).json({ sucesso: false, erro: error.message });
    }
});

// ── Excluir Lista ────────────────────────────────────────────────────────
app.delete('/api/listas/:id', verificarAutenticacao, async (req, res) => {
    const lista_id = req.params.id;
    const usuario_id = req.usuarioId;

    try {
        const { data: lista, error: erroBusca } = await supabase
            .from('listas')
            .select('id')
            .eq('id', lista_id)
            .eq('usuario_id', usuario_id)
            .maybeSingle();

        if (erroBusca || !lista) {
            return res.status(404).json({ sucesso: false, erro: 'Lista não encontrada ou acesso não autorizado.' });
        }

        await supabase.from('lista_itens').delete().eq('lista_id', lista_id);
        const { error } = await supabase.from('listas').delete().eq('id', lista_id).eq('usuario_id', usuario_id);
        if (error) throw error;
        res.json({ sucesso: true, mensagem: 'Lista excluída com sucesso!' });
    } catch (error) {
        res.status(500).json({ sucesso: false, erro: error.message });
    }
});

// ── Excluir Item da Lista ─────────────────────────────────────────────────
app.delete('/api/listas/itens/:id', verificarAutenticacao, async (req, res) => {
    const item_id = req.params.id;
    const usuario_id = req.usuarioId;

    try {
        const { data: item, error: erroItem } = await supabase
            .from('lista_itens')
            .select('id, lista_id')
            .eq('id', item_id)
            .maybeSingle();

        if (erroItem || !item) {
            return res.status(404).json({ sucesso: false, erro: 'Item não encontrado.' });
        }

        const { data: lista, error: erroLista } = await supabase
            .from('listas')
            .select('id')
            .eq('id', item.lista_id)
            .eq('usuario_id', usuario_id)
            .maybeSingle();

        if (erroLista || !lista) {
            return res.status(403).json({ sucesso: false, erro: 'Acesso não autorizado a esta lista.' });
        }

        const { error } = await supabase.from('lista_itens').delete().eq('id', item_id);
        if (error) throw error;
        res.json({ sucesso: true, mensagem: 'Item removido da lista!' });
    } catch (error) {
        res.status(500).json({ sucesso: false, erro: error.message });
    }
});

// ── Alternar Status de Compra do Item (Checklist) ─────────────────────────
app.patch('/api/listas/itens/:id/toggle', verificarAutenticacao, async (req, res) => {
    const item_id = req.params.id;
    const usuario_id = req.usuarioId;
    const { comprado } = req.body;

    try {
        const { data: item, error: erroItem } = await supabase
            .from('lista_itens')
            .select('id, lista_id')
            .eq('id', item_id)
            .maybeSingle();

        if (erroItem || !item) {
            return res.status(404).json({ sucesso: false, erro: 'Item não encontrado.' });
        }

        const { data: lista, error: erroLista } = await supabase
            .from('listas')
            .select('id')
            .eq('id', item.lista_id)
            .eq('usuario_id', usuario_id)
            .maybeSingle();

        if (erroLista || !lista) {
            return res.status(403).json({ sucesso: false, erro: 'Acesso não autorizado a esta lista.' });
        }

        const { data, error } = await supabase
            .from('lista_itens')
            .update({ comprado: Boolean(comprado) })
            .eq('id', item_id)
            .select()
            .single();

        if (error) throw error;
        res.json({ sucesso: true, mensagem: 'Status do item atualizado!', item: data });
    } catch (error) {
        res.status(500).json({ sucesso: false, erro: error.message });
    }
});

// ── Algoritmo de Geração Inteligente de Cardápio ─────────────────────────
function categorizarAlimentoCardapio(nome) {
    const n = (nome || '').toLowerCase();
    
    // Junk food e ultraprocessados / gorduras ou farinhas puras
    if (n.includes('refrigerante') || n.includes('cerveja') || n.includes('biscoito') || 
        n.includes('chocolate') || n.includes('chips') || n.includes('gelatina') || 
        n.includes('geleia') || n.includes('ketchup') || n.includes('maionese') || 
        n.includes('mostarda') || n.includes('açúcar') || n.includes('pipoca') ||
        n.includes('caldo de carne') || n.includes('chia') || n.includes('linhaça') ||
        n.includes('óleo') || n.includes('azeite') || n.includes('manteiga') || n.includes('farinha') ||
        n.includes('sagu')) {
        return 'evitar';
    }

    // Proteínas principais magras/balanceadas
    if (n.includes('frango') || n.includes('carne bovina') || n.includes('carne suína') || 
        n.includes('peixe') || n.includes('tilápia') || n.includes('merluza') || 
        n.includes('atum') || n.includes('sardinha')) {
        return 'proteina_principal';
    }

    // Ovos
    if (n.includes('ovo')) {
        return 'ovos';
    }

    // Leguminosas
    if (n.includes('feijão') || n.includes('lentilha') || n.includes('grão-de-bico')) {
        return 'leguminosa';
    }

    // Carboidratos complexos da culinária diária
    if (n.includes('arroz') || (n.includes('batata') && n.includes('cozida')) || 
        (n.includes('mandioca') && n.includes('cozida')) || n.includes('macarrão')) {
        return 'carbo_base';
    }

    // Café da manhã / Lanche saudável
    if (n.includes('aveia') || n.includes('pão') || n.includes('leite') || n.includes('iogurte')) {
        return 'cafe_lanche';
    }

    // Frutas
    if (n.includes('banana') || n.includes('maçã') || n.includes('laranja') || 
        n.includes('mamão') || n.includes('manga') || n.includes('melancia') || 
        n.includes('abacaxi') || n.includes('morango') || n.includes('uva') || n.includes('pera')) {
        return 'fruta';
    }

    // Vegetais / Legumes / Saladas
    if (n.includes('alface') || n.includes('tomate') || n.includes('cenoura') || 
        n.includes('abobrinha') || n.includes('chuchu') || n.includes('couve') || 
        n.includes('espinafre') || n.includes('repolho') || n.includes('pepino') || n.includes('abóbora')) {
        return 'vegetal';
    }

    return 'outros';
}

function sortearItemTop(lista, n = 3) {
    if (!lista || lista.length === 0) return null;
    const topo = lista.slice(0, Math.min(n, lista.length));
    return topo[Math.floor(Math.random() * topo.length)];
}

function montarCardapioEquilibrado(alimentosDisponiveis, dailyBudget, targetCal, targetProt) {
    const foods = alimentosDisponiveis
        .filter(a => a.precos && a.precos.length > 0 && a.precos[0].preco_medio > 0)
        .map(a => ({
            id: a.id,
            nome: a.nome_descricao,
            calorias: a.calorias || 0,
            proteinas: a.proteinas || 0,
            carboidratos: a.carboidratos || 0,
            gorduras: a.gorduras || 0,
            preco_kg: a.precos[0].preco_medio,
            categoria: categorizarAlimentoCardapio(a.nome_descricao)
        }))
        .filter(a => a.categoria !== 'evitar');

    const carboList = foods.filter(f => f.categoria === 'carbo_base')
        .sort((a, b) => (a.preco_kg / (a.calorias || 1)) - (b.preco_kg / (b.calorias || 1)));
    const carbo = sortearItemTop(carboList, 3);

    const legList = foods.filter(f => f.categoria === 'leguminosa')
        .sort((a, b) => (a.preco_kg / (a.calorias || 1)) - (b.preco_kg / (b.calorias || 1)));
    const leg = sortearItemTop(legList, 3);

    const prot1List = foods.filter(f => f.categoria === 'proteina_principal')
        .sort((a, b) => (a.preco_kg / (a.proteinas || 1)) - (b.preco_kg / (b.proteinas || 1)));
    const prot1 = sortearItemTop(prot1List, 3);

    const ovosList = foods.filter(f => f.categoria === 'ovos');
    const ovos = sortearItemTop(ovosList, 2);

    const cafeList = foods.filter(f => f.categoria === 'cafe_lanche')
        .sort((a, b) => (a.preco_kg / (a.calorias || 1)) - (b.preco_kg / (b.calorias || 1)));
    const cafe = sortearItemTop(cafeList, 3);

    const frutaList = foods.filter(f => f.categoria === 'fruta')
        .sort((a, b) => (a.preco_kg / (a.calorias || 1)) - (b.preco_kg / (b.calorias || 1)));
    const fruta = sortearItemTop(frutaList, 3);

    const vegList = foods.filter(f => f.categoria === 'vegetal')
        .sort((a, b) => a.preco_kg - b.preco_kg);
    const veg = sortearItemTop(vegList, 3);

    let cesta = [
        { food: carbo, gramas: 300 },
        { food: leg, gramas: 180 },
        { food: prot1, gramas: 200 },
        { food: ovos, gramas: 100 },
        { food: cafe, gramas: 80 },
        { food: fruta, gramas: 150 },
        { food: veg, gramas: 120 }
    ].filter(p => p.food);

    if (cesta.length === 0 && foods.length > 0) {
        cesta = foods.slice(0, Math.min(5, foods.length)).map(f => ({ food: f, gramas: 100 }));
    }

    const calcTotais = (itens) => {
        let custo = 0, cal = 0, prot = 0;
        itens.forEach(i => {
            custo += (i.food.preco_kg / 1000) * i.gramas;
            cal += (i.food.calorias / 100) * i.gramas;
            prot += (i.food.proteinas / 100) * i.gramas;
        });
        return { custo, cal, prot };
    };

    let totais = calcTotais(cesta);

    // Ajuste proporcional se o custo base estourar o dailyBudget
    if (totais.custo > dailyBudget && dailyBudget > 0) {
        const fator = (dailyBudget * 0.95) / totais.custo;
        cesta.forEach(i => {
            i.gramas = Math.max(50, Math.round((i.gramas * fator) / 10) * 10);
        });
        totais = calcTotais(cesta);
    }

    // Se sobrou margem no orçamento diário e calorias < meta, aumentar itens nutritivos
    if (totais.cal < targetCal && totais.custo < dailyBudget * 0.95) {
        const itensParaAumentar = cesta.filter(i => 
            i.food.categoria === 'carbo_base' || 
            i.food.categoria === 'cafe_lanche' || 
            i.food.categoria === 'fruta' ||
            i.food.categoria === 'proteina_principal'
        );

        let tentativas = 0;
        while (totais.cal < targetCal - 50 && totais.custo < dailyBudget * 0.96 && tentativas < 25) {
            tentativas++;
            let aumentou = false;
            for (const item of itensParaAumentar) {
                const custo20g = (item.food.preco_kg / 1000) * 20;
                const cal20g = (item.food.calorias / 100) * 20;
                if (totais.custo + custo20g <= dailyBudget && totais.cal + cal20g <= targetCal + 100) {
                    item.gramas += 20;
                    totais = calcTotais(cesta);
                    aumentou = true;
                }
            }
            if (!aumentou) break;
        }
    }

    return cesta.map(i => {
        const precoCalculado = parseFloat(((i.food.preco_kg / 1000) * i.gramas).toFixed(2));
        return {
            alimento_id: i.food.id,
            quantidade_gramas: i.gramas,
            preco_calculado: precoCalculado
        };
    });
}

// ── Gerador Automático de Cardápio ───────────────────────────────────────
app.post('/api/cardapio/gerar', verificarAutenticacao, async (req, res) => {
    const usuario_id = req.usuarioId;

    try {
        const { data: meta } = await supabase.from('metas_usuario').select('*').eq('usuario_id', usuario_id).maybeSingle();
        if (!meta) return res.status(404).json({ sucesso: false, erro: 'Meta não configurada. Configure suas metas antes de gerar o cardápio.' });

        const { data: orcamento } = await supabase.from('orcamentos').select('*').eq('usuario_id', usuario_id).maybeSingle();
        if (!orcamento) return res.status(404).json({ sucesso: false, erro: 'Orçamento não configurado. Defina um orçamento primeiro.' });

        const periodoNorm = (orcamento.periodo || 'mensal').toString().trim().toLowerCase();
        let diasPeriodo = 30;
        if (periodoNorm === 'semanal') diasPeriodo = 7;
        else if (periodoNorm === 'quinzenal') diasPeriodo = 15;
        else if (periodoNorm === 'mensal') diasPeriodo = 30;

        let dailyBudget = orcamento.valor / diasPeriodo;
        if (dailyBudget <= 0) dailyBudget = 15.0;

        const { data: alimentos } = await supabase
            .from('alimentos')
            .select('id, nome_descricao, calorias, proteinas, carboidratos, gorduras, precos(preco_medio)')
            .not('precos', 'is', null);

        const availableFoods = (alimentos || []).filter(a => a.precos && a.precos.length > 0 && a.precos[0].preco_medio > 0);

        if (availableFoods.length === 0) {
            return res.status(404).json({ sucesso: false, erro: 'Nenhum alimento com preço encontrado no banco.' });
        }

        const selectedItems = montarCardapioEquilibrado(
            availableFoods,
            dailyBudget,
            meta.calorias_dia || 2000,
            meta.proteinas_dia || 80
        );

        if (selectedItems.length === 0) {
            return res.status(400).json({ sucesso: false, erro: 'Orçamento muito baixo para gerar cardápio completo.' });
        }

        // Verifica se já existe uma lista do tipo cardápio para o usuário
        const { data: listaExistente } = await supabase
            .from('listas')
            .select('id')
            .eq('usuario_id', usuario_id)
            .eq('tipo', 'cardapio')
            .order('criado_em', { ascending: false })
            .limit(1)
            .maybeSingle();

        let listaAtivaId;
        if (listaExistente) {
            listaAtivaId = listaExistente.id;
            // Limpa itens antigos da lista para atualizar com a nova sugestão inteligente
            await supabase.from('lista_itens').delete().eq('lista_id', listaAtivaId);
        } else {
            const { data: novaLista, error: errLista } = await supabase
                .from('listas')
                .insert([{ usuario_id, titulo_lista: 'Cardápio Inteligente', tipo: 'cardapio' }])
                .select()
                .single();

            if (errLista) throw errLista;
            listaAtivaId = novaLista.id;
        }

        const itensToInsert = selectedItems.map(item => ({ lista_id: listaAtivaId, ...item }));
        const { error: errItens } = await supabase.from('lista_itens').insert(itensToInsert);
        if (errItens) throw errItens;

        const totalCusto = selectedItems.reduce((acc, i) => acc + i.preco_calculado, 0);

        res.status(201).json({
            sucesso: true,
            mensagem: 'Cardápio inteligente gerado com sucesso!',
            lista: {
                id: listaAtivaId,
                titulo_lista: 'Cardápio Inteligente',
                tipo: 'cardapio'
            },
            resumo: {
                lista_id: listaAtivaId,
                total_itens: selectedItems.length,
                custo_diario: parseFloat(totalCusto.toFixed(2)),
                custo_semanal: parseFloat((totalCusto * 7).toFixed(2)),
                custo_quinzenal: parseFloat((totalCusto * 15).toFixed(2)),
                custo_mensal: parseFloat((totalCusto * 30).toFixed(2)),
                orcamento_diario: parseFloat(dailyBudget.toFixed(2)),
                orcamento_original: orcamento.valor,
                periodo_orcamento: periodoNorm,
                dias_periodo: diasPeriodo
            }
        });
    } catch (error) {
        res.status(500).json({ sucesso: false, erro: error.message });
    }
});

// ── Salvar Sugestão Ativa como Lista de Mercado "Sugestão" ───────────────
app.post('/api/cardapio/salvar-sugestao', verificarAutenticacao, async (req, res) => {
    const usuario_id = req.usuarioId;

    try {
        // 1. Busca o cardápio ativo do usuário
        const { data: cardapio, error: errCardapio } = await supabase
            .from('listas')
            .select(`
                id,
                lista_itens (
                    alimento_id,
                    quantidade_gramas,
                    preco_calculado
                )
            `)
            .eq('usuario_id', usuario_id)
            .eq('tipo', 'cardapio')
            .order('criado_em', { ascending: false })
            .limit(1)
            .maybeSingle();

        if (errCardapio) throw errCardapio;
        if (!cardapio || !cardapio.lista_itens || cardapio.lista_itens.length === 0) {
            return res.status(400).json({ sucesso: false, erro: 'Nenhuma sugestão ativa para salvar.' });
        }

        // 2. Verifica se a lista "Sugestão" do tipo 'mercado' já existe para esse usuário
        const { data: listaExistente, error: errBusca } = await supabase
            .from('listas')
            .select('id')
            .eq('usuario_id', usuario_id)
            .eq('titulo_lista', 'Sugestão')
            .eq('tipo', 'mercado')
            .maybeSingle();

        if (errBusca) throw errBusca;

        let targetListaId;
        if (listaExistente) {
            targetListaId = listaExistente.id;
            // Limpa itens antigos da lista "Sugestão" existente para substituir
            const { error: errDel } = await supabase
                .from('lista_itens')
                .delete()
                .eq('lista_id', targetListaId);
            if (errDel) throw errDel;
        } else {
            // Cria a lista "Sugestão"
            const { data: novaLista, error: errNova } = await supabase
                .from('listas')
                .insert([{
                    usuario_id,
                    titulo_lista: 'Sugestão',
                    tipo: 'mercado'
                }])
                .select()
                .single();

            if (errNova) throw errNova;
            targetListaId = novaLista.id;
        }

        // 3. Insere todos os alimentos da sugestão na lista "Sugestão"
        const itensToInsert = cardapio.lista_itens.map(item => ({
            lista_id: targetListaId,
            alimento_id: item.alimento_id,
            quantidade_gramas: item.quantidade_gramas,
            preco_calculado: item.preco_calculado
        }));

        const { error: errInsert } = await supabase.from('lista_itens').insert(itensToInsert);
        if (errInsert) throw errInsert;

        res.status(200).json({
            sucesso: true,
            mensagem: "Lista 'Sugestão' salva com sucesso!",
            lista_id: targetListaId
        });
    } catch (error) {
        res.status(500).json({ sucesso: false, erro: error.message });
    }
});

app.get('/api/listas/:id', verificarAutenticacao, async (req, res) => {
    const lista_id = req.params.id;
    const usuario_id = req.usuarioId;

    try {
        const { data: lista, error } = await supabase
            .from('listas')
            .select(`
                id, 
                titulo_lista, 
                tipo, 
                criado_em,
                lista_itens (
                    id, 
                    quantidade_gramas, 
                    preco_calculado,
                    alimentos (
                        nome_descricao
                    )
                )
            `)
            .eq('id', lista_id)
            .eq('usuario_id', usuario_id)
            .maybeSingle();

        if (error || !lista) {
            return res.status(404).json({ sucesso: false, erro: 'Lista não encontrada.' });
        }

        const valorTotal = lista.lista_itens.reduce((soma, item) => soma + Number(item.preco_calculado), 0);

        res.json({
            sucesso: true,
            resumo: {
                id_lista: lista.id,
                titulo: lista.titulo_lista,
                tipo: lista.tipo,
                total_itens: lista.lista_itens.length,
                valor_total_calculado: parseFloat(valorTotal.toFixed(2)),
                itens: lista.lista_itens
            }
        });
    } catch (error) {
        res.status(500).json({ sucesso: false, erro: error.message });
    }
});

// ── Obter Orçamento Atual ────────────────────────────────────────────────
app.get('/api/orcamento', verificarAutenticacao, async (req, res) => {
    const usuario_id = req.usuarioId;

    try {
        const { data, error } = await supabase
            .from('orcamentos')
            .select('*')
            .eq('usuario_id', usuario_id)
            .maybeSingle();

        if (error) throw error;
        res.json({ sucesso: true, orcamento: data });
    } catch (error) {
        res.status(500).json({ sucesso: false, erro: error.message });
    }
});

async function buscarSugestoesComOrcamento(usuario_id, objetivo) {
    const { data: orcamento } = await supabase.from('orcamentos').select('*').eq('usuario_id', usuario_id).maybeSingle();
    
    let dailyBudget = 0;
    if (orcamento) {
        const pNorm = (orcamento.periodo || 'mensal').toString().trim().toLowerCase();
        const dias = pNorm === 'semanal' ? 7 : (pNorm === 'quinzenal' ? 15 : 30);
        dailyBudget = orcamento.valor / dias;
    }

    const { data: alimentos } = await supabase
        .from('alimentos')
        .select('id, nome_descricao, calorias, proteinas, carboidratos, gorduras, precos!inner(preco_medio)')
        .gt('proteinas', 5)
        .lte('calorias', objetivo === 'emagrecer' ? 200 : 400);

    let availableFoods = alimentos || [];

    if (dailyBudget > 0) {
        availableFoods.sort((a, b) => {
            const valA = a.calorias / a.precos[0].preco_medio;
            const valB = b.calorias / b.precos[0].preco_medio;
            return valB - valA;
        });
    }

    return availableFoods.slice(0, 6);
}

app.get('/api/metas', verificarAutenticacao, async (req, res) => {
    const usuario_id = req.usuarioId;
    try {
        const { data, error } = await supabase
            .from('metas_usuario')
            .select('*')
            .eq('usuario_id', usuario_id)
            .maybeSingle();

        if (error || !data) {
            return res.status(404).json({ sucesso: false, erro: 'Nenhuma meta encontrada.' });
        }

        const sugestoes = await buscarSugestoesComOrcamento(usuario_id, data.objetivo);

        res.json({ sucesso: true, meta: data, sugestoes });
    } catch (error) {
        res.status(500).json({ sucesso: false, erro: error.message });
    }
});

// ── POST /api/metas ───
app.post('/api/metas', verificarAutenticacao, async (req, res) => {
    const { peso_atual, altura_cm, objetivo, peso_meta: reqPesoMeta } = req.body;
    const usuario_id = req.usuarioId;

    const numPeso = Number(peso_atual);
    const numAltura = Number(altura_cm);
    const objetivosValidos = ['emagrecer', 'engordar', 'manter'];
    const objNormalizado = (objetivo || '').toString().trim().toLowerCase();

    if (isNaN(numPeso) || numPeso <= 20 || numPeso > 300) {
        return res.status(400).json({ sucesso: false, erro: 'Informe um peso atual válido (entre 20 e 300 kg).' });
    }
    if (isNaN(numAltura) || numAltura <= 50 || numAltura > 260) {
        return res.status(400).json({ sucesso: false, erro: 'Informe uma altura válida (entre 50 e 260 cm).' });
    }
    if (!objetivosValidos.includes(objNormalizado)) {
        return res.status(400).json({ sucesso: false, erro: 'Objetivo inválido. Escolha entre emagrecer, manter ou engordar.' });
    }
    if (reqPesoMeta !== undefined && reqPesoMeta !== null) {
        const numPesoMeta = Number(reqPesoMeta);
        if (isNaN(numPesoMeta) || numPesoMeta <= 20 || numPesoMeta > 300) {
            return res.status(400).json({ sucesso: false, erro: 'Informe um peso meta válido (entre 20 e 300 kg).' });
        }
    }

    const idade = 25;
    const tmb = 88.36 + (13.4 * numPeso) + (4.8 * numAltura) - (5.7 * idade);
    const fatorAtividade = 1.375; // levemente ativo

    let calorias, proteinas, carboidratos, gorduras, peso_meta;

    if (objNormalizado === 'emagrecer') {
        calorias = Math.round(tmb * fatorAtividade * 0.80); // déficit 20%
        proteinas = Math.round(numPeso * 2.2);
        gorduras = Math.round(numPeso * 0.8);
        carboidratos = Math.round((calorias - proteinas * 4 - gorduras * 9) / 4);
        peso_meta = reqPesoMeta ? Number(reqPesoMeta) : parseFloat((numPeso * 0.90).toFixed(1));
    } else if (objNormalizado === 'engordar') {
        calorias = Math.round(tmb * fatorAtividade * 1.15); // superávit 15%
        proteinas = Math.round(numPeso * 2.0);
        gorduras = Math.round(numPeso * 1.0);
        carboidratos = Math.round((calorias - proteinas * 4 - gorduras * 9) / 4);
        peso_meta = reqPesoMeta ? Number(reqPesoMeta) : parseFloat((numPeso * 1.05).toFixed(1));
    } else { // manter
        calorias = Math.round(tmb * fatorAtividade);
        proteinas = Math.round(numPeso * 1.8);
        gorduras = Math.round(numPeso * 0.9);
        carboidratos = Math.round((calorias - proteinas * 4 - gorduras * 9) / 4);
        peso_meta = reqPesoMeta ? Number(reqPesoMeta) : numPeso;
    }

    if (carboidratos < 0) carboidratos = 30;

    try {
        const { data: existente } = await supabase
            .from('metas_usuario')
            .select('id')
            .eq('usuario_id', usuario_id)
            .maybeSingle();

        const payload = {
            usuario_id,
            peso_atual: numPeso,
            altura_cm: numAltura,
            peso_meta,
            objetivo: objNormalizado,
            calorias_dia: calorias,
            proteinas_dia: proteinas,
            carboidratos_dia: carboidratos,
            gorduras_dia: gorduras
        };

        let resultado;
        if (existente) {
            const { data, error } = await supabase
                .from('metas_usuario')
                .update(payload)
                .eq('usuario_id', usuario_id)
                .select().single();
            if (error) throw error;
            resultado = data;
        } else {
            const { data, error } = await supabase
                .from('metas_usuario')
                .insert([payload])
                .select().single();
            if (error) throw error;
            resultado = data;
        }

        const sugestoes = await buscarSugestoesComOrcamento(usuario_id, objetivo);

        res.json({
            sucesso: true,
            mensagem: 'Meta salva!',
            meta: resultado,
            sugestoes: sugestoes || []
        });
    } catch (error) {
        res.status(500).json({ sucesso: false, erro: error.message });
    }
});

const PORT = process.env.PORT || 3000;
app.listen(PORT, () => {
    console.log(`Backend rodando porta ${PORT}`);
});