package com.example.pratocerto.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pratocerto.model.ResumoLista
import com.example.pratocerto.network.RetrofitInstance
import com.example.pratocerto.util.SessionManager
import kotlinx.coroutines.launch

class CardapioViewModel : ViewModel() {

    var lista by mutableStateOf<ResumoLista?>(null)
        private set

    var carregando by mutableStateOf(false)
        private set

    var erro by mutableStateOf<String?>(null)
        private set

    // Carrega a lista de id 1 por padrão.
    // Quando o app tiver criação de lista, passe o id correto.
    fun carregarLista(idLista: Int = 1) {
        viewModelScope.launch {
            carregando = true
            erro = null
            try {
                val resposta = RetrofitInstance.api.buscarLista(
                    token = SessionManager.bearer(),
                    id = idLista
                )
                if (resposta.sucesso) {
                    lista = resposta.resumo
                } else {
                    erro = resposta.erro ?: "Lista não encontrada."
                }
            } catch (e: Exception) {
                erro = "Erro de conexão: ${e.message}"
            } finally {
                carregando = false
            }
        }
    }
}
