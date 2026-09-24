package com.example.pratocerto.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pratocerto.model.Alimento
import com.example.pratocerto.network.RetrofitInstance
import kotlinx.coroutines.launch

class AlimentosViewModel : ViewModel() {

    var alimentos by mutableStateOf<List<Alimento>>(emptyList())
        private set

    var carregando by mutableStateOf(false)
        private set

    var erro by mutableStateOf<String?>(null)
        private set

    var termoPesquisa by mutableStateOf("")

    fun buscar(termo: String) {
        if (termo.isBlank()) return
        viewModelScope.launch {
            carregando = true
            erro = null
            try {
                val resposta = RetrofitInstance.api.buscarAlimentos(termo)
                alimentos = resposta.dados
            } catch (e: Exception) {
                erro = "Erro de conexão: ${e.message}"
            } finally {
                carregando = false
            }
        }
    }

    fun limpar() {
        alimentos = emptyList()
        termoPesquisa = ""
        erro = null
    }
}
