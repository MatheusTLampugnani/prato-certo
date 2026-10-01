package com.example.pratocerto.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.pratocerto.model.Alimento
import com.example.pratocerto.model.MetaData
import com.example.pratocerto.ui.theme.PratoCertoColors
import com.example.pratocerto.viewmodel.AlimentosViewModel
import kotlin.math.min

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TelaPesquisaAlimentos(
    onVoltar: () -> Unit,
    navContent: @Composable () -> Unit = {},
    viewModel: AlimentosViewModel = viewModel()
) {
    val teclado = LocalSoftwareKeyboardController.current

    Box(modifier = Modifier.fillMaxSize().background(PratoCertoColors.Background)) {
        Column(modifier = Modifier.fillMaxSize().padding(bottom = 100.dp)) {

            // Header
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 24.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(onClick = onVoltar) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar", tint = Color(0xFF333333))
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Kitchen, contentDescription = null, tint = Color(0xFF333333), modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Base TACO", fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                }
                IconButton(onClick = { /* Ação de escanear */ }) {
                    Icon(Icons.Default.CameraAlt, contentDescription = "Escanear", tint = Color(0xFF333333))
                }
            }

            // Campo de busca
            Box(modifier = Modifier.padding(horizontal = 20.dp, vertical = 0.dp)) {
                TextField(
                    value = viewModel.termoPesquisa,
                    onValueChange = { viewModel.termoPesquisa = it },
                    placeholder = { Text("Buscar alimento...", color = Color(0xFF666666)) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = PratoCertoColors.InputBg,
                        unfocusedContainerColor = PratoCertoColors.InputBg,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    ),
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color(0xFF666666)) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(onSearch = {
                        viewModel.buscar(viewModel.termoPesquisa); teclado?.hide()
                    })
                )
            }

            Spacer(Modifier.height(8.dp))

            Button(
                onClick = { viewModel.buscar(viewModel.termoPesquisa); teclado?.hide() },
                enabled = viewModel.termoPesquisa.isNotBlank() && !viewModel.carregando,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp).height(48.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF222222))
            ) {
                Text(if (viewModel.carregando) "Buscando…" else "Buscar", fontWeight = FontWeight.Bold, fontSize = 15.sp)
            }

            Spacer(Modifier.height(8.dp))

            when {
                viewModel.carregando -> {
                    Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = PratoCertoColors.Green)
                    }
                }
                viewModel.erro != null -> {
                    Text(viewModel.erro ?: "", color = Color.Red, modifier = Modifier.padding(20.dp), fontSize = 13.sp)
                }
                viewModel.alimentos.isEmpty() && viewModel.termoPesquisa.isNotBlank() -> {
                    Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                        Text("Nenhum alimento encontrado.", color = PratoCertoColors.TextGray)
                    }
                }
                else -> {
                    LazyColumn {
                        items(viewModel.alimentos) { alimento ->
                            ItemAlimento(alimento, viewModel.meta)
                            HorizontalDivider(color = PratoCertoColors.Divider)
                        }
                    }
                }
            }
        }

        Box(modifier = Modifier.align(Alignment.BottomCenter)) { navContent() }
    }
}

@Composable
private fun ItemAlimento(alimento: Alimento, meta: MetaData?) {
    Column(
        modifier = Modifier.fillMaxWidth().background(Color.White).padding(horizontal = 20.dp, vertical = 12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.size(44.dp).clip(RoundedCornerShape(12.dp)).background(PratoCertoColors.IconBoxBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Eco, contentDescription = null, tint = PratoCertoColors.TextGreen, modifier = Modifier.size(22.dp))
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(alimento.nome, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = Color(0xFF333333))
                Text("Fonte: TACO  •  ${alimento.calorias.toInt()} kcal  •  ${alimento.proteinas}g prot",
                    fontSize = 11.sp, color = PratoCertoColors.TextGray)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text("R$ ${"%.2f".format(alimento.preco)}", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = PratoCertoColors.TextGreen)
                Text("/kg (CONAB)", fontSize = 10.sp, color = PratoCertoColors.TextGray)
            }
        }

        if (meta != null) {
            Spacer(Modifier.height(10.dp))
            MacroBar(Icons.Default.LocalFireDepartment, "Cal", alimento.calorias, meta.calorias_dia.toDouble(), "kcal", Color(0xFFFF7043))
            Spacer(Modifier.height(5.dp))
            MacroBar(Icons.Default.SetMeal, "Prot", alimento.proteinas, meta.proteinas_dia.toDouble(), "g", Color(0xFF42A5F5))
            Spacer(Modifier.height(5.dp))
            MacroBar(Icons.Default.BakeryDining, "Carb", alimento.carboidratos ?: 0.0, meta.carboidratos_dia.toDouble(), "g", Color(0xFFFFCA28))
            Spacer(Modifier.height(5.dp))
            MacroBar(Icons.Default.WaterDrop, "Gord", alimento.gorduras ?: 0.0, meta.gorduras_dia.toDouble(), "g", Color(0xFF66BB6A))
        } else {
            Spacer(Modifier.height(6.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Warning, contentDescription = null, tint = PratoCertoColors.TextGray, modifier = Modifier.size(14.dp))
                Spacer(Modifier.width(4.dp))
                Text("Cadastre suas metas para ver o % da necessidade diária", fontSize = 11.sp, color = PratoCertoColors.TextGray)
            }
        }
    }
}

@Composable
private fun MacroBar(icone: ImageVector, label: String, valor: Double, meta: Double, unidade: String, cor: Color) {
    val pct = if (meta > 0) min(valor / meta, 1.0).toFloat() else 0f
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.width(60.dp)) {
            Icon(icone, contentDescription = null, tint = PratoCertoColors.TextGray, modifier = Modifier.size(14.dp))
            Spacer(Modifier.width(4.dp))
            Text(label, fontSize = 11.sp, color = PratoCertoColors.TextGray)
        }
        Box(modifier = Modifier.weight(1f).height(5.dp).clip(RoundedCornerShape(50)).background(Color(0xFFEEEEEE))) {
            Box(modifier = Modifier.fillMaxWidth(pct).fillMaxHeight().background(cor, RoundedCornerShape(50)))
        }
        Text("${"%.1f".format(valor)}$unidade (${(pct * 100).toInt()}%)",
            fontSize = 11.sp, color = Color(0xFF333333), fontWeight = FontWeight.SemiBold, modifier = Modifier.width(90.dp))
    }
}