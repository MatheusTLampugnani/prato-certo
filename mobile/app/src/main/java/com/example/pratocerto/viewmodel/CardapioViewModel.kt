package com.example.pratocerto.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pratocerto.model.AdicionarItemRequest
import com.example.pratocerto.model.CardapioItemData
import com.example.pratocerto.model.CriarListaRequest
import com.example.pratocerto.network.RetrofitInstance
import com.example.pratocerto.util.SessionManager
import kotlinx.coroutines.launch

class CardapioViewModel : ViewModel() {

    var listasCardapio by mutableStateOf<List<CardapioItemData>>(emptyList())
        private set

    var orcamentoValor by mutableStateOf<Double?>(null)
        private set

    var carregando by mutableStateOf(false)
        private set

    var erro by mutableStateOf<String?>(null)
        private set

    // ── Carregar Cardápios/Listas ─────────────────────────────────────────────
    fun carregarCardapio() {
        viewModelScope.launch {
            carregando = true
            erro = null
            try {
                val resposta = RetrofitInstance.api.buscarCardapio(
                    token = SessionManager.bearer()
                )
                if (resposta.sucesso) {
                    listasCardapio = resposta.dados
                } else {
                    erro = resposta.erro ?: "Erro ao carregar cardápio."
                }
            } catch (e: Exception) {
                erro = "Erro de conexão: ${e.message}"
            } finally {
                carregando = false
            }
        }
    }

    // ── Carregar Orçamento Salvo ──────────────────────────────────────────────
    fun carregarOrcamento() {
        viewModelScope.launch {
            try {
                val resposta = RetrofitInstance.api.buscarOrcamento(
                    token = SessionManager.bearer()
                )
                if (resposta.sucesso) {
                    orcamentoValor = resposta.orcamento?.valor
                }
            } catch (_: Exception) {
                // Silencioso se o usuário ainda não tiver cadastrado orçamento
            }
        }
    }

    // ── Criar Novo Cardápio ───────────────────────────────────────────────────
    fun criarNovoCardapio(titulo: String) {
        viewModelScope.launch {
            carregando = true
            try {
                val resposta = RetrofitInstance.api.criarLista(
                    token = SessionManager.bearer(),
                    body = CriarListaRequest(titulo_lista = titulo, tipo = "cardapio")
                )
                if (resposta.sucesso) {
                    carregarCardapio()
                } else {
                    erro = resposta.erro ?: "Erro ao criar cardápio."
                }
            } catch (e: Exception) {
                erro = "Erro ao criar: ${e.message}"
            } finally {
                carregando = false
            }
        }
    }

    // ── Adicionar Alimento a um Cardápio ──────────────────────────────────────
    fun adicionarAlimentoNaLista(idLista: Int, alimentoId: Int, quantidadeGramas: Double) {
        viewModelScope.launch {
            try {
                val resposta = RetrofitInstance.api.adicionarItemLista(
                    token = SessionManager.bearer(),
                    idLista = idLista,
                    body = AdicionarItemRequest(
                        alimento_id = alimentoId,
                        quantidade_gramas = quantidadeGramas
                    )
                )
                if (resposta.sucesso) {
                    carregarCardapio()
                } else {
                    erro = resposta.erro ?: "Erro ao adicionar item."
                }
            } catch (e: Exception) {
                erro = "Erro ao adicionar: ${e.message}"
            }
        }
    }
}