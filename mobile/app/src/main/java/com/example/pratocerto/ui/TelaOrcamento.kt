package com.example.pratocerto.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Wallet
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.pratocerto.ui.theme.PratoCertoColors
import com.example.pratocerto.viewmodel.OrcamentoViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TelaOrcamento(
    onVoltar: () -> Unit,
    viewModel: OrcamentoViewModel = viewModel()
) {
    var valorTexto by remember { mutableStateOf("") }
    var periodoSelecionado by remember { mutableStateOf("Semanal") }
    val periodos = listOf("Semanal", "Quinzenal", "Mensal")

    // Observa o sucesso para voltar à tela principal
    LaunchedEffect(viewModel.sucesso) {
        if (viewModel.sucesso) {
            viewModel.resetStatus()
            onVoltar()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PratoCertoColors.Background)
    ) {
        TopAppBar(
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Wallet, contentDescription = null, modifier = Modifier.size(24.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Orçamento", fontWeight = FontWeight.SemiBold)
                }
            },
            navigationIcon = {
                IconButton(onClick = onVoltar) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
        )

        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                "Defina seu limite de gastos para que possamos sugerir o cardápio ideal.",
                fontSize = 13.sp,
                color = PratoCertoColors.TextGray,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            OutlinedTextField(
                value = valorTexto,
                onValueChange = { valorTexto = it },
                label = { Text("Valor disponível (R$)") },
                placeholder = { Text("Ex: 250.00") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PratoCertoColors.PrimaryGreen,
                    focusedLabelColor = PratoCertoColors.PrimaryGreen
                )
            )

            Spacer(Modifier.height(16.dp))
            Text(
                "Período",
                fontSize = 11.sp,
                color = PratoCertoColors.TextGray,
                modifier = Modifier.padding(start = 4.dp, bottom = 6.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                periodos.forEach { periodo ->
                    val selecionado = periodo == periodoSelecionado
                    FilterChip(
                        selected = selecionado,
                        onClick = { periodoSelecionado = periodo },
                        label = { Text(periodo) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = PratoCertoColors.PrimaryGreen,
                            selectedLabelColor = Color.White
                        ),
                        shape = RoundedCornerShape(20.dp)
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            if (viewModel.erro != null) {
                Text(
                    viewModel.erro!!,
                    color = Color.Red,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            Button(
                onClick = {
                    // Substitui vírgula por ponto para evitar erro no parsing do Double
                    val valorDouble = valorTexto.replace(",", ".").toDoubleOrNull()
                    if (valorDouble != null) {
                        viewModel.salvarOrcamento(valorDouble, periodoSelecionado)
                    } else {
                        // Pode adicionar um feedback visual se o usuário não digitar um número
                    }
                },
                enabled = valorTexto.isNotBlank() && !viewModel.carregando,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PratoCertoColors.DarkButton)
            ) {
                if (viewModel.carregando) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Text("Salvar Orçamento", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}