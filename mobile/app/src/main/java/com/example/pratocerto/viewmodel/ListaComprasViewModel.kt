package com.example.pratocerto.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pratocerto.model.ItemCompra
import com.example.pratocerto.network.RetrofitInstance
import kotlinx.coroutines.launch

class ListaComprasViewModel : ViewModel() {

    var itens by mutableStateOf<List<ItemCompra>>(emptyList())
        private set

    var carregando by mutableStateOf(false)
        private set

    var erro by mutableStateOf<String?>(null)
        private set

    fun carregarItens() {
        viewModelScope.launch {
            carregando = true
            erro = null
            try {
                val resposta = RetrofitInstance.api.listarItens()
                itens = resposta.dados
            } catch (e: Exception) {
                erro = "Erro ao carregar: ${e.message}"
            } finally {
                carregando = false
            }
        }
    }
}