package com.example.pratocerto.network

import com.example.pratocerto.model.*
import retrofit2.http.*

interface ApiService {

    // ── Alimentos ────────────────────────────────────────────────────────────
    @GET("api/alimentos")
    suspend fun buscarAlimentos(
        @Query("pesquisa") pesquisa: String
    ): RespostaAlimentos

    // ── Auth ─────────────────────────────────────────────────────────────────
    @POST("api/auth/registrar")
    suspend fun registrar(@Body body: UsuarioRegistro): RespostaRegistro

    @POST("api/auth/login")
    suspend fun login(@Body body: UsuarioLogin): RespostaLogin

    // ── Orçamento ────────────────────────────────────────────────────────────
    @POST("api/orcamento")
    suspend fun salvarOrcamento(
        @Header("Authorization") token: String,
        @Body body: OrcamentoRequest
    ): RespostaOrcamento

    // ── Lista ────────────────────────────────────────────────────────────────
    @GET("api/listas/{id}")
    suspend fun buscarLista(
        @Header("Authorization") token: String,
        @Path("id") id: Int
    ): RespostaLista
}
