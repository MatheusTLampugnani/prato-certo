package com.example.pratocerto.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitInstance {

    // 10.0.2.2 é o endereço do seu computador dentro do emulador Android
    // Se for testar em celular físico, troca por: http://SEU_IP_LOCAL:3000/
    private const val BASE_URL = "http://10.0.2.2:3000/"

    val api: ListaComprasApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ListaComprasApiService::class.java)
    }
}