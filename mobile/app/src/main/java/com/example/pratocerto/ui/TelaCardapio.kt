package com.example.pratocerto.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.pratocerto.model.CardapioItemData
import com.example.pratocerto.model.ItemListaCardapio
import com.example.pratocerto.ui.theme.PratoCertoColors
import com.example.pratocerto.viewmodel.CardapioViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TelaCardapio(
    onVoltar: () -> Unit,
    viewModel: CardapioViewModel = viewModel()
) {
    var mostrarDialogoNovoCardapio by remember { mutableStateOf(false) }
    var novoTituloCardapio by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        viewModel.carregarCardapio()
        viewModel.carregarOrcamento()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Cardápio / Lista", fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = onVoltar) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { mostrarDialogoNovoCardapio = true },
                containerColor = PratoCertoColors.Green,
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "Criar Cardápio")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(PratoCertoColors.Background)
        ) {
            // ── Card de Orçamento Salvo e Restante ───────────────────────────
            viewModel.orcamentoValor?.let { orcamento ->
                val totalGasto = viewModel.listasCardapio.sumOf { cardapio ->
                    cardapio.listaItens.sumOf { it.precoCalculado }
                }
                val saldoRestante = orcamento - totalGasto

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                "Orçamento Definido",
                                fontSize = 12.sp,
                                color = Color(0xFF2E7D32),
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                "R$ ${"%.2f".format(orcamento)}",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1B5E20)
                            )
                        }

                        VerticalDivider(
                            modifier = Modifier.height(36.dp),
                            color = Color(0xFFA5D6A7)
                        )

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                "Saldo Restante",
                                fontSize = 12.sp,
                                color = if (saldoRestante >= 0) Color(0xFF2E7D32) else Color.Red,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                "R$ ${"%.2f".format(saldoRestante)}",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (saldoRestante >= 0) Color(0xFF1B5E20) else Color.Red
                            )
                        }
                    }
                }
            }

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

                viewModel.listasCardapio.isNotEmpty() -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 80.dp)
                    ) {
                        items(viewModel.listasCardapio) { cardapio ->
                            CardapioCard(cardapio)
                            Spacer(modifier = Modifier.height(12.dp))
                        }
                    }
                }

                else -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            "Nenhum cardápio cadastrado ainda.",
                            color = PratoCertoColors.TextGray,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
    }

    // ── Diálogo de Criação de Cardápio ───────────────────────────────────────
    if (mostrarDialogoNovoCardapio) {
        AlertDialog(
            onDismissRequest = { mostrarDialogoNovoCardapio = false },
            title = { Text("Novo Cardápio") },
            text = {
                OutlinedTextField(
                    value = novoTituloCardapio,
                    onValueChange = { novoTituloCardapio = it },
                    label = { Text("Nome do Cardápio") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (novoTituloCardapio.isNotBlank()) {
                            viewModel.criarNovoCardapio(novoTituloCardapio)
                            novoTituloCardapio = ""
                            mostrarDialogoNovoCardapio = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PratoCertoColors.Green)
                ) {
                    Text("Criar")
                }
            },
            dismissButton = {
                TextButton(onClick = { mostrarDialogoNovoCardapio = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
private fun CardapioCard(cardapio: CardapioItemData) {
    val totalItens = cardapio.listaItens.size
    val valorTotal = cardapio.listaItens.sumOf { it.precoCalculado }

    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
        Card(
            modifier = Modifier.fillMaxWidth(),
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
                    Text(
                        cardapio.tituloLista,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 15.sp
                    )
                    Text(
                        "$totalItens item(s)  ·  R$ ${"%.2f".format(valorTotal)}",
                        color = Color.White.copy(0.8f),
                        fontSize = 13.sp,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        cardapio.listaItens.forEach { item ->
            ItemListaRow(item)
            HorizontalDivider(color = PratoCertoColors.Divider)
        }
    }
}

@Composable
private fun ItemListaRow(item: ItemListaCardapio) {
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