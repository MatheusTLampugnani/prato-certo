package com.example.pratocerto.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.pratocerto.model.Alimento
import com.example.pratocerto.model.CardapioItemData
import com.example.pratocerto.ui.theme.PratoCertoColors
import com.example.pratocerto.viewmodel.AlimentosViewModel
import com.example.pratocerto.viewmodel.CardapioViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TelaPesquisaAlimentos(
    onVoltar: () -> Unit,
    viewModel: AlimentosViewModel = viewModel(),
    cardapioViewModel: CardapioViewModel = viewModel()
) {
    val teclado = LocalSoftwareKeyboardController.current

    var alimentoSelecionado by remember { mutableStateOf<Alimento?>(null) }
    var quantidadeGramasTexto by remember { mutableStateOf("100") }
    var cardapioSelecionado by remember { mutableStateOf<CardapioItemData?>(null) }
    var menuCardapioEfeito by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        cardapioViewModel.carregarCardapio()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PratoCertoColors.Background)
    ) {
        TopAppBar(
            title = { Text("Pesquisar alimentos", fontWeight = FontWeight.SemiBold) },
            navigationIcon = {
                IconButton(onClick = onVoltar) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
        )

        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
            OutlinedTextField(
                value = viewModel.termoPesquisa,
                onValueChange = { viewModel.termoPesquisa = it },
                placeholder = { Text("Ex: arroz, frango, feijão…") },
                leadingIcon = { Icon(Icons.Default.Search, null, tint = PratoCertoColors.TextGray) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = {
                    viewModel.buscar(viewModel.termoPesquisa)
                    teclado?.hide()
                }),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PratoCertoColors.Green,
                    focusedLabelColor = PratoCertoColors.Green
                )
            )

            Spacer(Modifier.height(8.dp))

            Button(
                onClick = {
                    viewModel.buscar(viewModel.termoPesquisa)
                    teclado?.hide()
                },
                enabled = viewModel.termoPesquisa.isNotBlank() && !viewModel.carregando,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(46.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PratoCertoColors.Green)
            ) {
                Text(if (viewModel.carregando) "Buscando…" else "Buscar", fontWeight = FontWeight.SemiBold)
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

            viewModel.alimentos.isEmpty() && viewModel.termoPesquisa.isNotBlank() -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Nenhum alimento encontrado.", color = PratoCertoColors.TextGray)
                }
            }

            else -> {
                if (viewModel.alimentos.isNotEmpty()) {
                    Text(
                        "${viewModel.alimentos.size} resultado(s)",
                        fontSize = 12.sp,
                        color = PratoCertoColors.TextGray,
                        modifier = Modifier.padding(start = 16.dp, bottom = 4.dp)
                    )
                }
                LazyColumn {
                    items(viewModel.alimentos) { alimento ->
                        CardAlimento(
                            alimento = alimento,
                            onAdicionar = {
                                alimentoSelecionado = alimento
                                if (cardapioViewModel.listasCardapio.isNotEmpty()) {
                                    cardapioSelecionado = cardapioViewModel.listasCardapio.first()
                                }
                            }
                        )
                        HorizontalDivider(color = PratoCertoColors.Divider)
                    }
                }
            }
        }
    }

    // ── Modal de Adicionar Alimento ao Cardápio ─────────────────────────────
    alimentoSelecionado?.let { alimento ->
        AlertDialog(
            onDismissRequest = { alimentoSelecionado = null },
            title = { Text("Adicionar ao Cardápio") },
            text = {
                Column {
                    Text(alimento.nome, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Spacer(Modifier.height(12.dp))

                    // Seleção da Lista / Cardápio
                    Text("Selecione o Cardápio:", fontSize = 12.sp, color = PratoCertoColors.TextGray)
                    Spacer(Modifier.height(4.dp))

                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedButton(
                            onClick = { menuCardapioEfeito = true },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(cardapioSelecionado?.tituloLista ?: "Selecione um cardápio")
                        }

                        DropdownMenu(
                            expanded = menuCardapioEfeito,
                            onDismissRequest = { menuCardapioEfeito = false }
                        ) {
                            cardapioViewModel.listasCardapio.forEach { cardapio ->
                                DropdownMenuItem(
                                    text = { Text(cardapio.tituloLista) },
                                    onClick = {
                                        cardapioSelecionado = cardapio
                                        menuCardapioEfeito = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(12.dp))

                    // Quantidade em Gramas
                    OutlinedTextField(
                        value = quantidadeGramasTexto,
                        onValueChange = { quantidadeGramasTexto = it },
                        label = { Text("Quantidade (gramas)") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val gramas = quantidadeGramasTexto.toDoubleOrNull() ?: 100.0
                        val idLista = cardapioSelecionado?.id

                        if (idLista != null) {
                            cardapioViewModel.adicionarAlimentoNaLista(
                                idLista = idLista,
                                alimentoId = alimento.id,
                                quantidadeGramas = gramas
                            )
                            alimentoSelecionado = null
                        }
                    },
                    enabled = cardapioSelecionado != null && quantidadeGramasTexto.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(containerColor = PratoCertoColors.Green)
                ) {
                    Text("Adicionar")
                }
            },
            dismissButton = {
                TextButton(onClick = { alimentoSelecionado = null }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
private fun CardAlimento(
    alimento: Alimento,
    onAdicionar: () -> Unit
) {
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
            Text("🥦", fontSize = 20.sp)
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(alimento.nome, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = Color(0xFF222222))
            Text(
                "${alimento.calorias.toInt()} kcal  ·  ${alimento.proteinas}g prot",
                fontSize = 12.sp,
                color = PratoCertoColors.TextGray,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(
                "R$ ${"%.2f".format(alimento.preco)}",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = PratoCertoColors.TextGreen
            )
            Text("/100g", fontSize = 11.sp, color = PratoCertoColors.TextGray)
        }

        Spacer(Modifier.width(8.dp))

        IconButton(
            onClick = onAdicionar,
            colors = IconButtonDefaults.iconButtonColors(
                containerColor = PratoCertoColors.Green.copy(alpha = 0.1f),
                contentColor = PratoCertoColors.Green
            )
        ) {
            Icon(Icons.Default.Add, contentDescription = "Adicionar ao Cardápio")
        }
    }
}