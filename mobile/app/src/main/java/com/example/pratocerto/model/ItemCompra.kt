package com.example.pratocerto.model

import com.google.gson.annotations.SerializedName

data class Preco(
    @SerializedName("preco_medio") val precoMedio: Double
)

data class ItemCompra(
    val id: Int,
    @SerializedName("nome_descricao") val nome: String,
    val calorias: Double,
    val proteinas: Double,
    val precos: List<Preco> = emptyList(),
    var comprado: Boolean = false
) {
    val preco: Double get() = precos.firstOrNull()?.precoMedio ?: 0.0
}