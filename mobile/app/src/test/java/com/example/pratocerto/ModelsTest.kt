package com.example.pratocerto

import com.example.pratocerto.model.Alimento
import com.example.pratocerto.model.Preco
import org.junit.Assert.assertEquals
import org.junit.Test

class ModelsTest {

    @Test
    fun alimento_retornaPrecoCorreto_quandoPossuiListaDePrecos() {
        val alimento = Alimento(
            id = 1,
            nome = "Feijão Preto",
            calorias = 77.0,
            proteinas = 4.5,
            precos = listOf(Preco(precoMedio = 8.50))
        )
        assertEquals(8.50, alimento.preco, 0.001)
    }

    @Test
    fun alimento_retornaPrecoZero_quandoListaDePrecosForNulaOuVazia() {
        val alimentoSemPreco = Alimento(id = 2, nome = "Alimento Sem Preço", precos = null)
        assertEquals(0.0, alimentoSemPreco.preco, 0.001)

        val alimentoListaVazia = Alimento(id = 3, nome = "Lista Vazia", precos = emptyList())
        assertEquals(0.0, alimentoListaVazia.preco, 0.001)
    }

    @Test
    fun calculoDeProjecaoOrcamentaria_diarioDeveDividirCorretamente() {
        val orcamentoMensal = 600.0
        val diasNoMes = 30
        val diarioCalculado = orcamentoMensal / diasNoMes
        assertEquals(20.0, diarioCalculado, 0.001)
    }
}

