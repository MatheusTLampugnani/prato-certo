package com.example.pratocerto.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.AttachMoney
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
    var valor by remember { mutableStateOf("") }
    var periodoSelecionado by remember { mutableStateOf("semanal") }
    val periodos = listOf("diario" to "Diário", "semanal" to "Semanal", "quinzenal" to "Quinzenal", "mensal" to "Mensal")

    LaunchedEffect(viewModel.salvoComSucesso) {
        if (viewModel.salvoComSucesso) {
            viewModel.resetSucesso()
            onVoltar()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PratoCertoColors.Background)
    ) {
        // Topbar
        TopAppBar(
            title = { Text("Definir orçamento", fontWeight = FontWeight.SemiBold) },
            navigationIcon = {
                IconButton(onClick = onVoltar) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Voltar")
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color.White
            )
        )

        Column(modifier = Modifier.padding(20.dp)) {

            // Card verde com ícone
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = PratoCertoColors.Green)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.AttachMoney, null, tint = Color.White, modifier = Modifier.size(32.dp))
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text("Configure seu orçamento", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 15.sp)
                        Text("Defina quanto você pode gastar com alimentação", color = Color.White.copy(0.85f), fontSize = 12.sp)
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            Text("Valor disponível (R\$)", fontSize = 13.sp, color = PratoCertoColors.TextGray, fontWeight = FontWeight.Medium)
            Spacer(Modifier.height(6.dp))
            OutlinedTextField(
                value = valor,
                onValueChange = { valor = it.filter { c -> c.isDigit() || c == '.' } },
                placeholder = { Text("Ex: 250.00") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PratoCertoColors.Green,
                    focusedLabelColor = PratoCertoColors.Green
                )
            )

            Spacer(Modifier.height(20.dp))

            Text("Período", fontSize = 13.sp, color = PratoCertoColors.TextGray, fontWeight = FontWeight.Medium)
            Spacer(Modifier.height(8.dp))

            // Chips de período
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                periodos.forEach { (valor, label) ->
                    FilterChip(
                        selected = periodoSelecionado == valor,
                        onClick = { periodoSelecionado = valor },
                        label = { Text(label, fontSize = 13.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = PratoCertoColors.Green,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }

            Spacer(Modifier.height(32.dp))

            Button(
                onClick = {
                    val v = valor.toDoubleOrNull()
                    if (v != null && v > 0) viewModel.salvar(v, periodoSelecionado)
                },
                enabled = !viewModel.carregando && valor.isNotBlank(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PratoCertoColors.Green)
            ) {
                Text(
                    if (viewModel.carregando) "Salvando…" else "Salvar orçamento",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp
                )
            }

            viewModel.erro?.let {
                Spacer(Modifier.height(12.dp))
                Text(it, color = Color.Red, fontSize = 13.sp)
            }
        }
    }
}
