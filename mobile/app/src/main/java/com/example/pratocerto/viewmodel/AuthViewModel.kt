package com.example.pratocerto.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pratocerto.model.UsuarioLogin
import com.example.pratocerto.model.UsuarioRegistro
import com.example.pratocerto.network.RetrofitInstance
import com.example.pratocerto.util.SessionManager
import kotlinx.coroutines.launch

class AuthViewModel : ViewModel() {

    var carregando by mutableStateOf(false)
        private set

    var erro by mutableStateOf<String?>(null)
        private set

    var loginSucesso by mutableStateOf(false)
        private set

    fun login(email: String, senha: String) {
        viewModelScope.launch {
            carregando = true
            erro = null
            try {
                val resposta = RetrofitInstance.api.login(UsuarioLogin(email, senha))
                if (resposta.sucesso && resposta.token != null && resposta.usuario != null) {
                    SessionManager.salvar(
                        token = resposta.token,
                        nome = resposta.usuario.nome,
                        email = resposta.usuario.email
                    )
                    loginSucesso = true
                } else {
                    erro = resposta.erro ?: "Erro ao fazer login."
                }
            } catch (e: Exception) {
                erro = "Erro de conexão: ${e.message}"
            } finally {
                carregando = false
            }
        }
    }

    fun registrar(nome: String, email: String, senha: String, onSucesso: () -> Unit) {
        viewModelScope.launch {
            carregando = true
            erro = null
            try {
                val resposta = RetrofitInstance.api.registrar(UsuarioRegistro(nome, email, senha))
                if (resposta.sucesso) {
                    onSucesso()
                } else {
                    erro = resposta.erro ?: "Erro ao criar conta."
                }
            } catch (e: Exception) {
                erro = "Erro de conexão: ${e.message}"
            } finally {
                carregando = false
            }
        }
    }

    fun limparErro() { erro = null }
    fun resetLoginSucesso() { loginSucesso = false }
}
