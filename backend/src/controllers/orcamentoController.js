const supabase = require('../config/db');

exports.salvarOrcamento = async (req, res) => {
    const usuario_id = req.usuarioId;
    const { valor, periodo } = req.body;

    try {
        const { data: existente } = await supabase
            .from('orcamentos')
            .select('id')
            .eq('usuario_id', usuario_id)
            .maybeSingle();

        let orcamentoFinal;
        if (existente) {
            const { data, error: erroUpdate } = await supabase
                .from('orcamentos')
                .update({ valor, periodo })
                .eq('usuario_id', usuario_id)
                .select().single();

            if (erroUpdate) throw erroUpdate;
            orcamentoFinal = data;
        } else {
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
};

exports.buscarOrcamento = async (req, res) => {
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
};
