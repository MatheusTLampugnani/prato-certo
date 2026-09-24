package com.example.pratocerto.util

// Guarda o token JWT em memória enquanto o app está aberto.
// Para uma versão real, use EncryptedSharedPreferences.
object SessionManager {
    var token: String = ""
    var nomeUsuario: String = ""
    var emailUsuario: String = ""

    fun bearer() = "Bearer $token"
    fun logado() = token.isNotEmpty()

    fun salvar(token: String, nome: String, email: String) {
        this.token = token
        this.nomeUsuario = nome
        this.emailUsuario = email
    }

    fun limpar() {
        token = ""
        nomeUsuario = ""
        emailUsuario = ""
    }
}
