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

@Composable
fun TelaHome(
    onIrOrcamento: () -> Unit,
    onIrCardapio: () -> Unit,
    onIrLista: () -> Unit,
    onIrPesquisa: () -> Unit,
    navContent: @Composable () -> Unit
) {
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
                subtitulo = "Disponível: R$ 190,00 de R$ 400",
                badge = "OK",
                barraPercent = 0.52f,
                onClick = onIrOrcamento
            )

            // Card Macros
            InfoCard(
                icone = Icons.Default.Restaurant,
                titulo = "Macros (Hoje)",
                subtitulo = "1.840 de 2.200 kcal  •  Proteínas: 98g / 150g",
                badge = "OK",
                barraPercent = 0.84f,
                onClick = onIrCardapio
            )

            // Card Lista
            InfoCard(
                icone = Icons.Default.ShoppingCart,
                titulo = "Lista de Compras",
                subtitulo = "6 itens pendentes para o mercado",
                badge = null,
                extra = "Gasto est.: R$ 45,90",
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