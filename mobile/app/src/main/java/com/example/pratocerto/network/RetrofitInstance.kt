package com.example.pratocerto.network

import okhttp3.HttpUrl
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException
import java.util.concurrent.TimeUnit

object RetrofitInstance {
    // 10.0.2.2 é o IP padrão que o Emulador Android usa para acessar o localhost do PC
    var BASE_URL = "http://10.0.2.2:3000/"

    @Volatile
    private var activeHost: String? = null

    private val fallbackHosts = listOf("10.0.2.2", "127.0.0.1")

    private val fallbackInterceptor = Interceptor { chain ->
        val originalRequest = chain.request()
        val originalUrl = originalRequest.url()

        // Se já descobrimos um host funcional nesta sessão, usa ele direto
        val preferredHost = activeHost
        if (preferredHost != null && originalUrl.host() != preferredHost) {
            val newUrl = originalUrl.newBuilder().host(preferredHost).build()
            return@Interceptor chain.proceed(originalRequest.newBuilder().url(newUrl).build())
        }

        var lastException: IOException? = null
        val hostsToTry = if (preferredHost != null) {
            listOf(preferredHost) + fallbackHosts.filter { it != preferredHost }
        } else {
            val configuredHost = HttpUrl.parse(BASE_URL)?.host() ?: "10.0.2.2"
            listOf(configuredHost) + fallbackHosts.filter { it != configuredHost }
        }

        for (host in hostsToTry) {
            val tryUrl = originalUrl.newBuilder().host(host).build()
            val tryRequest = originalRequest.newBuilder().url(tryUrl).build()
            try {
                val response = chain.proceed(tryRequest)
                activeHost = host
                return@Interceptor response
            } catch (e: IOException) {
                lastException = e
                activeHost = null
            }
        }

        throw lastException ?: IOException("Não foi possível conectar ao servidor.")
    }

    private val client = OkHttpClient.Builder()
        .connectTimeout(5, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .writeTimeout(15, TimeUnit.SECONDS)
        .addInterceptor(fallbackInterceptor)
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
