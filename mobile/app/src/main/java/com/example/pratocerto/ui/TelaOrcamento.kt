package com.example.pratocerto.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.draw.clip
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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

import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll

@Composable
fun TelaOrcamento(
    onVoltar: () -> Unit,
    viewModel: OrcamentoViewModel = viewModel()
) {
    var isEditing by remember { mutableStateOf(false) }
    
    // We pre-fill form when we enter edit mode
    var valor by remember { mutableStateOf("") }
    var periodoSelecionado by remember { mutableStateOf("semanal") }
    val periodos = listOf("semanal" to "Semanal", "quinzenal" to "Quinzenal", "mensal" to "Mensal")

    LaunchedEffect(viewModel.sucesso) {
        if (viewModel.sucesso) {
            viewModel.resetStatus()
            isEditing = false
        }
    }

    LaunchedEffect(viewModel.orcamento) {
        if (viewModel.orcamento != null) {
            valor = viewModel.orcamento!!.valor.toString()
            periodoSelecionado = viewModel.orcamento!!.periodo ?: "semanal"
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .imePadding()
            .padding(horizontal = 20.dp, vertical = 20.dp)
    ) {
        IconButton(onClick = onVoltar, modifier = Modifier.size(44.dp)) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Voltar",
                tint = Color(0xFF222222)
            )
        }

        Spacer(Modifier.height(12.dp))
        Text("Orçamento", fontSize = 28.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF1A1A1A))


        if (viewModel.carregando) {
            Box(Modifier.fillMaxWidth().padding(top=32.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = PratoCertoColors.Green)
            }
        } else if (viewModel.orcamento != null && !isEditing) {
            val orc = viewModel.orcamento!!
            val valorTotal = orc.valor
            val pNorm = (orc.periodo ?: "semanal").lowercase()
            val diasAtuais = when (pNorm) {
                "semanal" -> 7
                "quinzenal" -> 15
                else -> 30
            }
            val valorDiario = valorTotal / diasAtuais

            Text("Aqui está o limite de gastos que você definiu para suas compras.",
                fontSize = 13.sp, color = PratoCertoColors.TextGray, modifier = Modifier.padding(top = 8.dp, bottom = 20.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(PratoCertoColors.IconBoxBg)
                    .padding(20.dp)
            ) {
                Column {
                    Text("VALOR DISPONÍVEL", fontSize = 12.sp, color = PratoCertoColors.TextGray, fontWeight = FontWeight.Bold)
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text("R$ ${"%.2f".format(valorTotal)}", fontSize = 32.sp, fontWeight = FontWeight.ExtraBold, color = PratoCertoColors.Green)
                        val pLabel = when (pNorm) {
                            "semanal" -> " / semana"
                            "quinzenal" -> " / quinzena"
                            else -> " / mês"
                        }
                        Text(pLabel, fontSize = 14.sp, color = PratoCertoColors.TextGray, modifier = Modifier.padding(bottom = 5.dp, start = 4.dp))
                    }

                    Spacer(Modifier.height(16.dp))
                    Text("EQUIVALÊNCIA NOS 3 PERÍODOS", fontSize = 11.sp, color = PratoCertoColors.TextGreen, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(8.dp))

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.White)
                            .padding(12.dp)
                    ) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Diário (1 dia):", fontSize = 12.sp, color = Color.DarkGray)
                            Text("R$ ${"%.2f".format(valorDiario)} / dia", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = PratoCertoColors.TextGreen)
                        }
                        Spacer(Modifier.height(6.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Semanal (7 dias):", fontSize = 12.sp, color = Color.DarkGray)
                            Text("R$ ${"%.2f".format(valorDiario * 7)}", fontSize = 12.sp, fontWeight = if (pNorm == "semanal") FontWeight.ExtraBold else FontWeight.SemiBold, color = if (pNorm == "semanal") PratoCertoColors.Green else Color(0xFF333333))
                        }
                        Spacer(Modifier.height(6.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Quinzenal (15 dias):", fontSize = 12.sp, color = Color.DarkGray)
                            Text("R$ ${"%.2f".format(valorDiario * 15)}", fontSize = 12.sp, fontWeight = if (pNorm == "quinzenal") FontWeight.ExtraBold else FontWeight.SemiBold, color = if (pNorm == "quinzenal") PratoCertoColors.Green else Color(0xFF333333))
                        }
                        Spacer(Modifier.height(6.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Mensal (30 dias):", fontSize = 12.sp, color = Color.DarkGray)
                            Text("R$ ${"%.2f".format(valorDiario * 30)}", fontSize = 12.sp, fontWeight = if (pNorm == "mensal") FontWeight.ExtraBold else FontWeight.SemiBold, color = if (pNorm == "mensal") PratoCertoColors.Green else Color(0xFF333333))
                        }
                    }
                }
            }

            Spacer(Modifier.height(20.dp))
            BtnDark("Editar orçamento", true) {
                valor = viewModel.orcamento!!.valor.toString()
                periodoSelecionado = viewModel.orcamento!!.periodo ?: "semanal"
                isEditing = true
            }
        } else {
            Text("Defina seu limite de gastos para que possamos sugerir o cardápio ideal.",
                fontSize = 13.sp, color = PratoCertoColors.TextGray, modifier = Modifier.padding(top = 8.dp, bottom = 16.dp))

            Label("VALOR DISPONÍVEL (R\$)")
            PCInput(valor = valor, onValorChange = { valor = it.filter { c -> c.isDigit() || c == '.' } },
                placeholder = "400.00", tipo = KeyboardType.Decimal)

            Label("PERÍODO")
            Row(modifier = Modifier.padding(bottom = 12.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                periodos.forEach { (v, l) ->
                    FilterChip(
                        selected = periodoSelecionado.equals(v, ignoreCase = true),
                        onClick = { periodoSelecionado = v },
                        label = { Text(l, fontSize = 13.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = PratoCertoColors.Green,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }

            val valorNum = valor.toDoubleOrNull() ?: 0.0
            if (valorNum > 0) {
                val diasSel = when (periodoSelecionado.lowercase()) {
                    "semanal" -> 7
                    "quinzenal" -> 15
                    else -> 30
                }
                val diarioCalculado = valorNum / diasSel
                val periodoNomeSel = when (periodoSelecionado.lowercase()) {
                    "semanal" -> "Semana"
                    "quinzenal" -> "Quinzena"
                    else -> "Mês"
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(PratoCertoColors.IconBoxBg)
                        .padding(12.dp)
                ) {
                    Column {
                        Text(
                            "Projeção a partir do valor para $periodoNomeSel:",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = PratoCertoColors.TextGreen
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "• Diário: R$ ${"%.2f".format(diarioCalculado)} / dia",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF222222)
                        )
                        Text(
                            "• Semanal (7 dias): R$ ${"%.2f".format(diarioCalculado * 7)}",
                            fontSize = 11.sp,
                            color = Color.DarkGray
                        )
                        Text(
                            "• Quinzenal (15 dias): R$ ${"%.2f".format(diarioCalculado * 15)}",
                            fontSize = 11.sp,
                            color = Color.DarkGray
                        )
                        Text(
                            "• Mensal (30 dias): R$ ${"%.2f".format(diarioCalculado * 30)}",
                            fontSize = 11.sp,
                            color = Color.DarkGray
                        )
                    }
                }
            }

            BtnDark(if (viewModel.carregando) "Salvando…" else "Salvar Orçamento", valor.isNotBlank() && !viewModel.carregando) {
                valor.toDoubleOrNull()?.let { v -> viewModel.salvarOrcamento(v, periodoSelecionado.lowercase()) }
            }

            if (isEditing) {
                Spacer(Modifier.height(12.dp))
                OutlinedButton(
                    onClick = { isEditing = false },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    border = ButtonDefaults.outlinedButtonBorder.copy(width = 1.5.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF4B5563))
                ) {
                    Text("Cancelar edição", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                }
            }

            viewModel.erro?.let { Text(it, color = Color.Red, fontSize = 13.sp, modifier = Modifier.padding(top = 8.dp)) }
        }
    }
}