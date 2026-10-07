package com.example.pratocerto.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Restaurant
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
import com.example.pratocerto.viewmodel.MetasViewModel

@Composable
fun TelaCardapio(
    onVoltar: () -> Unit,
    onIrPesquisa: () -> Unit,
    onIrMetas: () -> Unit = {},
    onIrLista: () -> Unit = {},
    navContent: @Composable () -> Unit = {},
    viewModel: CardapioViewModel = viewModel(),
    metasViewModel: MetasViewModel = viewModel()
) {
    LaunchedEffect(Unit) {
        viewModel.carregarCardapio()
        metasViewModel.carregarMeta()
    }

    Box(modifier = Modifier.fillMaxSize().background(PratoCertoColors.Background)) {
        Column(modifier = Modifier.fillMaxSize().padding(bottom = 100.dp)) {

            Row(
                modifier = Modifier.fillMaxWidth().background(PratoCertoColors.Green)
                    .padding(horizontal = 20.dp, vertical = 24.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Voltar",
                    tint = Color.White,
                    modifier = Modifier.clickable { onVoltar() }
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.AutoMirrored.Filled.MenuBook, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Meu Cardápio", fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                }

                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Adicionar",
                    tint = Color.White,
                    modifier = Modifier.clickable { onIrPesquisa() }
                )
            }

            when {
                viewModel.carregando -> {
                    Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = PratoCertoColors.Green)
                    }
                }
                viewModel.erro != null -> {
                    Text(viewModel.erro ?: "", color = Color.Red, modifier = Modifier.padding(20.dp), fontSize = 13.sp)
                }
                viewModel.listasCardapio.any { it.tipo == "cardapio" } -> {
                    val lista = viewModel.listasCardapio.first { it.tipo == "cardapio" }

                    val itensSeguros = lista.itens ?: emptyList()
                    val valorSeguro = lista.valorTotal
                    val totalSeguro = lista.totalItens
                    
                    val caloriasAtuais = itensSeguros.sumOf { (it.alimentos?.calorias ?: 0.0) * (it.quantidadeGramas / 100.0) }
                    val proteinaAtual = itensSeguros.sumOf { (it.alimentos?.proteinas ?: 0.0) * (it.quantidadeGramas / 100.0) }

                    LazyColumn {
                        if (itensSeguros.isEmpty()) {
                            item {
                                Column(
                                    modifier = Modifier.fillMaxWidth().padding(32.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text("Seu cardápio está vazio.", color = PratoCertoColors.TextGray)
                                    Spacer(Modifier.height(16.dp))
                                    if (metasViewModel.sugestoes.isNotEmpty()) {
                                        Button(
                                            onClick = { viewModel.gerarListaSugestao(onSucesso = onIrLista) },
                                            colors = ButtonDefaults.buttonColors(containerColor = PratoCertoColors.Green),
                                            shape = RoundedCornerShape(12.dp)
                                        ) {
                                            Text("Criar lista Sugestão")
                                        }
                                    }
                                }
                            }
                        } else {
                            items(itensSeguros) { item ->
                                ItemCardapioRow(item)
                                HorizontalDivider(color = PratoCertoColors.Divider)
                            }
                        }

                        item {
                            Box(
                                modifier = Modifier.fillMaxWidth().padding(20.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(PratoCertoColors.IconBoxBg)
                                    .clickable { onIrMetas() }
                                    .padding(16.dp)
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                                    Text("Resumo Nutricional do Dia", fontSize = 12.sp, color = PratoCertoColors.TextGreen, fontWeight = FontWeight.SemiBold)
                                    Text("R$ ${"%.2f".format(valorSeguro)}", fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF1A1A1A), modifier = Modifier.padding(top = 4.dp))
                                    Text("$totalSeguro itens na lista", fontSize = 12.sp, color = PratoCertoColors.TextGray)
                                    
                                    if (metasViewModel.meta != null) {
                                        Spacer(Modifier.height(12.dp))
                                        val calMeta = metasViewModel.meta!!.calorias_dia
                                        val protMeta = metasViewModel.meta!!.proteinas_dia
                                        Text("Calorias: ${caloriasAtuais.toInt()} / $calMeta kcal", fontSize = 13.sp, color = Color.DarkGray, fontWeight = FontWeight.SemiBold)
                                        Text("Proteínas: ${proteinaAtual.toInt()}g / ${protMeta}g", fontSize = 13.sp, color = Color.DarkGray, fontWeight = FontWeight.SemiBold)
                                    }

                                    Spacer(Modifier.height(12.dp))
                                    Button(
                                        onClick = onIrMetas,
                                        colors = ButtonDefaults.buttonColors(containerColor = PratoCertoColors.Green),
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text("Editar metas", fontWeight = FontWeight.SemiBold)
                                    }
                                }
                            }
                        }
                    }
                }
                else -> {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(top = 40.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Nenhum cardápio criado.", color = PratoCertoColors.TextGray, fontSize = 14.sp)
                        Spacer(Modifier.height(24.dp))
                        if (metasViewModel.sugestoes.isNotEmpty()) {
                            Button(
                                onClick = { viewModel.gerarListaSugestao(onSucesso = onIrLista) },
                                colors = ButtonDefaults.buttonColors(containerColor = PratoCertoColors.Green),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.height(45.dp)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(8.dp))
                                Text("Criar Lista Sugestão", fontWeight = FontWeight.SemiBold)
                            }
                        } else {
                            Text("Configure suas metas para ver sugestões.", color = PratoCertoColors.TextGray, fontSize = 12.sp)
                            Spacer(Modifier.height(16.dp))
                            Button(
                                onClick = onIrMetas,
                                colors = ButtonDefaults.buttonColors(containerColor = PratoCertoColors.Green),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Configurar Metas", fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }
            }
        }
        Box(modifier = Modifier.align(Alignment.BottomCenter)) { navContent() }
    }
}

@Composable
private fun ItemCardapioRow(item: ItemLista) {
    val qtd = (item.quantidadeGramas as? Double) ?: 0.0
    val preco = (item.precoCalculado as? Double) ?: 0.0

    Row(
        modifier = Modifier.fillMaxWidth().background(Color.White).padding(horizontal = 20.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.size(44.dp).clip(RoundedCornerShape(12.dp)).background(PratoCertoColors.IconBoxBg),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Restaurant,
                contentDescription = null,
                tint = PratoCertoColors.TextGreen,
                modifier = Modifier.size(22.dp)
            )
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(item.alimentos?.nome ?: "Alimento", fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = Color(0xFF333333))
            Text("${qtd.toInt()}g", fontSize = 11.sp, color = PratoCertoColors.TextGray)
        }
        Text("R$ ${"%.2f".format(preco)}", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = PratoCertoColors.TextGreen)
    }
}