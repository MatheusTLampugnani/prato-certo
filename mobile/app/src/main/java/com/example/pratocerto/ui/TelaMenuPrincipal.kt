package com.example.pratocerto.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Wallet
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.pratocerto.ui.components.BottomNavFloating
import com.example.pratocerto.ui.theme.PratoCertoColors
import com.example.pratocerto.viewmodel.CardapioViewModel

@Composable
fun TelaMenuPrincipal(
    onIrCardapio: () -> Unit,
    onIrOrcamento: () -> Unit,
    onIrPesquisa: () -> Unit,
    onLogout: () -> Unit,
    onNavigate: (String) -> Unit,
    viewModel: CardapioViewModel = viewModel()
) {
    val lifecycleOwner = LocalLifecycleOwner.current

    // ── Atualizar sempre que o ecrã voltar ao foco (ON_RESUME) ───────────────
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.carregarOrcamento()
                viewModel.carregarCardapio()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    // ── Cálculos Dinâmicos ───────────────────────────────────────────────────
    val orcamentoDefinido = viewModel.orcamentoValor ?: 0.0
    val totalGasto = viewModel.listasCardapio.sumOf { cardapio ->
        cardapio.listaItens.sumOf { it.precoCalculado }
    }
    val saldoRestante = orcamentoDefinido - totalGasto
    val progressoOrcamento = if (orcamentoDefinido > 0.0) {
        (totalGasto / orcamentoDefinido).toFloat().coerceIn(0f, 1f)
    } else {
        0f
    }

    val totalItensCompras = viewModel.listasCardapio.sumOf { it.listaItens.size }

    // Cálculo de macros baseado na porção de gramas (assumindo que a base de dados TACO é por 100g)
    val totalCalorias = viewModel.listasCardapio.sumOf { cardapio ->
        cardapio.listaItens.sumOf { item ->
            val kcalPor100g = item.alimentos?.calorias ?: 0.0
            (kcalPor100g / 100.0) * item.quantidadeGramas
        }
    }

    val totalProteinas = viewModel.listasCardapio.sumOf { cardapio ->
        cardapio.listaItens.sumOf { item ->
            val protPor100g = item.alimentos?.proteinas ?: 0.0
            (protPor100g / 100.0) * item.quantidadeGramas
        }
    }

    Scaffold(
        bottomBar = {
            BottomNavFloating(
                telaAtual = "menu_principal",
                onNavigate = onNavigate
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(PratoCertoColors.Background)
        ) {
            // Header Top
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Menu,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp)
                )
                Text("Resumo Diário", fontWeight = FontWeight.SemiBold, fontSize = 18.sp)
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color.Gray)
                )
            }

            // Card 1: Orçamento Dinâmico
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 6.dp)
                    .clickable { onIrOrcamento() },
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .background(PratoCertoColors.IconBoxBg, RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Wallet, null, tint = PratoCertoColors.PrimaryGreen)
                    }
                    Spacer(Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Orçamento Guardado", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text(
                            "Disponível: R$ ${"%.2f".format(if (saldoRestante < 0) 0.0 else saldoRestante)} de R$ ${"%.2f".format(orcamentoDefinido)}",
                            fontSize = 11.sp,
                            color = if (saldoRestante < 0) Color.Red else PratoCertoColors.TextGray
                        )
                        Spacer(Modifier.height(8.dp))
                        LinearProgressIndicator(
                            progress = { progressoOrcamento },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = if (saldoRestante < 0) Color.Red else PratoCertoColors.PrimaryGreen,
                            trackColor = Color(0xFFEEEEEE)
                        )
                    }
                }
            }

            // Card 2: Macros Dinâmicos
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 6.dp)
                    .clickable { onIrCardapio() },
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .background(PratoCertoColors.IconBoxBg, RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("🥗", fontSize = 28.sp)
                    }
                    Spacer(Modifier.width(16.dp))
                    Column {
                        Text("Macros do Cardápio", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text("${"%.0f".format(totalCalorias)} kcal no total", fontSize = 11.sp, color = PratoCertoColors.TextGray)
                        Text("Proteínas: ${"%.1f".format(totalProteinas)}g", fontSize = 11.sp, color = PratoCertoColors.TextGray)
                    }
                }
            }

            // Card 3: Lista de Compras Dinâmica
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 6.dp)
                    .clickable { onNavigate("lista_compras") },
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .background(PratoCertoColors.IconBoxBg, RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.ShoppingBag, null, tint = PratoCertoColors.PrimaryGreen)
                    }
                    Spacer(Modifier.width(16.dp))
                    Column {
                        Text("Lista de Compras", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text("$totalItensCompras itens no cardápio", fontSize = 11.sp, color = PratoCertoColors.TextGray)
                        Text(
                            "Gasto total: R$ ${"%.2f".format(totalGasto)}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = PratoCertoColors.TextGreen,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            // Botões de Ação Rápida
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onIrPesquisa,
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = PratoCertoColors.TextGreen)
                ) {
                    Icon(Icons.Default.Search, contentDescription = null)
                    Spacer(Modifier.width(6.dp))
                    Text("Buscar TACO", fontSize = 13.sp)
                }

                Button(
                    onClick = { },
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PratoCertoColors.DarkButton)
                ) {
                    Icon(Icons.Default.QrCodeScanner, contentDescription = null)
                    Spacer(Modifier.width(6.dp))
                    Text("Escanear", fontSize = 13.sp)
                }
            }
        }
    }
}