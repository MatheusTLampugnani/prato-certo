package com.example.pratocerto.network

import com.example.pratocerto.model.*
import retrofit2.http.*

interface ApiService {

    @GET("api/alimentos")
    suspend fun buscarAlimentos(@Query("pesquisa") pesquisa: String): RespostaAlimentos

    @POST("api/auth/registrar")
    suspend fun registrar(@Body body: UsuarioRegistro): RespostaRegistro

    @POST("api/auth/login")
    suspend fun login(@Body body: UsuarioLogin): RespostaLogin

    @POST("api/orcamento")
    suspend fun salvarOrcamento(
        @Header("Authorization") token: String,
        @Body body: OrcamentoRequest
    ): RespostaOrcamento

    @GET("api/orcamento")
    suspend fun buscarOrcamento(@Header("Authorization") token: String): RespostaOrcamento

    @GET("api/cardapio")
    suspend fun buscarCardapio(@Header("Authorization") token: String): RespostaCardapio

    @POST("api/cardapio/gerar")
    suspend fun gerarCardapioInteligente(@Header("Authorization") token: String): RespostaCriarLista

    @POST("api/cardapio/salvar-sugestao")
    suspend fun salvarSugestaoComoLista(@Header("Authorization") token: String): RespostaPadrao

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

    @DELETE("api/listas/{id}")
    suspend fun excluirLista(
        @Header("Authorization") token: String,
        @Path("id") idLista: Int
    ): RespostaPadrao

    @DELETE("api/listas/itens/{id}")
    suspend fun excluirItemLista(
        @Header("Authorization") token: String,
        @Path("id") idItem: Int
    ): RespostaPadrao

    @GET("api/produtos/barcode/{codigo}")
    suspend fun buscarPorCodigoBarras(@Path("codigo") codigo: String): RespostaBarcode

    @PATCH("api/listas/itens/{id}/toggle")
    suspend fun alternarItemComprado(
        @Header("Authorization") token: String,
        @Path("id") idItem: Int,
        @Body body: ToggleItemRequest
    ): RespostaPadrao

    @POST("api/metas")
    suspend fun salvarMeta(
        @Header("Authorization") token: String,
        @Body body: MetaRequest
    ): RespostaMeta

    @GET("api/metas")
    suspend fun buscarMeta(@Header("Authorization") token: String): RespostaMeta
}
