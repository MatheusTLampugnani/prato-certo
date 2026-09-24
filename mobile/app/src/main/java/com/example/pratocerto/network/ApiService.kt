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

    @GET("api/orcamento")
    suspend fun buscarOrcamento(
        @Header("Authorization") token: String
    ): RespostaOrcamento

    // ── Cardápio / Listas ─────────────────────────────────────────────────────
    @GET("api/cardapio")
    suspend fun buscarCardapio(
        @Header("Authorization") token: String
    ): RespostaCardapio

    @POST("api/listas")
    suspend fun criarLista(
        @Header("Authorization") token: String,
        @Body body: CriarListaRequest
    ): RespostaCriarLista

    @POST("api/listas/{id}/itens")
    suspend fun adicionarItemLista(
        @Header("Authorization") token: String,
        @Path("id") idLista: Int,
        @Body body: AdicionarItemRequest
    ): RespostaAdicionarItem
}