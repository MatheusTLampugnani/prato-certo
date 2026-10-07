package com.example.pratocerto.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pratocerto.model.Alimento
import com.example.pratocerto.model.MetaData
import com.example.pratocerto.model.MetaRequest
import com.example.pratocerto.network.RetrofitInstance
import com.example.pratocerto.util.SessionManager
import kotlinx.coroutines.launch

class MetasViewModel : ViewModel() {

    var meta by mutableStateOf<MetaData?>(null)
        private set

    var sugestoes by mutableStateOf<List<Alimento>>(emptyList())
        private set

    var carregando by mutableStateOf(false)
        private set

    var erro by mutableStateOf<String?>(null)
        private set

    var salvoComSucesso by mutableStateOf(false)
        private set

    init { carregarMeta() }

    fun carregarMeta() {
        viewModelScope.launch {
            carregando = true
            erro = null
            try {
                val resposta = RetrofitInstance.api.buscarMeta(SessionManager.bearer())
                if (resposta.sucesso) {
                    meta = resposta.meta
                    sugestoes = resposta.sugestoes.orEmpty()
                }
            } catch (_: Exception) {
                // sem meta ainda — normal
            } finally {
                carregando = false
            }
        }
    }

    fun salvar(pesoAtual: Double, alturaCm: Double, objetivo: String, pesoMeta: Double?) {
        viewModelScope.launch {
            carregando = true
            erro = null
            try {
                val resposta = RetrofitInstance.api.salvarMeta(
                    token = SessionManager.bearer(),
                    body = MetaRequest(pesoAtual, alturaCm, objetivo, pesoMeta)
                )
                if (resposta.sucesso) {
                    meta = resposta.meta
                    sugestoes = resposta.sugestoes.orEmpty()
                    salvoComSucesso = true
                } else {
                    erro = resposta.erro ?: "Erro ao salvar meta."
                }
            } catch (e: Exception) {
                erro = "Erro de conexão: ${e.message}"
            } finally {
                carregando = false
            }
        }
    }

    fun resetSucesso() { salvoComSucesso = false }
}
