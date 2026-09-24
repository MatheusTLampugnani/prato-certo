package com.example.pratocerto.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.pratocerto.model.ItemLista
import com.example.pratocerto.ui.theme.PratoCertoColors
import com.example.pratocerto.viewmodel.CardapioViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TelaCardapio(
    onVoltar: () -> Unit,
    idLista: Int = 1,
    viewModel: CardapioViewModel = viewModel()
) {
    LaunchedEffect(Unit) {
        viewModel.carregarLista(idLista)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PratoCertoColors.Background)
    ) {
        TopAppBar(
            title = { Text("Cardápio / Lista", fontWeight = FontWeight.SemiBold) },
            navigationIcon = {
                IconButton(onClick = onVoltar) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Voltar")
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
        )

        when {
            viewModel.carregando -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = PratoCertoColors.Green)
                }
            }

            viewModel.erro != null -> {
                Text(
                    viewModel.erro ?: "",
                    color = Color.Red,
                    modifier = Modifier.padding(16.dp),
                    fontSize = 13.sp
                )
            }

            viewModel.lista != null -> {
                val lista = viewModel.lista!!

                // Card de resumo
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF222222))
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.ShoppingCart,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text(lista.titulo, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 15.sp)
                            Text(
                                "${lista.totalItens} item(s)  ·  R$ ${"%.2f".format(lista.valorTotal)}",
                                color = Color.White.copy(0.8f),
                                fontSize = 13.sp,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }
                }

                Text(
                    "Itens da lista",
                    fontSize = 14.sp,
                    color = PratoCertoColors.TextGray,
                    modifier = Modifier.padding(start = 16.dp, bottom = 4.dp)
                )

                LazyColumn {
                    items(lista.itens) { item ->
                        ItemListaRow(item)
                        HorizontalDivider(color = PratoCertoColors.Divider)
                    }
                }
            }
        }
    }
}

@Composable
private fun ItemListaRow(item: ItemLista) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .background(PratoCertoColors.IconBoxBg, RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text("🛒", fontSize = 20.sp)
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                item.alimentos?.nome ?: "Alimento",
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
                color = Color(0xFF222222)
            )
            Text(
                "${item.quantidadeGramas.toInt()}g",
                fontSize = 12.sp,
                color = PratoCertoColors.TextGray,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
        Text(
            "R$ ${"%.2f".format(item.precoCalculado)}",
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            color = PratoCertoColors.TextGreen
        )
    }
}
