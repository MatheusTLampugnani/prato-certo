const express = require('express');
const supabase = require('./config/db');
const TacoService = require('./services/tacoService');

const app = express();
app.use(express.json());

app.get('/api/teste-banco', async (req, res) => {
    try {
        const { data, error } = await supabase.from('alimentos').select('*').limit(1);
        
        if (error) throw error;

        res.json({
            sucesso: true,
            mensagem: 'Supabase conectado',
            dados: data
        });
    } catch (error) {
        res.status(500).json({ sucesso: false, erro: error.message });
    }
});

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
                .insert([{
                    alimento_id: novoAlimento.id,
                    preco_medio: parseFloat(precoAleatorio),
                    fonte_dado: 'CONAB'
                }]);

            if (erroPreco) throw erroPreco;
        }

        res.json({ sucesso: true, mensagem: 'Banco populado com dados da TACO e preços!' });
    } catch (error) {
        res.status(500).json({ sucesso: false, erro: error.message });
    }
});

app.get('/api/alimentos', async (req, res) => {
    const { pesquisa } = req.query;
    
    if (!pesquisa) {
        return res.status(400).json({ sucesso: false, erro: 'Envie um termo de pesquisa.' });
    }

    try {
        const { data, error } = await supabase
            .from('alimentos')
            .select(`
                id,
                nome_descricao,
                calorias,
                proteinas,
                precos (
                    preco_medio,
                    fonte_dado
                )
            `)
            .ilike('nome_descricao', `%${pesquisa}%`)
            .limit(10);

        if (error) throw error;

        res.json({ sucesso: true, total: data.length, dados: data });
    } catch (error) {
        res.status(500).json({ sucesso: false, erro: error.message });
    }
});

const PORT = process.env.PORT || 3000;
app.listen(PORT, () => {
    console.log(`Backend rodando na porta ${PORT}`);
});