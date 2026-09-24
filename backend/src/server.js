const express = require('express');
const bcrypt = require('bcryptjs');
require('dotenv').config();
const jwt = require('jsonwebtoken');
const supabase = require('./config/db'); 
const TacoService = require('./services/tacoService');
const verificarAutenticacao = require('./middleware/auth'); 

const app = express();
app.use(express.json());

const JWT_SECRET = process.env.JWT_SECRET || '472FuXFoFWtjkSZFwAerJ3ZR9O3PKnwpG/3sOh2kMwT/2yVUa3rlHJboLX4QCpOJVveVxOKki+HhMFfwoGMVHA==';

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

        if (existente) {
            // 2. Se já existir, atualiza o valor
            const { error: erroUpdate } = await supabase
                .from('orcamentos')
                .update({ valor, periodo })
                .eq('usuario_id', usuario_id);
            
            if (erroUpdate) throw erroUpdate;
        } else {
            // 3. Se não existir, cria um novo registro
            const { error: erroInsert } = await supabase
                .from('orcamentos')
                .insert([{ usuario_id, valor, periodo }]);
            
            if (erroInsert) throw erroInsert;
        }

        res.json({ sucesso: true, mensagem: "Orçamento salvo com sucesso!" });
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

        res.json({ sucesso: true, dados: listas || [] });
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

        if (precoError || !precoData) {
            return res.status(404).json({ sucesso: false, erro: 'Preço do alimento não encontrado.' });
        }

        const preco_calculado = (precoData.preco_medio / 100) * quantidade_gramas;

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

const PORT = process.env.PORT || 3000;
app.listen(PORT, () => {
    console.log(`Backend do Prato Certo rodando na porta ${PORT}`);
});