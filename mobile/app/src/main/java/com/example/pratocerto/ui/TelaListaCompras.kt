package com.example.pratocerto.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.outlined.Circle
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.pratocerto.model.ItemCompra
import com.example.pratocerto.ui.theme.PratoCertoColors
import com.example.pratocerto.viewmodel.ListaComprasViewModel

@Composable
fun TelaListaCompras(viewModel: ListaComprasViewModel = viewModel()) {

    // Dispara a requisição GET assim que a tela é exibida
    LaunchedEffect(Unit) {
        viewModel.carregarItens()
    }

    val totalGasto = viewModel.itens.sumOf { it.preco }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PratoCertoColors.Background)
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp, 24.dp, 24.dp, 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.ShoppingCart, contentDescription = null, tint = Color(0xFF333333))
            Spacer(Modifier.width(8.dp))
            Text("Lista de Compras", fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
        }

        // Card escuro com o gasto estimado (soma dos preços vindos da API)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = PratoCertoColors.CardDark)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Gasto Estimado", fontSize = 11.sp, color = Color(0xFFAAAAAA))
                Text(
                    "R$ ${"%.2f".format(totalGasto)}",
                    fontSize = 24.sp,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        when {
            viewModel.carregando -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = PratoCertoColors.Green)
                }
            }

            viewModel.erro != null -> {
                Text(
                    viewModel.erro ?: "",
                    color = Color.Red,
                    modifier = Modifier.padding(20.dp)
                )
            }

            else -> {
                Text(
                    "Itens",
                    fontSize = 14.sp,
                    color = PratoCertoColors.TextGray,
                    modifier = Modifier.padding(start = 20.dp, top = 16.dp, bottom = 8.dp)
                )

                // A lista construída a partir dos dados recebidos da API (JSON)
                LazyColumn {
                    items(viewModel.itens) { item ->
                        ItemListaRow(item)
                        Divider(color = PratoCertoColors.Divider)
                    }
                }
            }
        }
    }
}

@Composable
private fun ItemListaRow(item: ItemCompra) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(20.dp, 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(PratoCertoColors.IconBoxBg),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                if (item.comprado) Icons.Default.CheckCircle else Icons.Outlined.Circle,
                contentDescription = null,
                tint = if (item.comprado) PratoCertoColors.Green else PratoCertoColors.CircleEmpty
            )
        }
        Spacer(Modifier.width(12.dp))
        Text(
            item.nome,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.weight(1f)
        )
        Text(
            "R$ ${"%.2f".format(item.preco)}",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = PratoCertoColors.TextGreen
        )
    }
}
