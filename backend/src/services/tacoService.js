const fs = require('fs');
const path = require('path');

class TacoService {
    constructor() {
        this.tacoDataPath = path.join(__dirname, '../data/taco.json');
        this.alimentos = this._carregarDados();
    }

    _carregarDados() {
        try {
            const data = fs.readFileSync(this.tacoDataPath, 'utf8');
            return JSON.parse(data);
        } catch (error) {
            console.error('Erro ao carregar a base de dados da TACO:', error);
            return [];
        }
    }

    /**
     * @param {string} termo 
     * @returns {Array}
     */
    buscarPorNome(termo) {
        if (!termo) return [];

        const termoFormatado = termo.toLowerCase().trim();

        const resultados = this.alimentos.filter(alimento => 
            alimento.description.toLowerCase().includes(termoFormatado)
        );

        return resultados.map(alimento => ({
            id_taco: alimento.id,
            descricao: alimento.description,
            porcao_base: `${alimento.base_qty}${alimento.base_unit}`,
            nutrientes: {
                calorias: alimento.attributes.energy?.kcal || 0,
                proteinas: alimento.attributes.protein?.qty || 0,
                carboidratos: alimento.attributes.carbohydrate?.qty || 0,
                gorduras: alimento.attributes.lipid?.qty || 0
            }
        }));
    }
}

module.exports = new TacoService();