package com.example.pratocerto.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.pratocerto.model.ItemLista
import com.example.pratocerto.ui.theme.PratoCertoColors
import com.example.pratocerto.viewmodel.CardapioViewModel

@Composable
fun TelaLista(
    onVoltar: () -> Unit,
    onIrPesquisa: () -> Unit,
    navContent: @Composable () -> Unit = {},
    viewModel: CardapioViewModel = viewModel()
) {
    var mostrarDialog by remember { mutableStateOf(false) }
    var tituloNovaLista by remember { mutableStateOf("") }
    var listaParaExcluirId by remember { mutableStateOf<Int?>(null) }

    LaunchedEffect(Unit) { viewModel.carregarCardapio() }

    Box(modifier = Modifier.fillMaxSize().background(PratoCertoColors.Background)) {
        Column(modifier = Modifier.fillMaxSize().padding(bottom = 100.dp)) {

            // Cabeçalho Superior
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onVoltar, modifier = Modifier.size(44.dp)) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Voltar",
                        tint = Color(0xFF222222)
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.ShoppingCart, contentDescription = null, modifier = Modifier.size(20.dp), tint = PratoCertoColors.TextGreen)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Lista de Compras", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1F2937))
                }

                IconButton(onClick = { mostrarDialog = true }, modifier = Modifier.size(44.dp)) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Nova Lista",
                        tint = PratoCertoColors.TextGreen
                    )
                }
            }

            when {
                viewModel.carregando -> {
                    Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = PratoCertoColors.Green)
                    }
                }
                viewModel.erro != null -> {
                    Text(viewModel.erro ?: "", color = Color.Red, modifier = Modifier.padding(20.dp))
                }
                else -> {
                    val listasMercado = viewModel.listasCardapio.filter { it.tipo == "mercado" }
                    
                    if (listasMercado.isEmpty()) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 48.dp, start = 24.dp, end = 24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(72.dp)
                                    .clip(RoundedCornerShape(36.dp))
                                    .background(PratoCertoColors.IconBoxBg),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.ShoppingCart, contentDescription = null, tint = PratoCertoColors.TextGreen, modifier = Modifier.size(36.dp))
                            }
                            Spacer(Modifier.height(16.dp))
                            Text("Nenhuma lista de compras criada ainda.", color = PratoCertoColors.TextGray, fontSize = 15.sp)
                            Spacer(Modifier.height(20.dp))

                            Button(
                                onClick = { mostrarDialog = true },
                                colors = ButtonDefaults.buttonColors(containerColor = PratoCertoColors.Green),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.height(48.dp)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(8.dp))
                                Text("Criar Nova Lista", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            }
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(bottom = 110.dp)
                        ) {
                            items(listasMercado) { lista ->
                                val itensSeguros = lista.itens ?: emptyList()
                                val valorSeguro = lista.valorTotal

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 20.dp, vertical = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        lista.titulo_lista ?: "Lista de Compras", 
                                        fontSize = 18.sp, 
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF1F2937),
                                        modifier = Modifier.weight(1f)
                                    )
                                    IconButton(
                                        onClick = { listaParaExcluirId = lista.id },
                                        modifier = Modifier.size(40.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.DeleteOutline,
                                            contentDescription = "Excluir Lista",
                                            tint = Color(0xFFD32F2F),
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }

                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 20.dp, vertical = 4.dp),
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1F2937))
                                ) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Text("Gasto Estimado Total", fontSize = 12.sp, color = Color(0xFF9CA3AF))
                                        Spacer(Modifier.height(4.dp))
                                        Text("R$ ${"%.2f".format(valorSeguro)}", fontSize = 26.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
                                        Spacer(Modifier.height(8.dp))
                                        Box(modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(4.dp)).background(Color(0xFF374151))) {
                                            Box(modifier = Modifier.fillMaxWidth(if (valorSeguro > 0) 0.35f else 0f).fillMaxHeight().background(PratoCertoColors.Green, RoundedCornerShape(4.dp)))
                                        }
                                        Text("${itensSeguros.size} itens na lista", fontSize = 11.sp, color = Color(0xFF9CA3AF), modifier = Modifier.padding(top = 6.dp).align(Alignment.End))
                                    }
                                }

                                if (itensSeguros.isEmpty()) {
                                    Column(
                                        modifier = Modifier.fillMaxWidth().padding(top = 16.dp, bottom = 32.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text("Sua lista está vazia.", color = PratoCertoColors.TextGray, fontSize = 14.sp)
                                        Spacer(Modifier.height(16.dp))

                                        Button(
                                            onClick = onIrPesquisa,
                                            colors = ButtonDefaults.buttonColors(containerColor = PratoCertoColors.Green),
                                            shape = RoundedCornerShape(12.dp),
                                            modifier = Modifier.height(45.dp)
                                        ) {
                                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                                            Spacer(Modifier.width(8.dp))
                                            Text("Adicionar Itens", fontWeight = FontWeight.SemiBold)
                                        }
                                    }
                                } else {
                                    Row(
                                        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("Itens", fontSize = 14.sp, color = PratoCertoColors.TextGray, fontWeight = FontWeight.SemiBold)
                                        TextButton(onClick = onIrPesquisa) {
                                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp), tint = PratoCertoColors.TextGreen)
                                            Spacer(Modifier.width(4.dp))
                                            Text("Adicionar mais", color = PratoCertoColors.TextGreen, fontSize = 12.sp)
                                        }
                                    }
                                    
                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 20.dp, vertical = 4.dp),
                                        shape = RoundedCornerShape(16.dp),
                                        colors = CardDefaults.cardColors(containerColor = Color.White),
                                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                                    ) {
                                        Column {
                                            itensSeguros.forEachIndexed { index, item ->
                                                val itemId = item.id ?: 0
                                                ItemListaRow(
                                                    item = item,
                                                    checado = item.comprado,
                                                    onToggleCheck = {
                                                        if (itemId > 0) {
                                                            viewModel.alternarItemComprado(itemId, !item.comprado)
                                                        }
                                                    },
                                                    onExcluir = {
                                                        item.id?.let { viewModel.excluirItem(it) }
                                                    }
                                                )
                                                if (index < itensSeguros.lastIndex) {
                                                    HorizontalDivider(color = PratoCertoColors.Divider)
                                                }
                                            }
                                        }
                                    }
                                    
                                    Spacer(Modifier.height(24.dp))
                                }
                            }
                        }
                    }
                }
            }
        }

        // Navegação Inferior
        Box(modifier = Modifier.align(Alignment.BottomCenter)) { navContent() }

        // Diálogo de Nova Lista
        if (mostrarDialog) {
            AlertDialog(
                onDismissRequest = { mostrarDialog = false },
                title = { Text("Nova Lista de Compras", fontWeight = FontWeight.Bold) },
                text = {
                    OutlinedTextField(
                        value = tituloNovaLista,
                        onValueChange = { tituloNovaLista = it },
                        label = { Text("Título da lista") },
                        singleLine = true
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            if (tituloNovaLista.isNotBlank()) {
                                viewModel.criarNovaListaMercado(tituloNovaLista)
                                mostrarDialog = false
                                tituloNovaLista = ""
                            }
                        }
                    ) {
                        Text("Criar", color = PratoCertoColors.TextGreen, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { mostrarDialog = false }) {
                        Text("Cancelar", color = Color.Gray)
                    }
                }
            )
        }

        // Diálogo de Confirmação para Excluir Lista
        listaParaExcluirId?.let { idLista ->
            AlertDialog(
                onDismissRequest = { listaParaExcluirId = null },
                title = { Text("Excluir Lista", fontWeight = FontWeight.Bold) },
                text = { Text("Deseja realmente excluir esta lista e todos os seus itens?") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            viewModel.excluirLista(idLista)
                            listaParaExcluirId = null
                        }
                    ) {
                        Text("Excluir", color = Color.Red, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { listaParaExcluirId = null }) {
                        Text("Cancelar", color = Color.Gray)
                    }
                }
            )
        }
    }
}

@Composable
private fun ItemListaRow(
    item: ItemLista,
    checado: Boolean,
    onToggleCheck: () -> Unit,
    onExcluir: () -> Unit
) {
    val preco = (item.precoCalculado as? Double) ?: 0.0

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .clickable { onToggleCheck() }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onToggleCheck, modifier = Modifier.size(36.dp)) {
            Icon(
                imageVector = if (checado) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                contentDescription = "Marcar comprado",
                tint = if (checado) PratoCertoColors.Green else Color(0xFFBDBDBD),
                modifier = Modifier.size(24.dp)
            )
        }
        Spacer(Modifier.width(8.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                item.alimentos?.nome ?: "Item",
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
                textDecoration = if (checado) TextDecoration.LineThrough else TextDecoration.None,
                color = if (checado) Color.Gray else Color(0xFF222222)
            )
            Text(
                "${item.quantidadeGramas.toInt()}g",
                fontSize = 12.sp,
                color = PratoCertoColors.TextGray
            )
        }
        Text(
            "R$ ${"%.2f".format(preco)}",
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            color = if (checado) Color.Gray else PratoCertoColors.TextGreen
        )
        Spacer(Modifier.width(8.dp))
        IconButton(onClick = onExcluir, modifier = Modifier.size(40.dp)) {
            Icon(
                Icons.Default.Close,
                contentDescription = "Remover",
                tint = Color(0xFF9E9E9E),
                modifier = Modifier.size(18.dp)
            )
        }
    }
}