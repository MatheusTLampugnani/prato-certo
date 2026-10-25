const supabase = require('../config/db');
const MetasService = require('../services/metasService');

exports.buscarMetas = async (req, res) => {
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

        const sugestoes = await MetasService.buscarSugestoesComOrcamento(usuario_id, data.objetivo);

        res.json({ sucesso: true, meta: data, sugestoes });
    } catch (error) {
        res.status(500).json({ sucesso: false, erro: error.message });
    }
};

exports.salvarMetas = async (req, res) => {
    const { peso_atual, altura_cm, objetivo, peso_meta: reqPesoMeta } = req.body;
    const usuario_id = req.usuarioId;

    if (!peso_atual || !altura_cm || !objetivo) {
        return res.status(400).json({ sucesso: false, erro: 'Informe peso_atual, altura_cm e objetivo.' });
    }

    try {
        const macros = MetasService.calcularMacros(peso_atual, altura_cm, objetivo, reqPesoMeta);

        const { data: existente } = await supabase
            .from('metas_usuario')
            .select('id')
            .eq('usuario_id', usuario_id)
            .single();

        const payload = {
            usuario_id,
            peso_atual: parseFloat(peso_atual),
            altura_cm: parseFloat(altura_cm),
            peso_meta: macros.peso_meta,
            objetivo,
            calorias_dia: macros.calorias,
            proteinas_dia: macros.proteinas,
            carboidratos_dia: macros.carboidratos,
            gorduras_dia: macros.gorduras
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

        const sugestoes = await MetasService.buscarSugestoesComOrcamento(usuario_id, objetivo);

        res.json({
            sucesso: true,
            mensagem: 'Meta salva!',
            meta: resultado,
            sugestoes: sugestoes || []
        });
    } catch (error) {
        res.status(500).json({ sucesso: false, erro: error.message });
    }
};
