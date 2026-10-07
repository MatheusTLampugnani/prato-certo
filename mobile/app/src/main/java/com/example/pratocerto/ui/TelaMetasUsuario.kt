package com.example.pratocerto.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingFlat
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.pratocerto.model.MetaData
import com.example.pratocerto.ui.theme.PratoCertoColors
import com.example.pratocerto.viewmodel.MetasViewModel

data class OpcaoObjetivo(val id: String, val icone: ImageVector, val label: String)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TelaMetasUsuario(
    onVoltar: () -> Unit,
    viewModel: MetasViewModel = viewModel()
) {
    var pesoAtual by remember { mutableStateOf("") }
    var pesoMeta by remember { mutableStateOf("") }
    var altura by remember { mutableStateOf("") }
    var objetivoSelecionado by remember { mutableStateOf("manter") }
    var isEditing by remember { mutableStateOf(false) }

    val objetivos = listOf(
        OpcaoObjetivo("emagrecer", Icons.AutoMirrored.Filled.TrendingDown, "Emagrecer"),
        OpcaoObjetivo("manter", Icons.AutoMirrored.Filled.TrendingFlat, "Manter peso"),
        OpcaoObjetivo("engordar", Icons.AutoMirrored.Filled.TrendingUp, "Ganhar massa")
    )

    LaunchedEffect(Unit) { viewModel.carregarMeta() }

    LaunchedEffect(viewModel.salvoComSucesso) {
        if (viewModel.salvoComSucesso) {
            viewModel.resetSucesso()
        }
    }

    Column(modifier = Modifier.fillMaxSize().background(PratoCertoColors.Background)) {
        TopAppBar(
            title = { Text("As minhas metas", fontWeight = FontWeight.SemiBold) },
            navigationIcon = {
                IconButton(onClick = onVoltar) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar") }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
        )

        Column(
            modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            viewModel.meta?.let {
                ResultadoMacros(it)
                AlimentosRecomendados(it.objetivo, viewModel.sugestoes)
            }

            if (viewModel.meta == null || isEditing) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Calcular os meus macros", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF222222))
                    Text("Informe os seus dados para o cálculo exato", fontSize = 12.sp, color = PratoCertoColors.TextGray, modifier = Modifier.padding(bottom = 16.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Peso atual (kg)", fontSize = 13.sp, color = PratoCertoColors.TextGray)
                            OutlinedTextField(
                                value = pesoAtual,
                                onValueChange = { pesoAtual = it.filter { c -> c.isDigit() || c == '.' } },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Altura (cm)", fontSize = 13.sp, color = PratoCertoColors.TextGray)
                            OutlinedTextField(
                                value = altura,
                                onValueChange = { altura = it.filter { c -> c.isDigit() } },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }

                    Spacer(Modifier.height(12.dp))
                    Text("Peso meta (kg)", fontSize = 13.sp, color = PratoCertoColors.TextGray)
                    OutlinedTextField(
                        value = pesoMeta,
                        onValueChange = { pesoMeta = it.filter { c -> c.isDigit() || c == '.' } },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(Modifier.height(16.dp))
                    Text("Objetivo", fontSize = 13.sp, color = PratoCertoColors.TextGray)
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        objetivos.forEach { objetivo ->
                            val selecionado = objetivoSelecionado == objetivo.id
                            OutlinedButton(
                                onClick = { objetivoSelecionado = objetivo.id },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    containerColor = if (selecionado) PratoCertoColors.Green else Color.Transparent,
                                    contentColor = if (selecionado) Color.White else PratoCertoColors.TextGray
                                )
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = objetivo.icone,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Text(objetivo.label, fontWeight = if (selecionado) FontWeight.Bold else FontWeight.Normal)
                                }
                            }
                        }
                    }
                }
            }

            Button(
                onClick = {
                    val pa = pesoAtual.toDoubleOrNull()
                    val pm = pesoMeta.toDoubleOrNull()
                    val alt = altura.toDoubleOrNull() ?: 0.0
                    if (pa != null && alt > 0) {
                        viewModel.salvar(pa, alt, objetivoSelecionado, pm)
                        isEditing = false
                    }
                },
                enabled = pesoAtual.isNotBlank() && altura.isNotBlank() && !viewModel.carregando,
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PratoCertoColors.Green)
            ) {
                Text(if (viewModel.carregando) "A calcular…" else "Calcular e salvar", fontWeight = FontWeight.SemiBold)
            }
            } else {
                Button(
                    onClick = {
                        pesoAtual = viewModel.meta!!.peso_atual.toString()
                        pesoMeta = viewModel.meta!!.peso_meta.toString()
                        altura = viewModel.meta!!.altura_cm?.toString() ?: ""
                        objetivoSelecionado = viewModel.meta!!.objetivo
                        isEditing = true
                    },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PratoCertoColors.Green)
                ) {
                    Text("Editar minhas metas", fontWeight = FontWeight.SemiBold)
                }
            }

            viewModel.erro?.let {
                Text(it, color = Color.Red, fontSize = 13.sp)
            }
        }
    }
}

@Composable
private fun AlimentosRecomendados(objetivo: String, sugestoes: List<com.example.pratocerto.model.Alimento>) {
    val titulo = when (objetivo) {
        "emagrecer" -> "Top Alimentos para Saciedade"
        "engordar" -> "Top Alimentos para Massa"
        else -> "Bons Alimentos para Manutenção"
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Star, contentDescription = null, tint = PratoCertoColors.Green, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text(titulo, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color(0xFF222222))
            }
            Spacer(Modifier.height(8.dp))
            if (sugestoes.isEmpty()) {
                Text("Nenhuma sugestão encontrada.", fontSize = 13.sp, color = Color.Gray)
            } else {
                sugestoes.forEach { alimento ->
                    val caloriasStr = "%.0f".format(alimento.calorias)
                    val protStr = "%.0f".format(alimento.proteinas)
                    Text("• ${alimento.nome} ($caloriasStr kcal / ${protStr}g prot)", fontSize = 13.sp, color = Color.DarkGray, modifier = Modifier.padding(vertical = 2.dp))
                }
            }
        }
    }
}

@Composable
private fun ResultadoMacros(meta: MetaData) {
    val cal = (meta.calorias_dia as? Int) ?: 0
    val prot = (meta.proteinas_dia as? Int) ?: 0
    val carb = (meta.carboidratos_dia as? Int) ?: 0
    val gord = (meta.gorduras_dia as? Int) ?: 0
    val pAtual = (meta.peso_atual as? Double) ?: 0.0
    val pMeta = (meta.peso_meta as? Double) ?: 0.0
    val obj = meta.objetivo as? String

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = PratoCertoColors.Green)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("As suas metas diárias", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color.White)
            Text(
                "Objetivo: ${
                    when (obj) {
                        "emagrecer" -> "Emagrecer (${pAtual}kg → ${pMeta}kg)"
                        "engordar"  -> "Ganhar massa (${pAtual}kg → ${pMeta}kg)"
                        else        -> "Manter peso (${pAtual}kg)"
                    }
                }",
                fontSize = 12.sp,
                color = Color.White.copy(0.85f),
                modifier = Modifier.padding(top = 2.dp, bottom = 14.dp)
            )

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                MacroChip(Icons.Default.LocalFireDepartment, "Calorias", "$cal kcal", Modifier.weight(1f))
                MacroChip(Icons.Default.SetMeal, "Proteína", "${prot}g", Modifier.weight(1f))
            }
            Spacer(Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                MacroChip(Icons.Default.BakeryDining, "Carboidrato", "${carb}g", Modifier.weight(1f))
                MacroChip(Icons.Default.WaterDrop, "Gordura", "${gord}g", Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun MacroChip(icone: ImageVector, label: String, valor: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .background(Color.White.copy(alpha = 0.15f), RoundedCornerShape(10.dp))
            .padding(10.dp)
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icone, contentDescription = null, tint = Color.White.copy(0.8f), modifier = Modifier.size(14.dp))
                Spacer(Modifier.width(4.dp))
                Text(label, fontSize = 11.sp, color = Color.White.copy(0.8f))
            }
            Text(valor, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
        }
    }
}