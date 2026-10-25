const supabase = require('../config/db');

exports.criarLista = async (req, res) => {
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
};

exports.adicionarItem = async (req, res) => {
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
};

exports.buscarListaPorId = async (req, res) => {
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
};
