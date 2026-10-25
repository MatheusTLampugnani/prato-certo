const supabase = require('../config/db');

exports.gerarListaCardapio = async (usuario_id) => {
    const { data: meta } = await supabase.from('metas_usuario').select('*').eq('usuario_id', usuario_id).single();
    if (!meta) throw new Error('Meta não configurada.');

    const { data: orcamento } = await supabase.from('orcamentos').select('*').eq('usuario_id', usuario_id).maybeSingle();
    if (!orcamento) throw new Error('Orçamento não configurado.');

    let dailyBudget = orcamento.valor;
    if (orcamento.periodo === 'semanal') dailyBudget /= 7;
    else if (orcamento.periodo === 'mensal') dailyBudget /= 30;

    const { data: alimentos } = await supabase
        .from('alimentos')
        .select('id, nome_descricao, calorias, proteinas, carboidratos, gorduras, precos(preco_medio)')
        .not('precos', 'is', null);

    const availableFoods = (alimentos || []).filter(a => a.precos && a.precos.length > 0 && a.precos[0].preco_medio > 0);

    if (availableFoods.length === 0) {
        throw new Error('Nenhum alimento com preço encontrado no banco.');
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
        throw new Error('Orçamento muito baixo para gerar cardápio.');
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

    return novaLista;
};
