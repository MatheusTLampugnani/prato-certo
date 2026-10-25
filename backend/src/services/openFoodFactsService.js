const supabase = require('../config/db');

class OpenFoodFactsService {
    /**
     * Busca dados nutricionais e detalhes de um produto pelo código de barras.
     * Salva ou recupera o alimento no banco de dados para permitir adição a listas/cardápios.
     * @param {string} barcode 
     * @returns {Promise<Object|null>}
     */
    async buscarPorCodigoBarras(barcode) {
        if (!barcode || !barcode.trim()) {
            return null;
        }

        const codigoLimpo = barcode.trim();

        // 1. Consulta a API pública do Open Food Facts
        const url = `https://world.openfoodfacts.org/api/v2/product/${codigoLimpo}.json`;
        const response = await fetch(url);
        
        if (!response.ok) {
            if (response.status === 404) return null;
            throw new Error(`Erro ao conectar com Open Food Facts (HTTP ${response.status})`);
        }

        const json = await response.json();
        if (json.status !== 1 || !json.product) {
            return null;
        }

        const p = json.product;
        const nutriments = p.nutriments || {};

        const nomeBase = p.product_name || p.generic_name || 'Produto sem nome';
        const marca = p.brands ? ` (${p.brands})` : '';
        const nomeCompleto = `${nomeBase}${marca}`;

        const calorias = parseFloat((nutriments['energy-kcal_100g'] ?? nutriments['energy-kcal'] ?? 0).toFixed(1));
        const proteinas = parseFloat((nutriments['proteins_100g'] ?? 0).toFixed(1));
        const carboidratos = parseFloat((nutriments['carbohydrates_100g'] ?? 0).toFixed(1));
        const gorduras = parseFloat((nutriments['fat_100g'] ?? 0).toFixed(1));
        const porcaoBase = p.serving_size || '100g';
        const imagemUrl = p.image_front_url || p.image_url || null;

        // 2. Verifica se o alimento já existe no banco Supabase
        const { data: existente } = await supabase
            .from('alimentos')
            .select('id, nome_descricao, calorias, proteinas, carboidratos, gorduras, porcao_base, precos(preco_medio, fonte_dado)')
            .ilike('nome_descricao', `%${nomeBase}%`)
            .limit(1)
            .maybeSingle();

        if (existente) {
            return {
                id: existente.id,
                nome_descricao: existente.nome_descricao,
                calorias: existente.calorias,
                proteinas: existente.proteinas,
                carboidratos: existente.carboidratos,
                gorduras: existente.gorduras,
                porcao_base: existente.porcao_base,
                precos: existente.precos || [{ preco_medio: 9.90, fonte_dado: 'Open Food Facts' }],
                imagem_url: imagemUrl,
                origem: 'Open Food Facts'
            };
        }

        // 3. Cadastra o novo alimento no Supabase
        const { data: novoAlimento, error: errInsert } = await supabase
            .from('alimentos')
            .insert([{
                nome_descricao: nomeCompleto,
                calorias,
                proteinas,
                carboidratos,
                gorduras,
                porcao_base: porcaoBase
            }])
            .select()
            .single();

        if (errInsert) {
            console.error('Erro ao cadastrar alimento do Open Food Facts no banco:', errInsert);
            return {
                id: 0,
                nome_descricao: nomeCompleto,
                calorias,
                proteinas,
                carboidratos,
                gorduras,
                porcao_base: porcaoBase,
                precos: [{ preco_medio: 9.90, fonte_dado: 'Open Food Facts' }],
                imagem_url: imagemUrl,
                origem: 'Open Food Facts'
            };
        }

        // Preço estimado de referência para produtos de código de barras
        const precoEstimado = 9.90;
        await supabase
            .from('precos')
            .insert([{
                alimento_id: novoAlimento.id,
                preco_medio: precoEstimado,
                fonte_dado: 'Open Food Facts'
            }]);

        return {
            id: novoAlimento.id,
            nome_descricao: novoAlimento.nome_descricao,
            calorias: novoAlimento.calorias,
            proteinas: novoAlimento.proteinas,
            carboidratos: novoAlimento.carboidratos,
            gorduras: novoAlimento.gorduras,
            porcao_base: novoAlimento.porcao_base,
            precos: [{ preco_medio: precoEstimado, fonte_dado: 'Open Food Facts' }],
            imagem_url: imagemUrl,
            origem: 'Open Food Facts'
        };
    }
}

module.exports = new OpenFoodFactsService();
