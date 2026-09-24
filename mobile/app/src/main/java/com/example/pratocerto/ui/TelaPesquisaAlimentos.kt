package com.example.pratocerto.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.foundation.lazy.items
import com.example.pratocerto.model.Alimento
import com.example.pratocerto.ui.theme.PratoCertoColors
import com.example.pratocerto.viewmodel.AlimentosViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TelaPesquisaAlimentos(
    onVoltar: () -> Unit,
    viewModel: AlimentosViewModel = viewModel()
) {
    val teclado = LocalSoftwareKeyboardController.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PratoCertoColors.Background)
    ) {
        TopAppBar(
            title = { Text("Pesquisar alimentos", fontWeight = FontWeight.SemiBold) },
            navigationIcon = {
                IconButton(onClick = onVoltar) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Voltar")
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
        )

        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
            // Campo de busca
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
                        CardAlimento(alimento)
                        HorizontalDivider(color = PratoCertoColors.Divider)
                    }
                }
            }
        }
    }
}

@Composable
private fun CardAlimento(alimento: Alimento) {
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
    }
}
