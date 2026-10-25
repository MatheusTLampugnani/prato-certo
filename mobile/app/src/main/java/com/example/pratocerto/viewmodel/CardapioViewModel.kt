package com.example.pratocerto.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pratocerto.model.AdicionarItemRequest
import com.example.pratocerto.model.CardapioItemData
import com.example.pratocerto.model.CriarListaRequest
import com.example.pratocerto.model.ToggleItemRequest
import com.example.pratocerto.network.RetrofitInstance
import com.example.pratocerto.util.SessionManager
import kotlinx.coroutines.launch

class CardapioViewModel : ViewModel() {

    var listasCardapio by mutableStateOf<List<CardapioItemData>>(emptyList())
        private set

    var orcamentoValor by mutableStateOf<Double?>(null)
        private set

    var orcamentoData by mutableStateOf<com.example.pratocerto.model.OrcamentoData?>(null)
        private set

    var carregando by mutableStateOf(false)
        private set

    var erro by mutableStateOf<String?>(null)
        private set

    fun carregarCardapio() {
        viewModelScope.launch {
            carregando = true
            erro = null
            try {
                carregarOrcamento()
                val resposta = RetrofitInstance.api.buscarCardapio(SessionManager.bearer())
                if (resposta.sucesso) {
                    // Garante que lista_itens nunca seja null em nenhum nível
                    listasCardapio = (resposta.dados ?: emptyList()).map { lista ->
                        lista.copy(itens = lista.itens.orEmpty())
                    }
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

    fun carregarOrcamento() {
        viewModelScope.launch {
            try {
                val resposta = RetrofitInstance.api.buscarOrcamento(SessionManager.bearer())
                if (resposta.sucesso) {
                    orcamentoData = resposta.orcamento
                    orcamentoValor = resposta.orcamento?.valor
                }
            } catch (_: Exception) { /* silencioso */ }
        }
    }

    fun criarNovoCardapio(titulo: String) {
        viewModelScope.launch {
            carregando = true
            try {
                val resposta = RetrofitInstance.api.criarLista(
                    token = SessionManager.bearer(),
                    body = CriarListaRequest(titulo_lista = titulo, tipo = "cardapio")
                )
                if (resposta.sucesso) carregarCardapio()
                else erro = resposta.erro ?: "Erro ao criar cardápio."
            } catch (e: Exception) {
                erro = "Erro: ${e.message}"
            } finally {
                carregando = false
            }
        }
    }

    fun criarNovaListaMercado(titulo: String) {
        viewModelScope.launch {
            carregando = true
            try {
                val resposta = RetrofitInstance.api.criarLista(
                    token = SessionManager.bearer(),
                    body = CriarListaRequest(titulo_lista = titulo, tipo = "mercado")
                )
                if (resposta.sucesso) carregarCardapio()
                else erro = resposta.erro ?: "Erro ao criar lista."
            } catch (e: Exception) {
                erro = "Erro: ${e.message}"
            } finally {
                carregando = false
            }
        }
    }

    fun gerarListaSugestao(onSucesso: () -> Unit = {}) {
        viewModelScope.launch {
            carregando = true
            try {
                val resposta = RetrofitInstance.api.gerarCardapioInteligente(SessionManager.bearer())
                if (resposta.sucesso) {
                    carregarCardapio()
                    onSucesso()
                } else {
                    erro = resposta.erro ?: "Erro ao gerar cardápio inteligente."
                }
            } catch (e: Exception) {
                erro = "Erro: ${e.message}"
            } finally {
                carregando = false
            }
        }
    }

    fun adicionarAlimentoNaLista(idLista: Int, alimentoId: Int, quantidadeGramas: Double, onSucesso: () -> Unit = {}) {
        viewModelScope.launch {
            try {
                val resposta = RetrofitInstance.api.adicionarItemLista(
                    token = SessionManager.bearer(),
                    idLista = idLista,
                    body = AdicionarItemRequest(alimento_id = alimentoId, quantidade_gramas = quantidadeGramas)
                )
                if (resposta.sucesso) {
                    carregarCardapio()
                    onSucesso()
                } else {
                    erro = resposta.erro ?: "Erro ao adicionar item."
                }
            } catch (e: Exception) {
                erro = "Erro: ${e.message}"
            }
        }
    }

    fun excluirLista(idLista: Int, onSucesso: () -> Unit = {}) {
        viewModelScope.launch {
            try {
                val resposta = RetrofitInstance.api.excluirLista(SessionManager.bearer(), idLista)
                if (resposta.sucesso) {
                    carregarCardapio()
                    onSucesso()
                } else {
                    erro = resposta.erro ?: "Erro ao excluir lista."
                }
            } catch (e: Exception) {
                erro = "Erro: ${e.message}"
            }
        }
    }

    fun salvarSugestaoComoLista(onSucesso: (String) -> Unit = {}, onErro: (String) -> Unit = {}) {
        viewModelScope.launch {
            try {
                val resposta = RetrofitInstance.api.salvarSugestaoComoLista(SessionManager.bearer())
                if (resposta.sucesso) {
                    carregarCardapio()
                    onSucesso(resposta.mensagem ?: "Lista 'Sugestão' salva com sucesso!")
                } else {
                    onErro(resposta.erro ?: "Não foi possível salvar a sugestão.")
                }
            } catch (e: Exception) {
                onErro("Erro de conexão: ${e.message}")
            }
        }
    }

    fun limparSugestao(idLista: Int, onSucesso: () -> Unit = {}) {
        viewModelScope.launch {
            carregando = true
            try {
                val resposta = RetrofitInstance.api.excluirLista(SessionManager.bearer(), idLista)
                if (resposta.sucesso) {
                    carregarCardapio()
                    onSucesso()
                } else {
                    erro = resposta.erro ?: "Erro ao limpar sugestão."
                }
            } catch (e: Exception) {
                erro = "Erro: ${e.message}"
            } finally {
                carregando = false
            }
        }
    }

    fun excluirItem(idItem: Int, onSucesso: () -> Unit = {}) {
        viewModelScope.launch {
            try {
                val resposta = RetrofitInstance.api.excluirItemLista(SessionManager.bearer(), idItem)
                if (resposta.sucesso) {
                    carregarCardapio()
                    onSucesso()
                } else {
                    erro = resposta.erro ?: "Erro ao excluir item."
                }
            } catch (e: Exception) {
                erro = "Erro: ${e.message}"
            }
        }
    }

    fun alternarItemComprado(idItem: Int, novoStatus: Boolean) {
        // Atualização otimista na lista da UI
        listasCardapio = listasCardapio.map { lista ->
            val novosItens = lista.itens?.map { item ->
                if (item.id == idItem) item.copy(comprado = novoStatus) else item
            }
            lista.copy(itens = novosItens)
        }

        viewModelScope.launch {
            try {
                RetrofitInstance.api.alternarItemComprado(
                    token = SessionManager.bearer(),
                    idItem = idItem,
                    body = ToggleItemRequest(novoStatus)
                )
            } catch (e: Exception) {
                erro = "Erro ao atualizar status: ${e.message}"
                carregarCardapio() // Em caso de erro, sincroniza com o banco
            }
        }
    }
}
