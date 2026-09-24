package com.example.pratocerto.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pratocerto.model.OrcamentoRequest
import com.example.pratocerto.network.RetrofitInstance
import com.example.pratocerto.util.SessionManager
import kotlinx.coroutines.launch

class OrcamentoViewModel : ViewModel() {
    var carregando by mutableStateOf(false)
        private set
    var erro by mutableStateOf<String?>(null)
        private set
    var sucesso by mutableStateOf(false)
        private set

    fun salvarOrcamento(valor: Double, periodo: String) {
        viewModelScope.launch {
            carregando = true
            erro = null
            sucesso = false
            try {
                val resposta = RetrofitInstance.api.salvarOrcamento(
                    token = SessionManager.bearer(),
                    body = OrcamentoRequest(valor, periodo)
                )
                if (resposta.sucesso) {
                    sucesso = true
                } else {
                    erro = resposta.erro ?: "Erro ao salvar orçamento."
                }
            } catch (e: Exception) {
                erro = "Erro de conexão: ${e.message}"
            } finally {
                carregando = false
            }
        }
    }

    fun resetStatus() {
        sucesso = false
        erro = null
    }
}