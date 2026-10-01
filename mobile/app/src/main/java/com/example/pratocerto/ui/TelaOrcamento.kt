package com.example.pratocerto.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.pratocerto.ui.theme.PratoCertoColors
import com.example.pratocerto.viewmodel.OrcamentoViewModel

@Composable
fun TelaOrcamento(
    onVoltar: () -> Unit,
    viewModel: OrcamentoViewModel = viewModel()
) {
    var valor by remember { mutableStateOf("") }
    var periodoSelecionado by remember { mutableStateOf("semanal") }
    val periodos = listOf("semanal" to "Semanal", "quinzenal" to "Quinzenal", "mensal" to "Mensal")

    LaunchedEffect(viewModel.sucesso) {
        if (viewModel.sucesso) {
            viewModel.resetStatus()
            onVoltar()
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(24.dp)) {
        // Substituído o Text("←") pelo Icon
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = "Voltar",
            modifier = Modifier.clickable { onVoltar() }
        )

        Spacer(Modifier.height(16.dp))
        Text("Orçamento", fontSize = 28.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF1A1A1A))
        Text("Defina seu limite de gastos para que possamos sugerir o cardápio ideal.",
            fontSize = 13.sp, color = PratoCertoColors.TextGray, modifier = Modifier.padding(top = 8.dp, bottom = 16.dp))

        Label("VALOR DISPONÍVEL (R\$)")
        PCInput(valor = valor, onValorChange = { valor = it.filter { c -> c.isDigit() || c == '.' } },
            placeholder = "250,00", tipo = KeyboardType.Decimal)

        Label("PERÍODO")
        Row(modifier = Modifier.padding(bottom = 24.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            periodos.forEach { (v, l) ->
                FilterChip(
                    selected = periodoSelecionado == v,
                    onClick = { periodoSelecionado = v },
                    label = { Text(l, fontSize = 13.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = PratoCertoColors.Green,
                        selectedLabelColor = Color.White
                    )
                )
            }
        }

        BtnDark(if (viewModel.carregando) "Salvando…" else "Salvar Orçamento", valor.isNotBlank() && !viewModel.carregando) {
            valor.toDoubleOrNull()?.let { v -> viewModel.salvarOrcamento(v, periodoSelecionado) }
        }

        viewModel.erro?.let { Text(it, color = Color.Red, fontSize = 13.sp, modifier = Modifier.padding(top = 8.dp)) }
    }
}