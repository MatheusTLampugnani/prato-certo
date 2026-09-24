package com.example.pratocerto.network

import com.example.pratocerto.model.ItemCompra
import retrofit2.http.GET
import retrofit2.http.Query

data class RespostaAlimentos(
    val sucesso: Boolean,
    val total: Int,
    val dados: List<ItemCompra>
)

interface ListaComprasApiService {

    @GET("api/alimentos")
    suspend fun listarItens(
        @Query("pesquisa") pesquisa: String = "a"
    ): RespostaAlimentos
}