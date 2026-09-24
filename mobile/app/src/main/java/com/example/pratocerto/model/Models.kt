package com.example.pratocerto.model

import com.google.gson.annotations.SerializedName

// ── Alimentos ──────────────────────────────────────────────────────────────

data class Preco(
    @SerializedName("preco_medio") val precoMedio: Double
)

data class Alimento(
    val id: Int,
    @SerializedName("nome_descricao") val nome: String,
    val calorias: Double,
    val proteinas: Double,
    val carboidratos: Double? = null,
    val gorduras: Double? = null,
    val precos: List<Preco> = emptyList()
) {
    val preco: Double get() = precos.firstOrNull()?.precoMedio ?: 0.0
}

data class RespostaAlimentos(
    val sucesso: Boolean,
    val total: Int,
    val dados: List<Alimento>
)

// ── Auth ───────────────────────────────────────────────────────────────────

data class UsuarioRegistro(
    val nome: String,
    val email: String,
    val senha: String
)

data class UsuarioLogin(
    val email: String,
    val senha: String
)

data class UsuarioInfo(
    val id: Int,
    val nome: String,
    val email: String
)

data class RespostaLogin(
    val sucesso: Boolean,
    val mensagem: String? = null,
    val token: String? = null,
    val usuario: UsuarioInfo? = null,
    val erro: String? = null
)

data class RespostaRegistro(
    val sucesso: Boolean,
    val mensagem: String? = null,
    val usuario: UsuarioInfo? = null,
    val erro: String? = null
)

// ── Orçamento ──────────────────────────────────────────────────────────────

data class OrcamentoRequest(
    val valor: Double,
    val periodo: String
)

data class RespostaOrcamento(
    val sucesso: Boolean,
    val orcamento: OrcamentoData? = null,
    val erro: String? = null
)

data class OrcamentoData(
    val id: Int? = null,
    val valor: Double,
    val periodo: String? = null
)

// ── Listas ─────────────────────────────────────────────────────────────────

data class ItemLista(
    val id: Int,
    @SerializedName("quantidade_gramas") val quantidadeGramas: Double,
    @SerializedName("preco_calculado") val precoCalculado: Double,
    val alimentos: AlimentoSimples? = null
)

data class AlimentoSimples(
    @SerializedName("nome_descricao") val nome: String
)

data class ResumoLista(
    @SerializedName("id_lista") val idLista: Int,
    val titulo: String,
    val tipo: String,
    @SerializedName("total_itens") val totalItens: Int,
    @SerializedName("valor_total_calculado") val valorTotal: Double,
    val itens: List<ItemLista>
)

data class RespostaLista(
    val sucesso: Boolean,
    val resumo: ResumoLista? = null,
    val erro: String? = null
)

// ── Cardápio ───────────────────────────────────────────────────────────────

data class AlimentoDetalhado(
    @SerializedName("nome_descricao") val nome: String,
    val calorias: Double? = null,
    val proteinas: Double? = null
)

data class ItemListaCardapio(
    val id: Int,
    @SerializedName("quantidade_gramas") val quantidadeGramas: Double,
    @SerializedName("preco_calculado") val precoCalculado: Double,
    val alimentos: AlimentoDetalhado? = null
)

data class CardapioItemData(
    val id: Int,
    @SerializedName("titulo_lista") val tituloLista: String,
    val tipo: String,
    @SerializedName("criado_em") val criadoEm: String? = null,
    @SerializedName("lista_itens") val listaItens: List<ItemListaCardapio> = emptyList()
)

data class RespostaCardapio(
    val sucesso: Boolean,
    val dados: List<CardapioItemData> = emptyList(),
    val erro: String? = null
)

data class CriarListaRequest(
    val titulo_lista: String,
    val tipo: String = "cardapio"
)

data class RespostaCriarLista(
    val sucesso: Boolean,
    val mensagem: String? = null,
    val lista: CardapioItemData? = null,
    val erro: String? = null
)

data class AdicionarItemRequest(
    val alimento_id: Int,
    val quantidade_gramas: Double
)

data class RespostaAdicionarItem(
    val sucesso: Boolean,
    val mensagem: String? = null,
    val erro: String? = null
)