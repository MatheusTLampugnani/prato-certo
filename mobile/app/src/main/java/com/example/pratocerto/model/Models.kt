package com.example.pratocerto.model

import com.google.gson.annotations.SerializedName

// ── Alimentos ──────────────────────────────────────────────────────────────

data class Preco(
    @SerializedName("preco_medio") val precoMedio: Double = 0.0
)

data class Alimento(
    val id: Int = 0,
    @SerializedName("nome_descricao") val nome: String = "",
    val calorias: Double = 0.0,
    val proteinas: Double = 0.0,
    val carboidratos: Double? = null,
    val gorduras: Double? = null,
    val precos: List<Preco>? = null
) {
    val preco: Double get() = precos?.firstOrNull()?.precoMedio ?: 0.0
}

data class RespostaAlimentos(
    val sucesso: Boolean,
    val total: Int = 0,
    val dados: List<Alimento>? = null
)

// ── Auth ───────────────────────────────────────────────────────────────────

data class UsuarioRegistro(val nome: String, val email: String, val senha: String)
data class UsuarioLogin(val email: String, val senha: String)
data class UsuarioInfo(val id: Int, val nome: String, val email: String)

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

data class OrcamentoRequest(val valor: Double, val periodo: String)

data class OrcamentoData(
    val id: Int? = null,
    val valor: Double = 0.0,
    val periodo: String? = null
)

data class RespostaOrcamento(
    val sucesso: Boolean,
    val orcamento: OrcamentoData? = null,
    val erro: String? = null
)

// ── Listas / Cardápio ──────────────────────────────────────────────────────

data class AlimentoData(
    val id: Int? = null,
    @SerializedName("nome_descricao") val nome: String = "",
    val calorias: Double? = null,
    val proteinas: Double? = null,
    val carboidratos: Double? = null,
    val gorduras: Double? = null
)

data class ItemLista(
    val id: Int? = null,
    val alimentos: AlimentoData? = null,
    @SerializedName("quantidade_gramas") val quantidadeGramas: Double = 0.0,
    @SerializedName("preco_calculado") val precoCalculado: Double = 0.0
)

data class CardapioItemData(
    val id: Int = 0,
    val titulo_lista: String? = null,
    val tipo: String? = null,
    @SerializedName("lista_itens") val itens: List<ItemLista>? = null,
    val valorTotal: Double = 0.0,
    val totalItens: Int = 0
)

data class RespostaCardapio(
    val sucesso: Boolean,
    val dados: List<CardapioItemData>? = null,
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

// ── Metas ──────────────────────────────────────────────────────────────────

data class MetaRequest(
    val peso_atual: Double,
    val altura_cm: Double,
    val objetivo: String,
    val peso_meta: Double? = null
)

data class MetaData(
    val id: Int = 0,
    val usuario_id: Int = 0,
    val peso_atual: Double = 0.0,
    val altura_cm: Double? = null,
    val peso_meta: Double = 0.0,
    val objetivo: String = "",
    val calorias_dia: Int = 0,
    val proteinas_dia: Int = 0,
    val carboidratos_dia: Int = 0,
    val gorduras_dia: Int = 0
)

data class RespostaMeta(
    val sucesso: Boolean,
    val mensagem: String? = null,
    val meta: MetaData? = null,
    val sugestoes: List<Alimento>? = null,
    val erro: String? = null
)
