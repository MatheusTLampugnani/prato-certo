package com.example.pratocerto.network

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitInstance {

    /**
     * DICA PARA TESTES COM O PROFESSOR:
     * - No Emulador Android Studio: mantenha "http://10.0.2.2:3000/"
     * - No Celular físico via USB: use "http://127.0.0.1:3000/" e rode no PC: `adb reverse tcp:3000 tcp:3000`
     * - No Celular físico via Wi-Fi: use o IP do PC na mesma rede (ex: "http://192.168.X.X:3000/")
     */
    var BASE_URL = "http://10.0.2.2:3000/"

    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    val api: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}