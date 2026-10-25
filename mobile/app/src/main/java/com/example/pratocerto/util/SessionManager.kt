package com.example.pratocerto.util

import android.content.Context
import android.content.SharedPreferences

object SessionManager {
    private var prefs: SharedPreferences? = null

    var token: String = ""
        private set
    var nomeUsuario: String = ""
        private set
    var emailUsuario: String = ""
        private set

    fun init(context: Context) {
        prefs = context.getSharedPreferences("prato_certo_prefs", Context.MODE_PRIVATE)
        token = prefs?.getString("token", "") ?: ""
        nomeUsuario = prefs?.getString("nome", "") ?: ""
        emailUsuario = prefs?.getString("email", "") ?: ""
    }

    fun bearer() = "Bearer $token"
    fun logado() = token.isNotEmpty()

    fun salvar(token: String, nome: String, email: String) {
        this.token = token
        this.nomeUsuario = nome
        this.emailUsuario = email
        prefs?.edit()
            ?.putString("token", token)
            ?.putString("nome", nome)
            ?.putString("email", email)
            ?.apply()
    }

    fun limpar() {
        token = ""
        nomeUsuario = ""
        emailUsuario = ""
        prefs?.edit()?.clear()?.apply()
    }
}
