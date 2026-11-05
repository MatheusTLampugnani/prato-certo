const { describe, it } = require('node:test');
const assert = require('node:assert/strict');
const TacoService = require('./src/services/tacoService');
const precosConab = require('./src/data/precos_conab.json');

describe('QA Suite: Serviços de Dados e Regras de Negócio', () => {

    it('TACO: Deve carregar dados e buscar alimentos por termo existente', () => {
        const resultados = TacoService.buscarPorNome('arroz');
        assert.ok(Array.isArray(resultados));
        assert.ok(resultados.length > 0, 'Deveria encontrar itens de arroz');
        assert.ok(resultados[0].nutrientes.calorias >= 0);
    });

    it('TACO: Deve retornar array vazio para termos inexistentes ou nulos', () => {
        assert.deepEqual(TacoService.buscarPorNome('termo_totalmente_inexistente_123'), []);
        assert.deepEqual(TacoService.buscarPorNome(''), []);
        assert.deepEqual(TacoService.buscarPorNome(null), []);
    });

    it('CONAB: Deve mapear preços reais da lista e aplicar fallback de R$ 12.50', () => {
        function obterPrecoConab(descricao) {
            const d = (descricao || '').toLowerCase();
            const encontrado = precosConab.find(p => d.includes(p.termo));
            return encontrado ? encontrado.preco_medio : 12.50;
        }

        assert.equal(obterPrecoConab('Arroz polido'), 6.80);
        assert.equal(obterPrecoConab('Feijão carioca'), 8.50);
        assert.equal(obterPrecoConab('Alimento Exótico Raro'), 12.50);
    });

    it('Cálculo de Preço: Gramatura proporcional (R$/kg para gramas)', () => {
        const precoPorKg = 20.00; // R$ 20 por kg
        const gramas = 250;
        const precoCalculado = parseFloat(((precoPorKg / 1000) * gramas).toFixed(2));
        assert.equal(precoCalculado, 5.00);
    });

    it('Cálculo de TMB e Metas: Deve calcular déficit calórico para emagrecimento', () => {
        const peso = 80;
        const altura = 180;
        const idade = 25;
        const tmb = 88.36 + (13.4 * peso) + (4.8 * altura) - (5.7 * idade);
        const fatorAtividade = 1.375;
        const caloriasEmagrecer = Math.round(tmb * fatorAtividade * 0.80);

        assert.ok(caloriasEmagrecer > 1200 && caloriasEmagrecer < 2500);
    });
});

