const supabase = require('../config/db');
const CardapioService = require('../services/cardapioService');

exports.buscarCardapios = async (req, res) => {
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
};

exports.gerarCardapio = async (req, res) => {
    const usuario_id = req.usuarioId;
    try {
        const novaLista = await CardapioService.gerarListaCardapio(usuario_id);
        res.status(201).json({ sucesso: true, mensagem: 'Cardápio gerado com sucesso!', lista: novaLista });
    } catch (error) {
        const isClientError = error.message.includes('não configurado') || error.message.includes('muito baixo');
        res.status(isClientError ? 400 : 500).json({ sucesso: false, erro: error.message });
    }
};
