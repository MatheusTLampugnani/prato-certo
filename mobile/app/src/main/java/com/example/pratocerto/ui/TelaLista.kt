package com.example.pratocerto.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.pratocerto.model.ItemLista
import com.example.pratocerto.ui.theme.PratoCertoColors
import com.example.pratocerto.viewmodel.CardapioViewModel

@Composable
fun TelaLista(
    onVoltar: () -> Unit,
    navContent: @Composable () -> Unit = {},
    viewModel: CardapioViewModel = viewModel()
) {
    LaunchedEffect(Unit) { viewModel.carregarCardapio() }

    Box(modifier = Modifier.fillMaxSize().background(PratoCertoColors.Background)) {
        Column(modifier = Modifier.fillMaxSize().padding(bottom = 100.dp)) {

            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 24.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Voltar",
                    modifier = Modifier.clickable { onVoltar() }
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.ShoppingCart, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Lista de Compras", fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                }

                Spacer(Modifier.width(24.dp))
            }

            when {
                viewModel.carregando -> {
                    Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = PratoCertoColors.Green)
                    }
                }
                viewModel.listasCardapio.isNotEmpty() -> {
                    val lista = viewModel.listasCardapio.first()

                    // SEGURANÇA contra valores nulos
                    val itensSeguros = (lista.itens as? List<ItemLista>) ?: emptyList()
                    val valorSeguro = (lista.valorTotal as? Double) ?: 0.0

                    Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 0.dp)
                        .clip(RoundedCornerShape(16.dp)).background(Color(0xFF222222)).padding(16.dp)
                    ) {
                        Column {
                            Text("Gasto Estimado (Semanal)", fontSize = 11.sp, color = Color(0xFFAAAAAA))
                            Text("R$ ${"%.2f".format(valorSeguro)}", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.White, modifier = Modifier.padding(bottom = 8.dp))
                            Box(modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(4.dp)).background(Color(0xFF444444))) {
                                Box(modifier = Modifier.fillMaxWidth(0.25f).fillMaxHeight().background(PratoCertoColors.Green, RoundedCornerShape(4.dp)))
                            }
                            Text("R$ 144,10 restantes do orçamento", fontSize = 10.sp, color = Color(0xFFAAAAAA), modifier = Modifier.padding(top = 6.dp).align(Alignment.End))
                        }
                    }

                    Spacer(Modifier.height(16.dp))
                    Text("Pendentes", fontSize = 14.sp, color = PratoCertoColors.TextGray, modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp))

                    LazyColumn {
                        items(itensSeguros) { item ->
                            ItemListaRow(item)
                            HorizontalDivider(color = PratoCertoColors.Divider)
                        }
                    }
                }
                viewModel.erro != null -> {
                    Text(viewModel.erro ?: "", color = Color.Red, modifier = Modifier.padding(20.dp))
                }
            }
        }
        Box(modifier = Modifier.align(Alignment.BottomCenter)) { navContent() }
    }
}

@Composable
private fun ItemListaRow(item: ItemLista) {
    val preco = (item.precoCalculado as? Double) ?: 0.0

    Row(
        modifier = Modifier.fillMaxWidth().background(Color.White).padding(horizontal = 20.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.RadioButtonUnchecked,
            contentDescription = null,
            tint = Color(0xFFCCCCCC),
            modifier = Modifier.size(24.dp)
        )
        Spacer(Modifier.width(12.dp))
        Text(item.alimentos?.nome ?: "Item", fontWeight = FontWeight.SemiBold, fontSize = 14.sp, modifier = Modifier.weight(1f))
        Text("R$ ${"%.2f".format(preco)}", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = PratoCertoColors.TextGreen)
    }
}