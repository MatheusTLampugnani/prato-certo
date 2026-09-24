package com.example.pratocerto.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pratocerto.ui.theme.PratoCertoColors
import com.example.pratocerto.util.SessionManager

@Composable
fun TelaMenuPrincipal(
    onIrOrcamento: () -> Unit,
    onIrPesquisa: () -> Unit,
    onIrCardapio: () -> Unit,
    onLogout: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PratoCertoColors.Background)
    ) {
        // Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(PratoCertoColors.Green)
                .padding(24.dp)
        ) {
            Column {
                Text(
                    "🥗 Prato Certo",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    "Olá, ${SessionManager.nomeUsuario.ifEmpty { "usuário" }}!",
                    fontSize = 14.sp,
                    color = Color.White.copy(alpha = 0.85f),
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Spacer(Modifier.height(4.dp))

            Text(
                "O que você quer fazer?",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF333333)
            )

            CardMenu(
                icone = Icons.Default.AttachMoney,
                titulo = "Definir orçamento",
                descricao = "Configure quanto você tem para gastar",
                cor = Color(0xFF4CAF50),
                onClick = onIrOrcamento
            )

            CardMenu(
                icone = Icons.Default.Search,
                titulo = "Pesquisar alimentos",
                descricao = "Busque alimentos com info nutricional e preço",
                cor = Color(0xFF2196F3),
                onClick = onIrPesquisa
            )

            CardMenu(
                icone = Icons.Default.List,
                titulo = "Meu cardápio / Lista",
                descricao = "Veja os itens da sua lista de compras",
                cor = Color(0xFFFF9800),
                onClick = onIrCardapio
            )

            Spacer(Modifier.weight(1f))

            // Logout
            TextButton(
                onClick = {
                    SessionManager.limpar()
                    onLogout()
                },
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                Icon(
                    Icons.Default.ExitToApp,
                    contentDescription = null,
                    tint = PratoCertoColors.TextGray
                )
                Spacer(Modifier.width(6.dp))
                Text("Sair da conta", color = PratoCertoColors.TextGray, fontSize = 14.sp)
            }
        }
    }
}

@Composable
private fun CardMenu(
    icone: ImageVector,
    titulo: String,
    descricao: String,
    cor: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(cor.copy(alpha = 0.12f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icone, contentDescription = null, tint = cor, modifier = Modifier.size(26.dp))
            }
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(titulo, fontWeight = FontWeight.SemiBold, fontSize = 15.sp, color = Color(0xFF222222))
                Text(descricao, fontSize = 12.sp, color = PratoCertoColors.TextGray, modifier = Modifier.padding(top = 2.dp))
            }
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color(0xFFCCCCCC))
        }
    }
}
