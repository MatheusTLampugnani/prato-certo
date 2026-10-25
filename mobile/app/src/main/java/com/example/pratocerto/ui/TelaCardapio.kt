package com.example.pratocerto.ui

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
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
    val context = LocalContext.current
    var mostrarCriarDialog by remember { mutableStateOf(false) }
    var tituloNovoCardapio by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        viewModel.carregarCardapio()
        metasViewModel.carregarMeta()
    }

    Box(modifier = Modifier.fillMaxSize().background(PratoCertoColors.Background)) {
        Column(modifier = Modifier.fillMaxSize().padding(bottom = 90.dp)) {

            // Top Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(PratoCertoColors.Green)
                    .statusBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 18.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onVoltar, modifier = Modifier.size(40.dp)) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Voltar",
                        tint = Color.White
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.MenuBook,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Sugestão Inteligente",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                IconButton(
                    onClick = { mostrarCriarDialog = true },
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Novo Cardápio",
                        tint = Color.White
                    )
                }
            }

            when {
                viewModel.carregando -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = PratoCertoColors.Green)
                    }
                }
                viewModel.erro != null -> {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(viewModel.erro ?: "Erro inesperado.", color = Color(0xFFD32F2F), fontSize = 14.sp)
                        Spacer(Modifier.height(12.dp))
                        Button(
                            onClick = { viewModel.carregarCardapio() },
                            colors = ButtonDefaults.buttonColors(containerColor = PratoCertoColors.Green),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Tentar Novamente")
                        }
                    }
                }
                viewModel.listasCardapio.any { it.tipo == "cardapio" && !it.itens.isNullOrEmpty() } -> {
                    val lista = viewModel.listasCardapio.first { it.tipo == "cardapio" && !it.itens.isNullOrEmpty() }
                    val itensSeguros = lista.itens ?: emptyList()
                    val valorTotalAlimentos = lista.valorTotal

                    // Cálculo consolidado dos 4 macros
                    val caloriasTotais = itensSeguros.sumOf { (it.alimentos?.calorias ?: 0.0) * (it.quantidadeGramas / 100.0) }
                    val proteinasTotais = itensSeguros.sumOf { (it.alimentos?.proteinas ?: 0.0) * (it.quantidadeGramas / 100.0) }
                    val carboidratosTotais = itensSeguros.sumOf { (it.alimentos?.carboidratos ?: 0.0) * (it.quantidadeGramas / 100.0) }
                    val gordurasTotais = itensSeguros.sumOf { (it.alimentos?.gorduras ?: 0.0) * (it.quantidadeGramas / 100.0) }

                    val meta = metasViewModel.meta
                    val calMeta = meta?.calorias_dia ?: 0
                    val protMeta = meta?.proteinas_dia ?: 0
                    val carbMeta = meta?.carboidratos_dia ?: 0
                    val gordMeta = meta?.gorduras_dia ?: 0

                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 16.dp)
                    ) {
                        // Header da lista com Ações (Adicionar e Limpar)
                        item {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 20.dp, vertical = 14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        lista.titulo_lista ?: "Sugestão Inteligente",
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF1F2937)
                                    )
                                    Text(
                                        "${itensSeguros.size} alimentos selecionados",
                                        fontSize = 12.sp,
                                        color = PratoCertoColors.TextGray
                                    )
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    // Botão Adicionar Alimento
                                    TextButton(
                                        onClick = onIrPesquisa,
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                        modifier = Modifier.height(40.dp)
                                    ) {
                                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp), tint = PratoCertoColors.TextGreen)
                                        Spacer(Modifier.width(4.dp))
                                        Text("Adicionar", color = PratoCertoColors.TextGreen, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }

                                    // Botão Limpar Sugestão
                                    TextButton(
                                        onClick = {
                                            viewModel.limparSugestao(lista.id) {
                                                Toast.makeText(context, "Sugestão limpa com sucesso!", Toast.LENGTH_SHORT).show()
                                            }
                                        },
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                        modifier = Modifier.height(40.dp)
                                    ) {
                                        Icon(Icons.Default.DeleteOutline, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color(0xFFD32F2F))
                                        Spacer(Modifier.width(4.dp))
                                        Text("Limpar", color = Color(0xFFD32F2F), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }

                        // Alimentos da Sugestão
                        items(itensSeguros, key = { it.id ?: it.hashCode() }) { item ->
                            ItemCardapioRow(
                                item = item,
                                onExcluir = {
                                    item.id?.let { viewModel.excluirItem(it) }
                                }
                            )
                            HorizontalDivider(color = PratoCertoColors.Divider)
                        }

                        // Card de Resumo de Custos e Macros (sem períodos)
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 12.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(PratoCertoColors.IconBoxBg)
                                    .padding(16.dp)
                            ) {
                                Column(modifier = Modifier.fillMaxWidth()) {
                                    // Título e Contador
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            "RESUMO DE CUSTO E MACROS",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = PratoCertoColors.TextGreen
                                        )
                                        Text(
                                            "${itensSeguros.size} itens",
                                            fontSize = 11.sp,
                                            color = PratoCertoColors.TextGray
                                        )
                                    }

                                    Spacer(Modifier.height(6.dp))

                                    // Custo Total
                                    Text(
                                        "R$ ${"%.2f".format(valorTotalAlimentos)}",
                                        fontSize = 28.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color(0xFF111827)
                                    )
                                    Text(
                                        "Custo total dos alimentos selecionados",
                                        fontSize = 12.sp,
                                        color = PratoCertoColors.TextGray
                                    )

                                    Spacer(Modifier.height(14.dp))

                                    // Box com os 4 Macros
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(Color.White)
                                            .padding(14.dp),
                                        verticalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        MacroBarItem(
                                            nome = "Calorias",
                                            atual = "${caloriasTotais.toInt()} kcal",
                                            meta = if (calMeta > 0) "$calMeta kcal" else null,
                                            progresso = if (calMeta > 0) (caloriasTotais / calMeta).toFloat().coerceIn(0f, 1f) else 0f,
                                            cor = PratoCertoColors.Green
                                        )

                                        MacroBarItem(
                                            nome = "Proteínas",
                                            atual = "${"%.1f".format(proteinasTotais)}g",
                                            meta = if (protMeta > 0) "${protMeta}g" else null,
                                            progresso = if (protMeta > 0) (proteinasTotais / protMeta).toFloat().coerceIn(0f, 1f) else 0f,
                                            cor = Color(0xFF2E7D32)
                                        )

                                        MacroBarItem(
                                            nome = "Carboidratos",
                                            atual = "${"%.1f".format(carboidratosTotais)}g",
                                            meta = if (carbMeta > 0) "${carbMeta}g" else null,
                                            progresso = if (carbMeta > 0) (carboidratosTotais / carbMeta).toFloat().coerceIn(0f, 1f) else 0f,
                                            cor = Color(0xFFE65100)
                                        )

                                        MacroBarItem(
                                            nome = "Gorduras",
                                            atual = "${"%.1f".format(gordurasTotais)}g",
                                            meta = if (gordMeta > 0) "${gordMeta}g" else null,
                                            progresso = if (gordMeta > 0) (gordurasTotais / gordMeta).toFloat().coerceIn(0f, 1f) else 0f,
                                            cor = Color(0xFFF9A825)
                                        )
                                    }

                                    Spacer(Modifier.height(16.dp))

                                    // Botão 1: Salvar como lista
                                    Button(
                                        onClick = {
                                            viewModel.salvarSugestaoComoLista(
                                                onSucesso = { mensagem ->
                                                    Toast.makeText(context, mensagem, Toast.LENGTH_SHORT).show()
                                                },
                                                onErro = { erro ->
                                                    Toast.makeText(context, erro, Toast.LENGTH_SHORT).show()
                                                }
                                            )
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = PratoCertoColors.Green),
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(48.dp)
                                    ) {
                                        Icon(Icons.Default.BookmarkAdd, contentDescription = null, modifier = Modifier.size(18.dp))
                                        Spacer(Modifier.width(8.dp))
                                        Text("Salvar como lista", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    }

                                    Spacer(Modifier.height(10.dp))

                                    // Botões 2 e 3: Sugerir Cardápio e Ajustar Metas
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        OutlinedButton(
                                            onClick = {
                                                viewModel.gerarListaSugestao(onSucesso = {
                                                    viewModel.carregarCardapio()
                                                    Toast.makeText(context, "Nova sugestão gerada!", Toast.LENGTH_SHORT).show()
                                                })
                                            },
                                            shape = RoundedCornerShape(12.dp),
                                            modifier = Modifier
                                                .weight(1.2f)
                                                .height(46.dp)
                                        ) {
                                            Text("Sugerir Cardápio", fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = PratoCertoColors.TextGreen)
                                        }

                                        OutlinedButton(
                                            onClick = onIrMetas,
                                            shape = RoundedCornerShape(12.dp),
                                            modifier = Modifier
                                                .weight(1f)
                                                .height(46.dp)
                                        ) {
                                            Text("Ajustar Metas", fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = Color.DarkGray)
                                        }
                                    }

                                    Spacer(Modifier.height(8.dp))

                                    // Botão 4: Ver Lista de Compras
                                    TextButton(
                                        onClick = onIrLista,
                                        modifier = Modifier
                                            .align(Alignment.CenterHorizontally)
                                            .height(44.dp)
                                    ) {
                                        Icon(Icons.Default.ShoppingCart, contentDescription = null, tint = PratoCertoColors.TextGreen, modifier = Modifier.size(16.dp))
                                        Spacer(Modifier.width(6.dp))
                                        Text("Ver Lista de Compras", color = PratoCertoColors.TextGreen, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                                    }
                                }
                            }
                        }
                    }
                }
                else -> {
<<<<<<< HEAD
                    // Estado Vazio / Sem Sugestão
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp, vertical = 48.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .clip(RoundedCornerShape(20.dp))
                                .background(PratoCertoColors.IconBoxBg),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.MenuBook,
                                contentDescription = null,
                                tint = PratoCertoColors.TextGreen,
                                modifier = Modifier.size(36.dp)
                            )
                        }

                        Spacer(Modifier.height(16.dp))
                        Text(
                            "Nenhuma sugestão ativa",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1F2937)
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(
                            "Clique abaixo para gerar um cardápio inteligente com base nas suas metas e orçamento.",
                            color = PratoCertoColors.TextGray,
                            fontSize = 13.sp,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            lineHeight = 18.sp
                        )

                        Spacer(Modifier.height(24.dp))
                        Button(
                            onClick = {
                                viewModel.gerarListaSugestao(onSucesso = {
                                    viewModel.carregarCardapio()
                                    Toast.makeText(context, "Cardápio inteligente gerado!", Toast.LENGTH_SHORT).show()
                                })
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = PratoCertoColors.Green),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                        ) {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Gerar Cardápio Inteligente", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }

                        Spacer(Modifier.height(12.dp))
                        OutlinedButton(
                            onClick = onIrPesquisa,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                        ) {
                            Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp), tint = PratoCertoColors.TextGreen)
                            Spacer(Modifier.width(8.dp))
                            Text("Pesquisar Alimentos Manualmente", color = PratoCertoColors.TextGreen, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
=======
                    val meta = metasViewModel.meta
                    val sugestoes = metasViewModel.sugestoes

                    LazyColumn(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        item {
                            Text("Nenhum cardápio criado.", color = PratoCertoColors.TextGray, fontSize = 14.sp)
                            Spacer(Modifier.height(24.dp))
                        }

                        if (meta != null) {
                            item {
                                Box(
                                    modifier = Modifier.fillMaxWidth()
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(PratoCertoColors.IconBoxBg)
                                        .padding(16.dp)
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                                        Text("Sua Meta Diária", fontSize = 14.sp, color = PratoCertoColors.TextGreen, fontWeight = FontWeight.Bold)
                                        Spacer(Modifier.height(8.dp))
                                        Text("Calorias: ${meta.calorias_dia} kcal", fontSize = 13.sp, color = Color.DarkGray, fontWeight = FontWeight.SemiBold)
                                        Text("Proteínas: ${meta.proteinas_dia}g", fontSize = 13.sp, color = Color.DarkGray, fontWeight = FontWeight.SemiBold)
                                        
                                        Spacer(Modifier.height(12.dp))
                                        Button(
                                            onClick = onIrMetas,
                                            colors = ButtonDefaults.buttonColors(containerColor = PratoCertoColors.Green),
                                            shape = RoundedCornerShape(12.dp),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Text("Mudar Meta", fontWeight = FontWeight.SemiBold)
                                        }
                                    }
                                }
                                Spacer(Modifier.height(24.dp))
                            }

                            if (sugestoes.isNotEmpty()) {
                                item {
                                    Text("Alimentos Sugeridos", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1A1A1A))
                                    Spacer(Modifier.height(12.dp))
                                }
                                
                                items(sugestoes) { alimento ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(Color.White).padding(16.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Restaurant,
                                            contentDescription = null,
                                            tint = PratoCertoColors.TextGreen,
                                            modifier = Modifier.size(24.dp)
                                        )
                                        Spacer(Modifier.width(12.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(alimento.nome, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = Color(0xFF333333))
                                            Text("${alimento.calorias} kcal | ${alimento.proteinas}g prot", fontSize = 12.sp, color = PratoCertoColors.TextGray)
                                        }
                                    }
                                    Spacer(Modifier.height(8.dp))
                                }

                                item {
                                    Spacer(Modifier.height(16.dp))
                                    Button(
                                        onClick = { viewModel.gerarListaSugestao(onSucesso = onIrLista) },
                                        colors = ButtonDefaults.buttonColors(containerColor = PratoCertoColors.Green),
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier.fillMaxWidth().height(45.dp)
                                    ) {
                                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                                        Spacer(Modifier.width(8.dp))
                                        Text("Criar Lista Sugestão", fontWeight = FontWeight.SemiBold)
                                    }
                                }
                            }
                        } else {
                            item {
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
>>>>>>> 30af95bc5538e64c4491c68d3a9f17fbd2894dfa
                        }
                    }
                }
            }
        }

        Box(modifier = Modifier.align(Alignment.BottomCenter)) { navContent() }

        if (mostrarCriarDialog) {
            AlertDialog(
                onDismissRequest = { mostrarCriarDialog = false },
                title = { Text("Novo Cardápio", fontWeight = FontWeight.Bold) },
                text = {
                    OutlinedTextField(
                        value = tituloNovoCardapio,
                        onValueChange = { tituloNovoCardapio = it },
                        label = { Text("Nome do cardápio") },
                        placeholder = { Text("Ex: Cardápio Semanal") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            if (tituloNovoCardapio.isNotBlank()) {
                                viewModel.criarNovoCardapio(tituloNovoCardapio)
                                mostrarCriarDialog = false
                                tituloNovoCardapio = ""
                            }
                        }
                    ) {
                        Text("Criar", color = PratoCertoColors.TextGreen, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { mostrarCriarDialog = false }) {
                        Text("Cancelar", color = Color.Gray)
                    }
                }
            )
        }
    }
}

@Composable
private fun MacroBarItem(
    nome: String,
    atual: String,
    meta: String?,
    progresso: Float,
    cor: Color
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(nome, fontSize = 12.sp, color = Color.DarkGray, fontWeight = FontWeight.Medium)
            Text(
                if (meta != null) "$atual / $meta" else atual,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF222222)
            )
        }
        if (meta != null) {
            Spacer(Modifier.height(4.dp))
            LinearProgressIndicator(
                progress = { progresso },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = cor,
                trackColor = Color(0xFFEBEBEB)
            )
        }
    }
}

@Composable
private fun ItemCardapioRow(item: ItemLista, onExcluir: () -> Unit) {
    val qtd = (item.quantidadeGramas as? Double) ?: 0.0
    val preco = (item.precoCalculado as? Double) ?: 0.0
    val cal = ((item.alimentos?.calorias ?: 0.0) * (qtd / 100.0)).toInt()
    val prot = ((item.alimentos?.proteinas ?: 0.0) * (qtd / 100.0)).toInt()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(horizontal = 20.dp, vertical = 12.dp),
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
                imageVector = Icons.Default.Restaurant,
                contentDescription = null,
                tint = PratoCertoColors.TextGreen,
                modifier = Modifier.size(22.dp)
            )
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                item.alimentos?.nome ?: "Alimento",
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
                color = Color(0xFF1F2937)
            )
            Text(
                if (cal > 0 || prot > 0) "${qtd.toInt()}g  •  ${cal} kcal  •  ${prot}g prot" else "${qtd.toInt()}g",
                fontSize = 11.sp,
                color = PratoCertoColors.TextGray
            )
        }
        Text(
            "R$ ${"%.2f".format(preco)}",
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            color = PratoCertoColors.TextGreen
        )
        Spacer(Modifier.width(8.dp))
        IconButton(
            onClick = onExcluir,
            modifier = Modifier.size(40.dp) // Touch target confortável
        ) {
            Icon(
                Icons.Default.Close,
                contentDescription = "Remover",
                tint = Color.LightGray,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}