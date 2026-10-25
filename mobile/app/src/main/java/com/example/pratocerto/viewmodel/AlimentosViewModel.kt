package com.example.pratocerto.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pratocerto.model.Alimento
import com.example.pratocerto.model.MetaData
import com.example.pratocerto.network.RetrofitInstance
import com.example.pratocerto.util.SessionManager
import kotlinx.coroutines.launch

class AlimentosViewModel : ViewModel() {

    var alimentos by mutableStateOf<List<Alimento>>(emptyList())
        private set

    var meta by mutableStateOf<MetaData?>(null)
        private set

    var carregando by mutableStateOf(false)
        private set

    var erro by mutableStateOf<String?>(null)
        private set

    var termoPesquisa by mutableStateOf("")

    init {
        carregarMeta()
    }

    private fun carregarMeta() {
        viewModelScope.launch {
            try {
                val resposta = RetrofitInstance.api.buscarMeta(SessionManager.bearer())
                if (resposta.sucesso) meta = resposta.meta
            } catch (_: Exception) {
                // sem meta ainda — ignora
            }
        }
    }

    fun buscar(termo: String) {
        if (termo.isBlank()) return
        val t = termo.trim()
        if (t.all { it.isDigit() } && t.length >= 8) {
            buscarPorCodigoBarras(t)
            return
        }

        viewModelScope.launch {
            carregando = true
            erro = null
            try {
                val resposta = RetrofitInstance.api.buscarAlimentos(t)
                alimentos = resposta.dados.orEmpty()
            } catch (e: Exception) {
                erro = "Erro de conexão: ${e.message}"
            } finally {
                carregando = false
            }
        }
    }

    fun buscarPorCodigoBarras(codigo: String, onEncontrado: (Alimento) -> Unit = {}) {
        val cod = codigo.trim()
        if (cod.isBlank()) return
        viewModelScope.launch {
            carregando = true
            erro = null
            try {
                val resposta = RetrofitInstance.api.buscarPorCodigoBarras(cod)
                if (resposta.sucesso && resposta.dado != null) {
                    val produto = resposta.dado
                    alimentos = listOf(produto) + alimentos.filter { it.id != produto.id }
                    onEncontrado(produto)
                } else {
                    erro = resposta.erro ?: "Produto não encontrado no Open Food Facts para o código: $cod"
                }
            } catch (e: Exception) {
                erro = "Erro ao consultar código de barras: ${e.message}"
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