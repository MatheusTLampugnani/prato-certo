package com.example.pratocerto.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
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

    // Campos login
    var emailLogin by remember { mutableStateOf("") }
    var senhaLogin by remember { mutableStateOf("") }

    // Campos cadastro
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PratoCertoColors.Background)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(40.dp))

        // Logo / título
        Text(
            "🥗 Prato Certo",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = PratoCertoColors.Green
        )
        Text(
            "Comer bem com o que você tem.",
            fontSize = 14.sp,
            color = PratoCertoColors.TextGray,
            modifier = Modifier.padding(top = 4.dp, bottom = 32.dp)
        )

        // Abas
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFEEEEEE), RoundedCornerShape(12.dp))
                .padding(4.dp)
        ) {
            listOf("Entrar" to false, "Criar conta" to true).forEach { (label, isCadastro) ->
                Button(
                    onClick = {
                        abaCadastro = isCadastro
                        viewModel.limparErro()
                        mensagemSucesso = null
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (abaCadastro == isCadastro) PratoCertoColors.Green else Color.Transparent,
                        contentColor = if (abaCadastro == isCadastro) Color.White else PratoCertoColors.TextGray
                    ),
                    elevation = ButtonDefaults.buttonElevation(0.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(label, fontWeight = FontWeight.SemiBold)
                }
            }
        }

        Spacer(Modifier.height(24.dp))

        if (!abaCadastro) {
            // ── LOGIN ──────────────────────────────────────────────────────────
            CampoTexto(
                valor = emailLogin,
                onValorChange = { emailLogin = it },
                label = "E-mail",
                icone = { Icon(Icons.Default.Email, null, tint = PratoCertoColors.TextGray) },
                tipo = KeyboardType.Email
            )
            Spacer(Modifier.height(12.dp))
            CampoTexto(
                valor = senhaLogin,
                onValorChange = { senhaLogin = it },
                label = "Senha",
                icone = { Icon(Icons.Default.Lock, null, tint = PratoCertoColors.TextGray) },
                tipo = KeyboardType.Password,
                senha = true
            )
            Spacer(Modifier.height(20.dp))
            BotaoPrincipal(
                texto = if (viewModel.carregando) "Entrando…" else "Entrar",
                habilitado = !viewModel.carregando
            ) {
                viewModel.login(emailLogin, senhaLogin)
            }
        } else {
            // ── CADASTRO ───────────────────────────────────────────────────────
            CampoTexto(
                valor = nomeCadastro,
                onValorChange = { nomeCadastro = it },
                label = "Nome completo",
                icone = { Icon(Icons.Default.Person, null, tint = PratoCertoColors.TextGray) }
            )
            Spacer(Modifier.height(12.dp))
            CampoTexto(
                valor = emailCadastro,
                onValorChange = { emailCadastro = it },
                label = "E-mail",
                icone = { Icon(Icons.Default.Email, null, tint = PratoCertoColors.TextGray) },
                tipo = KeyboardType.Email
            )
            Spacer(Modifier.height(12.dp))
            CampoTexto(
                valor = senhaCadastro,
                onValorChange = { senhaCadastro = it },
                label = "Senha",
                icone = { Icon(Icons.Default.Lock, null, tint = PratoCertoColors.TextGray) },
                tipo = KeyboardType.Password,
                senha = true
            )
            Spacer(Modifier.height(20.dp))
            BotaoPrincipal(
                texto = if (viewModel.carregando) "Criando conta…" else "Criar conta",
                habilitado = !viewModel.carregando
            ) {
                viewModel.registrar(nomeCadastro, emailCadastro, senhaCadastro) {
                    mensagemSucesso = "Conta criada! Faça login."
                    abaCadastro = false
                }
            }
        }

        // Feedback
        viewModel.erro?.let {
            Spacer(Modifier.height(12.dp))
            Text(it, color = Color.Red, fontSize = 13.sp)
        }
        mensagemSucesso?.let {
            Spacer(Modifier.height(12.dp))
            Text(it, color = PratoCertoColors.Green, fontSize = 13.sp)
        }
    }
}

// ── Componentes reutilizáveis ──────────────────────────────────────────────

@Composable
private fun CampoTexto(
    valor: String,
    onValorChange: (String) -> Unit,
    label: String,
    icone: @Composable () -> Unit,
    tipo: KeyboardType = KeyboardType.Text,
    senha: Boolean = false
) {
    OutlinedTextField(
        value = valor,
        onValueChange = onValorChange,
        label = { Text(label) },
        leadingIcon = icone,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        keyboardOptions = KeyboardOptions(keyboardType = tipo),
        visualTransformation = if (senha) PasswordVisualTransformation() else androidx.compose.ui.text.input.VisualTransformation.None,
        singleLine = true,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = PratoCertoColors.Green,
            focusedLabelColor = PratoCertoColors.Green
        )
    )
}

@Composable
private fun BotaoPrincipal(
    texto: String,
    habilitado: Boolean = true,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        enabled = habilitado,
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(containerColor = PratoCertoColors.Green)
    ) {
        Text(texto, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
    }
}
