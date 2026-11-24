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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pratocerto.ui.theme.PratoCertoColors
import com.example.pratocerto.ui.components.SecaoCardsDicas

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
    onIrPerfil: () -> Unit = {},
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .verticalScroll(rememberScrollState())
                .navigationBarsPadding()
                .padding(bottom = 104.dp)
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onIrPerfil, modifier = Modifier.size(44.dp)) {
                    Icon(Icons.Default.Menu, contentDescription = "Menu / Perfil", tint = Color(0xFF222222), modifier = Modifier.size(24.dp))
                }
                Text("Resumo Diário", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1F2937))
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(50))
                        .background(PratoCertoColors.Green)
                        .clickable { onIrPerfil() },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = com.example.pratocerto.util.SessionManager.nomeUsuario.take(1).uppercase().ifEmpty { "U" },
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }
            }

            val periodoNome = when (orcamentoViewModel.orcamento?.periodo?.lowercase()) {
                "mensal" -> "Mensal"
                "quinzenal" -> "Quinzenal"
                "semanal" -> "Semanal"
                "diario" -> "Diário"
                else -> "Definido"
            }

            // Card Orçamento
            InfoCard(
                icone = Icons.Default.AccountBalanceWallet,
                titulo = "Orçamento $periodoNome",
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
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onIrPesquisa,
                    modifier = Modifier.weight(1f).height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp),
                    border = ButtonDefaults.outlinedButtonBorder.copy(width = 1.5.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = PratoCertoColors.TextGreen)
                ) {
                    Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(
                        "Buscar Alimentos",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Button(
                    onClick = onIrCardapio,
                    modifier = Modifier.weight(1f).height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF222222))
                ) {
                    Icon(Icons.Default.Restaurant, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(
                        "Ver Cardápio",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // Seção de Dicas, Notícias e Hábitos Saudáveis (posicionada logo acima da barra de navegação)
            SecaoCardsDicas(
                onIrOrcamento = onIrOrcamento,
                onIrCardapio = onIrCardapio,
                onIrPesquisa = onIrPesquisa
            )

            Spacer(Modifier.height(16.dp))
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
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 6.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(PratoCertoColors.IconBoxBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(icone, contentDescription = null, tint = PratoCertoColors.TextGreen, modifier = Modifier.size(26.dp))
            }
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = titulo,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1F2937),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    if (badge != null) {
                        val isOk = badge == "OK"
                        val corBadge = if (isOk) PratoCertoColors.TextGreen else Color(0xFFD32F2F)
                        val corFundoBadge = if (isOk) Color(0xFFEAF3D8) else Color(0xFFFFEBEE)
                        Spacer(Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(corFundoBadge)
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(badge, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = corBadge)
                        }
                    }
                }
                Text(
                    text = subtitulo,
                    fontSize = 12.sp,
                    color = PratoCertoColors.TextGray,
                    modifier = Modifier.padding(top = 3.dp)
                )
                if (barraPercent > 0f) {
                    val corBarra = if (badge == "ESTOUROU") Color(0xFFE53935) else PratoCertoColors.Green
                    Box(
                        modifier = Modifier
                            .padding(top = 8.dp)
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color(0xFFEEEEEE))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(barraPercent)
                                .fillMaxHeight()
                                .background(corBarra, RoundedCornerShape(4.dp))
                        )
                    }
                }
                if (extra != null) {
                    Text(extra, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = PratoCertoColors.TextGreen, modifier = Modifier.padding(top = 4.dp))
                }
            }
        }
    }
}