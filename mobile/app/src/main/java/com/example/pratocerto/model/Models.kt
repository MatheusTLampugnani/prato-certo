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

data class OrcamentoData(
    val id: Int,
    val usuario_id: Int,
    val valor: Double,
    val periodo: String
)

data class RespostaOrcamento(
    val sucesso: Boolean,
    val mensagem: String? = null,
    val orcamento: OrcamentoData? = null,
    val erro: String? = null
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
