package com.example.pratocerto.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.pratocerto.ui.theme.PratoCertoColors
import com.example.pratocerto.viewmodel.AuthViewModel

@Composable
fun TelaCadastroLogin(
    onLoginSucesso: () -> Unit,
    viewModel: AuthViewModel = viewModel()
) {
    var abaCadastro by remember { mutableStateOf(false) }
    var emailLogin by remember { mutableStateOf("") }
    var senhaLogin by remember { mutableStateOf("") }
    var nomeCadastro by remember { mutableStateOf("") }
    var emailCadastro by remember { mutableStateOf("") }
    var senhaCadastro by remember { mutableStateOf("") }
    var mensagemSucesso by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(viewModel.loginSucesso) {
        if (viewModel.loginSucesso) {
            viewModel.resetLoginSucesso()
            onLoginSucesso()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(PratoCertoColors.GradientTop, Color.White, Color.White, PratoCertoColors.GradientBot)
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center
        ) {
            // Logo
            Column(
                modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("🥗", fontSize = 64.sp)
                Text("Prato Certo", fontSize = 32.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF1A1A1A))
                Text("Comer bem com o que você tem.", fontSize = 12.sp, color = PratoCertoColors.TextGray)
            }

            if (!abaCadastro) {
                // LOGIN
                Label("E-MAIL")
                PCInput(valor = emailLogin, onValorChange = { emailLogin = it }, placeholder = "exemplo@email.com")
                Label("SENHA")
                PCInput(valor = senhaLogin, onValorChange = { senhaLogin = it }, placeholder = "******", senha = true)
                Spacer(Modifier.height(8.dp))
                BtnDark(if (viewModel.carregando) "Entrando…" else "Entrar", !viewModel.carregando) {
                    viewModel.login(emailLogin, senhaLogin)
                }
                BtnOutline("Criar conta") { abaCadastro = true; viewModel.limparErro(); mensagemSucesso = null }
            } else {
                // CADASTRO
                Text("Cadastro", fontSize = 28.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF1A1A1A), modifier = Modifier.padding(bottom = 16.dp))
                Label("NOME")
                PCInput(valor = nomeCadastro, onValorChange = { nomeCadastro = it }, placeholder = "Seu nome")
                Label("E-MAIL")
                PCInput(valor = emailCadastro, onValorChange = { emailCadastro = it }, placeholder = "email@exemplo.com", tipo = KeyboardType.Email)
                Label("SENHA")
                PCInput(valor = senhaCadastro, onValorChange = { senhaCadastro = it }, placeholder = "******", senha = true)
                Spacer(Modifier.height(8.dp))
                BtnDark(if (viewModel.carregando) "Criando…" else "Finalizar Cadastro", !viewModel.carregando) {
                    viewModel.registrar(nomeCadastro, emailCadastro, senhaCadastro) {
                        mensagemSucesso = "Conta criada! Faça login."
                        abaCadastro = false
                    }
                }
                BtnOutline("Voltar ao login") { abaCadastro = false; viewModel.limparErro() }
            }

            viewModel.erro?.let { Text(it, color = Color.Red, fontSize = 13.sp, modifier = Modifier.padding(top = 8.dp)) }
            mensagemSucesso?.let { Text(it, color = PratoCertoColors.TextGreen, fontSize = 13.sp, modifier = Modifier.padding(top = 8.dp)) }
        }
    }
}

@Composable
fun Label(text: String) {
    Text(text, fontSize = 11.sp, color = Color(0xFF888888), fontWeight = FontWeight.SemiBold,
        letterSpacing = 1.sp, modifier = Modifier.padding(start = 4.dp, bottom = 6.dp))
}

@Composable
fun PCInput(
    valor: String,
    onValorChange: (String) -> Unit,
    placeholder: String,
    tipo: KeyboardType = KeyboardType.Text,
    senha: Boolean = false
) {
    TextField(
        value = valor,
        onValueChange = onValorChange,
        placeholder = { Text(placeholder, color = Color(0xFF666666)) },
        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = PratoCertoColors.InputBg,
            unfocusedContainerColor = PratoCertoColors.InputBg,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            focusedTextColor = Color(0xFF333333),
            unfocusedTextColor = Color(0xFF333333)
        ),
        keyboardOptions = KeyboardOptions(keyboardType = tipo),
        visualTransformation = if (senha) PasswordVisualTransformation() else VisualTransformation.None,
        singleLine = true
    )
}

@Composable
fun BtnDark(texto: String, habilitado: Boolean = true, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        enabled = habilitado,
        modifier = Modifier.fillMaxWidth().height(54.dp).padding(top = 8.dp),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF222222))
    ) {
        Text(texto, fontWeight = FontWeight.Bold, fontSize = 16.sp)
    }
}

@Composable
fun BtnOutline(texto: String, onClick: () -> Unit) {
    OutlinedButton(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().height(54.dp).padding(top = 8.dp),
        shape = RoundedCornerShape(12.dp),
        border = ButtonDefaults.outlinedButtonBorder.copy(width = 2.dp),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = PratoCertoColors.TextGreen)
    ) {
        Text(texto, fontWeight = FontWeight.Bold, fontSize = 15.sp)
    }
}
