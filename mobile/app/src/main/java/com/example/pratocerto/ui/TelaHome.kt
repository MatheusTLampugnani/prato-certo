package com.example.pratocerto.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pratocerto.ui.theme.PratoCertoColors

import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.pratocerto.viewmodel.CardapioViewModel
import com.example.pratocerto.viewmodel.MetasViewModel
import com.example.pratocerto.viewmodel.OrcamentoViewModel
import androidx.compose.runtime.LaunchedEffect

@Composable
fun TelaHome(
    onIrOrcamento: () -> Unit,
    onIrCardapio: () -> Unit,
    onIrLista: () -> Unit,
    onIrPesquisa: () -> Unit,
    navContent: @Composable () -> Unit,
    orcamentoViewModel: OrcamentoViewModel = viewModel(),
    metasViewModel: MetasViewModel = viewModel(),
    cardapioViewModel: CardapioViewModel = viewModel()
) {
    LaunchedEffect(Unit) {
        orcamentoViewModel.carregarOrcamento()
        metasViewModel.carregarMeta()
        cardapioViewModel.carregarCardapio()
    }

    val orcamentoValor = orcamentoViewModel.orcamento?.valor ?: 0.0
    val listasMercado = cardapioViewModel.listasCardapio.filter { it.tipo == "mercado" }
    val gastoEstimado = listasMercado.sumOf { it.valorTotal }
    val disponivel = orcamentoValor - gastoEstimado
    
    val badgeOrcamento = if (orcamentoValor == 0.0) null else if (disponivel >= 0) "OK" else "ESTOUROU"
    val subtituloOrcamento = if (orcamentoValor > 0) "Disponível: R$ ${"%.2f".format(disponivel)} de R$ ${"%.2f".format(orcamentoValor)}" else "Nenhum orçamento definido"
    val percentOrcamento = if (orcamentoValor > 0) (gastoEstimado / orcamentoValor).toFloat().coerceIn(0f, 1f) else 0f

    val metaCalorias = metasViewModel.meta?.calorias_dia ?: 0
    val metaProteinas = metasViewModel.meta?.proteinas_dia ?: 0
    
    val listasCardapio = cardapioViewModel.listasCardapio.filter { it.tipo == "cardapio" }
    val itensCardapio = listasCardapio.flatMap { it.itens ?: emptyList() }
    val caloriasAtuais = itensCardapio.sumOf { (it.alimentos?.calorias ?: 0.0) * (it.quantidadeGramas / 100.0) }
    val proteinasAtuais = itensCardapio.sumOf { (it.alimentos?.proteinas ?: 0.0) * (it.quantidadeGramas / 100.0) }

    val badgeMacros = if (metaCalorias == 0) null else if (caloriasAtuais <= metaCalorias) "OK" else "ESTOUROU"
    val subtituloMacros = if (metaCalorias > 0) "${caloriasAtuais.toInt()} de $metaCalorias kcal  •  Proteínas: ${proteinasAtuais.toInt()}g / ${metaProteinas}g" else "Nenhuma meta definida"
    val percentMacros = if (metaCalorias > 0) (caloriasAtuais / metaCalorias).toFloat().coerceIn(0f, 1f) else 0f

    val totalItensMercado = listasMercado.sumOf { it.totalItens }
    val badgeLista = if (totalItensMercado > 0) "Pendente" else null
    val subtituloLista = if (totalItensMercado == 1) "1 item na lista de compras" else "$totalItensMercado itens pendentes para o mercado"
    val extraLista = if (gastoEstimado > 0) "Gasto est.: R$ ${"%.2f".format(gastoEstimado)}" else null

    Box(modifier = Modifier.fillMaxSize().background(PratoCertoColors.Background)) {
        Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(bottom = 100.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 24.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Menu, contentDescription = null, tint = Color(0xFF333333), modifier = Modifier.size(24.dp))
                Text("Resumo Diário", fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                Box(modifier = Modifier.size(36.dp).clip(RoundedCornerShape(50)).background(Color(0xFFCCCCCC)))
            }

            // Card Orçamento
            InfoCard(
                icone = Icons.Default.AccountBalanceWallet,
                titulo = "Orçamento Semanal",
                subtitulo = subtituloOrcamento,
                badge = badgeOrcamento,
                barraPercent = percentOrcamento,
                onClick = onIrOrcamento
            )

            // Card Macros
            InfoCard(
                icone = Icons.Default.Restaurant,
                titulo = "Macros (Hoje)",
                subtitulo = subtituloMacros,
                badge = badgeMacros,
                barraPercent = percentMacros,
                onClick = onIrCardapio
            )

            // Card Lista
            InfoCard(
                icone = Icons.Default.ShoppingCart,
                titulo = "Lista de Compras",
                subtitulo = subtituloLista,
                badge = badgeLista,
                extra = extraLista,
                onClick = onIrLista
            )

            // Botões rápidos
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onIrPesquisa,
                    modifier = Modifier.weight(1f).height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    border = ButtonDefaults.outlinedButtonBorder.copy(width = 2.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = PratoCertoColors.TextGreen)
                ) {
                    Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Buscar TACO", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                }
                Button(
                    onClick = {},
                    modifier = Modifier.weight(1f).height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF222222))
                ) {
                    Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Escanear", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                }
            }
        }

        // Nav flutuante
        Box(modifier = Modifier.align(Alignment.BottomCenter)) { navContent() }
    }
}

@Composable
fun InfoCard(
    icone: ImageVector,
    titulo: String,
    subtitulo: String,
    badge: String?,
    extra: String? = null,
    barraPercent: Float = 0f,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(Color.White)
            .clickable { onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Box(contentAlignment = Alignment.TopEnd) {
            Box(
                modifier = Modifier.size(60.dp).clip(RoundedCornerShape(12.dp)).background(PratoCertoColors.IconBoxBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(icone, contentDescription = null, tint = PratoCertoColors.TextGreen, modifier = Modifier.size(28.dp))
            }
            if (badge != null) {
                Box(
                    modifier = Modifier.offset(x = 6.dp, y = (-6).dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color.White)
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(badge, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFFE53935))
                }
            }
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(titulo, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            Text(subtitulo, fontSize = 11.sp, color = PratoCertoColors.TextGray, modifier = Modifier.padding(top = 2.dp))
            if (barraPercent > 0f) {
                Box(
                    modifier = Modifier.fillMaxWidth().height(6.dp).padding(top = 4.dp)
                        .clip(RoundedCornerShape(4.dp)).background(Color(0xFFEEEEEE))
                ) {
                    Box(modifier = Modifier.fillMaxWidth(barraPercent).fillMaxHeight()
                        .background(PratoCertoColors.Green, RoundedCornerShape(4.dp)))
                }
            }
            if (extra != null) {
                Text(extra, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = PratoCertoColors.TextGreen, modifier = Modifier.padding(top = 4.dp))
            }
        }
    }
}