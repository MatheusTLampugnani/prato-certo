const express = require('express');
const bcrypt = require('bcryptjs');
require('dotenv').config();
const jwt = require('jsonwebtoken');
const supabase = require('./config/db');
const TacoService = require('./services/tacoService');
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
app.post('/api/popular-banco', async (req, res) => {
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

            const precoAleatorio = (Math.random() * (25 - 5) + 5).toFixed(2);
            const { error: erroPreco } = await supabase
                .from('precos')
                .insert([{ alimento_id: novoAlimento.id, preco_medio: parseFloat(precoAleatorio), fonte_dado: 'CONAB' }]);

            if (erroPreco) throw erroPreco;
        }
        res.json({ sucesso: true, mensagem: 'Banco populado com dados da TACO e preços!' });
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
            .select(`id, nome_descricao, calorias, proteinas, precos ( preco_medio, fonte_dado )`)
            .ilike('nome_descricao', `%${pesquisa}%`)
            .limit(10);

        if (error) throw error;
        res.json({ sucesso: true, total: data.length, dados: data });
    } catch (error) {
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
                .update({ valor, periodo })
                .eq('usuario_id', usuario_id)
                .select().single();

            if (erroUpdate) throw erroUpdate;
            orcamentoFinal = data;
        } else {
            // 3. Se não existir, cria um novo registro
            const { data, error: erroInsert } = await supabase
                .from('orcamentos')
                .insert([{ usuario_id, valor, periodo }])
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

// ── Cardápio / Listas (Adicionada a rota em falta) ────────────────────────
app.get('/api/cardapio', verificarAutenticacao, async (req, res) => {
    const usuario_id = req.usuarioId;

    try {
        const { data: listas, error } = await supabase
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
                    alimentos ( nome_descricao, calorias, proteinas )
                )
            `)
            .eq('usuario_id', usuario_id);

        if (error) throw error;

        const formatado = (listas || []).map(lista => {
            const itens = lista.lista_itens || [];
            const valorTotal = itens.reduce((acc, it) => acc + (it.preco_calculado || 0), 0);
            return {
                ...lista,
                valorTotal,
                totalItens: itens.length
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
    const { alimento_id, quantidade_gramas } = req.body;

    if (!alimento_id || !quantidade_gramas) {
        return res.status(400).json({ sucesso: false, erro: 'Informe o alimento_id e a quantidade_gramas.' });
    }

    try {
        const { data: precoData, error: precoError } = await supabase
            .from('precos')
            .select('preco_medio')
            .eq('alimento_id', alimento_id)
            .maybeSingle();

        let precoMedio = (precoData && !precoError) ? precoData.preco_medio : 0;
        const preco_calculado = (precoMedio / 100) * quantidade_gramas;

        const { data, error } = await supabase
            .from('lista_itens')
            .insert([{
                lista_id,
                alimento_id,
                quantidade_gramas,
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

// ── Gerador Automático de Cardápio ───────────────────────────────────────
app.post('/api/cardapio/gerar', verificarAutenticacao, async (req, res) => {
    const usuario_id = req.usuarioId;

    try {
        const { data: meta } = await supabase.from('metas_usuario').select('*').eq('usuario_id', usuario_id).single();
        if (!meta) return res.status(404).json({ sucesso: false, erro: 'Meta não configurada.' });

        const { data: orcamento } = await supabase.from('orcamentos').select('*').eq('usuario_id', usuario_id).maybeSingle();
        if (!orcamento) return res.status(404).json({ sucesso: false, erro: 'Orçamento não configurado.' });

        let dailyBudget = orcamento.valor;
        if (orcamento.periodo === 'semanal') dailyBudget /= 7;
        else if (orcamento.periodo === 'mensal') dailyBudget /= 30;

        const { data: alimentos } = await supabase
            .from('alimentos')
            .select('id, nome_descricao, calorias, proteinas, carboidratos, gorduras, precos(preco_medio)')
            .not('precos', 'is', null);

        const availableFoods = (alimentos || []).filter(a => a.precos && a.precos.length > 0 && a.precos[0].preco_medio > 0);

        if (availableFoods.length === 0) {
            return res.status(404).json({ sucesso: false, erro: 'Nenhum alimento com preço encontrado no banco.' });
        }

        const costEffectiveFoods = availableFoods.sort((a, b) => {
            const valA = a.calorias / a.precos[0].preco_medio;
            const valB = b.calorias / b.precos[0].preco_medio;
            return valB - valA;
        });

        let selectedItems = [];
        let totalCost = 0;
        let totalCal = 0;

        for (const food of costEffectiveFoods) {
            const pricePer100g = food.precos[0].preco_medio;
            
            if (totalCost + pricePer100g <= dailyBudget && totalCal + food.calorias <= meta.calorias_dia + 100) {
                selectedItems.push({
                    alimento_id: food.id,
                    quantidade_gramas: 100,
                    preco_calculado: parseFloat(pricePer100g.toFixed(2))
                });
                totalCost += pricePer100g;
                totalCal += food.calorias;
            }
            
            if (totalCal >= meta.calorias_dia - 100 || totalCost >= dailyBudget) {
                break;
            }
        }

        if (selectedItems.length === 0) {
            return res.status(400).json({ sucesso: false, erro: 'Orçamento muito baixo para gerar cardápio.' });
        }

        const { data: novaLista, error: errLista } = await supabase
            .from('listas')
            .insert([{ usuario_id, titulo_lista: 'Sugestao', tipo: 'mercado' }])
            .select()
            .single();

        if (errLista) throw errLista;

        const itensToInsert = selectedItems.map(item => ({ lista_id: novaLista.id, ...item }));
        const { error: errItens } = await supabase.from('lista_itens').insert(itensToInsert);
        if (errItens) throw errItens;

        res.status(201).json({ sucesso: true, mensagem: 'Cardápio gerado com sucesso!', lista: novaLista });
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
        dailyBudget = orcamento.valor;
        if (orcamento.periodo === 'semanal') dailyBudget /= 7;
        else if (orcamento.periodo === 'mensal') dailyBudget /= 30;
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
            .single();

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

    if (!peso_atual || !altura_cm || !objetivo) {
        return res.status(400).json({
            sucesso: false,
            erro: 'Informe peso_atual, altura_cm e objetivo.'
        });
    }

    const idade = 25;
    const tmb = 88.36 + (13.4 * peso_atual) + (4.8 * altura_cm) - (5.7 * idade);
    const fatorAtividade = 1.375; // levemente ativo

    let calorias, proteinas, carboidratos, gorduras, peso_meta;

    if (objetivo === 'emagrecer') {
        calorias = Math.round(tmb * fatorAtividade * 0.80); // déficit 20%
        proteinas = Math.round(peso_atual * 2.2);
        gorduras = Math.round(peso_atual * 0.8);
        carboidratos = Math.round((calorias - proteinas * 4 - gorduras * 9) / 4);
        peso_meta = reqPesoMeta ? reqPesoMeta : parseFloat((peso_atual * 0.90).toFixed(1));
    } else if (objetivo === 'engordar') {
        calorias = Math.round(tmb * fatorAtividade * 1.15); // superávit 15%
        proteinas = Math.round(peso_atual * 2.0);
        gorduras = Math.round(peso_atual * 1.0);
        carboidratos = Math.round((calorias - proteinas * 4 - gorduras * 9) / 4);
        peso_meta = reqPesoMeta ? reqPesoMeta : parseFloat((peso_atual * 1.05).toFixed(1));
    } else { // manter
        calorias = Math.round(tmb * fatorAtividade);
        proteinas = Math.round(peso_atual * 1.8);
        gorduras = Math.round(peso_atual * 0.9);
        carboidratos = Math.round((calorias - proteinas * 4 - gorduras * 9) / 4);
        peso_meta = reqPesoMeta ? reqPesoMeta : peso_atual;
    }

    if (carboidratos < 0) carboidratos = 30;

    try {
        const { data: existente } = await supabase
            .from('metas_usuario')
            .select('id')
            .eq('usuario_id', usuario_id)
            .single();

        const payload = {
            usuario_id,
            peso_atual: parseFloat(peso_atual),
            altura_cm: parseFloat(altura_cm),
            peso_meta,
            objetivo,
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

// ── GET /api/metas ──────────────────────────────────────────────────
app.get('/api/metas', verificarAutenticacao, async (req, res) => {
    const usuario_id = req.usuarioId;
    try {
        const { data, error } = await supabase
            .from('metas_usuario')
            .select('*')
            .eq('usuario_id', usuario_id)
            .single();

        if (error || !data) {
            return res.status(404).json({ sucesso: false, erro: 'Nenhuma meta encontrada.' });
        }

        // Busca sugestões junto com a meta
        const { data: sugestoes } = await supabase
            .from('alimentos')
            .select('id, nome_descricao, calorias, proteinas, carboidratos, gorduras, precos(preco_medio)')
            .gt('proteinas', 5)
            .lte('calorias', data.objetivo === 'emagrecer' ? 200 : 400)
            .limit(6);

        res.json({
            sucesso: true,
            meta: data,
            sugestoes: sugestoes || []
        });
    } catch (error) {
        res.status(500).json({ sucesso: false, erro: error.message });
    }
});

// ── GET /api/orcamento ──────────────────────────────────────────────
app.get('/api/orcamento', verificarAutenticacao, async (req, res) => {
    const usuario_id = req.usuarioId;
    try {
        const { data, error } = await supabase
            .from('orcamentos')
            .select('*')
            .eq('usuario_id', usuario_id)
            .single();

        if (error || !data) {
            return res.status(404).json({ sucesso: false, erro: 'Nenhum orçamento encontrado.' });
        }
        res.json({ sucesso: true, orcamento: data });
    } catch (error) {
        res.status(500).json({ sucesso: false, erro: error.message });
    }
});

// ── GET /api/cardapio ───────────────────────────────────────────────
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

        const dados = (data || []).map(lista => ({
            id: lista.id,
            titulo_lista: lista.titulo_lista,
            tipo: lista.tipo,
            lista_itens: (lista.lista_itens || []).map(item => ({
                id: item.id,
                quantidade_gramas: item.quantidade_gramas,
                preco_calculado: item.preco_calculado,
                alimentos: item.alimentos
            })),
            totalItens: (lista.lista_itens || []).length,
            valorTotal: (lista.lista_itens || []).reduce(
                (soma, item) => soma + Number(item.preco_calculado || 0), 0
            )
        }));

        res.json({ sucesso: true, dados });
    } catch (error) {
        res.status(500).json({ sucesso: false, erro: error.message });
    }
});


const PORT = process.env.PORT || 3000;
app.listen(PORT, () => {
    console.log(`Backend do Prato Certo rodando na porta ${PORT}`);
});