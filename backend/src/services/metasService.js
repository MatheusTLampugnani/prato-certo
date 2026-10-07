const supabase = require('../config/db');

exports.calcularMacros = (peso, alturaCm, objetivo, reqPesoMeta) => {
    const idade = 25; // TODO: Obter da requisição
    const tmb = 88.36 + (13.4 * peso) + (4.8 * alturaCm) - (5.7 * idade);
    const fatorAtividade = 1.375; // levemente ativo

    let calorias, proteinas, carboidratos, gorduras, peso_meta;

    if (objetivo === 'emagrecer') {
        calorias = Math.round(tmb * fatorAtividade * 0.80);
        proteinas = Math.round(peso * 2.2);
        gorduras = Math.round(peso * 0.8);
        carboidratos = Math.round((calorias - proteinas * 4 - gorduras * 9) / 4);
        peso_meta = reqPesoMeta ? reqPesoMeta : parseFloat((peso * 0.90).toFixed(1));
    } else if (objetivo === 'engordar') {
        calorias = Math.round(tmb * fatorAtividade * 1.15);
        proteinas = Math.round(peso * 2.0);
        gorduras = Math.round(peso * 1.0);
        carboidratos = Math.round((calorias - proteinas * 4 - gorduras * 9) / 4);
        peso_meta = reqPesoMeta ? reqPesoMeta : parseFloat((peso * 1.05).toFixed(1));
    } else {
        calorias = Math.round(tmb * fatorAtividade);
        proteinas = Math.round(peso * 1.8);
        gorduras = Math.round(peso * 0.9);
        carboidratos = Math.round((calorias - proteinas * 4 - gorduras * 9) / 4);
        peso_meta = reqPesoMeta ? reqPesoMeta : peso;
    }

    if (carboidratos < 0) carboidratos = 30;

    return { calorias, proteinas, carboidratos, gorduras, peso_meta };
};

exports.buscarSugestoesComOrcamento = async (usuario_id, objetivo) => {
    const { data: orcamento } = await supabase.from('orcamentos').select('*').eq('usuario_id', usuario_id).maybeSingle();
    
    let dailyBudget = 0;
    if (orcamento) {
        dailyBudget = orcamento.valor;
        if (orcamento.periodo === 'semanal') dailyBudget /= 7;
        else if (orcamento.periodo === 'mensal') dailyBudget /= 30;
    }

    const maxCalorias = objetivo === 'emagrecer' ? 200 : 400;

    const { data: alimentos } = await supabase
        .from('alimentos')
        .select('id, nome_descricao, calorias, proteinas, carboidratos, gorduras, precos!inner(preco_medio)')
        .gt('proteinas', 5)
        .lte('calorias', maxCalorias);

    let availableFoods = alimentos || [];

    if (dailyBudget > 0) {
        availableFoods.sort((a, b) => {
            const valA = a.calorias / a.precos[0].preco_medio;
            const valB = b.calorias / b.precos[0].preco_medio;
            return valB - valA;
        });
    }

    return availableFoods.slice(0, 6);
};
