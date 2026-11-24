package com.example.pratocerto.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pratocerto.model.DicaOuNoticia
import com.example.pratocerto.model.TipoAcaoDica
import com.example.pratocerto.ui.theme.PratoCertoColors

val listaDicasNoticiasPadrao = listOf(
    DicaOuNoticia(
        id = "1",
        categoria = "ECONOMIA & NUTRIÇÃO",
        titulo = "Proteína barata: Ovos e Leguminosas",
        descricao = "Combine ovos, lentilha e feijão para atingir sua meta proteica diária gastando até 60% menos do que com carnes nobres.",
        detalheCompleto = "Ovos e leguminosas (como grão-de-bico, feijão preto e lentilha) fornecem aminoácidos essenciais de alto valor biológico com custo por porção significativamente menor. Use a busca do Prato Certo para comparar o custo por grama de proteína!",
        icone = Icons.Default.Savings,
        corDestaque = Color(0xFF2E7D32),
        corFundoTag = Color(0xFFE8F5E9),
        textoBotaoAcao = "Pesquisar Alimentos",
        tipoAcao = TipoAcaoDica.IR_PESQUISA
    ),
    DicaOuNoticia(
        id = "2",
        categoria = "SAFRAS & PREÇOS",
        titulo = "Frutas e Vegetais da Época",
        descricao = "Comprar hortifrúti na safra garante alimentos mais frescos, ricos em nutrientes e com preços até 40% mais baixos.",
        detalheCompleto = "Alimentos colhidos no período correto demandam menos agrotóxicos e transporte refrigerado rápido, resultando em menor preço nas feiras e supermercados e sabor muito superior para sua dieta.",
        icone = Icons.Default.Eco,
        corDestaque = Color(0xFFE65100),
        corFundoTag = Color(0xFFFFF3E0),
        textoBotaoAcao = "Montar Cardápio",
        tipoAcao = TipoAcaoDica.IR_CARDAPIO
    ),
    DicaOuNoticia(
        id = "3",
        categoria = "GESTÃO FINANCEIRA",
        titulo = "Evite o Desperdício com Planejamento",
        descricao = "Estudos apontam que até 30% da comida comprada vai para o lixo. Defina seu orçamento e compre apenas o necessário.",
        detalheCompleto = "Com um cardápio e lista de compras pré-definidos no app, você vai ao mercado com foco exato nos itens essenciais, eliminando compras por impulso e mantendo os gastos rigorosamente dentro da sua meta estipulada.",
        icone = Icons.Default.AccountBalanceWallet,
        corDestaque = Color(0xFF1565C0),
        corFundoTag = Color(0xFFE3F2FD),
        textoBotaoAcao = "Ajustar Orçamento",
        tipoAcao = TipoAcaoDica.IR_ORCAMENTO
    ),
    DicaOuNoticia(
        id = "4",
        categoria = "HÁBITOS SAUDÁVEIS",
        titulo = "Batch Cooking: Comida Pronta, Bolso Cheio",
        descricao = "Cozinhar porções da semana de uma só vez reduz custos de gás, tempo diário e evita pedir delivery caro à noite.",
        detalheCompleto = "Tirar 2 horas no domingo para preparar arroz integral, legumes assados e proteínas cozidas garante que você não caia na tentação do fast-food quando estiver cansado, mantendo a saúde e a economia em dia.",
        icone = Icons.Default.Restaurant,
        corDestaque = Color(0xFF6A1B9A),
        corFundoTag = Color(0xFFF3E5F5),
        textoBotaoAcao = "Ver Meu Cardápio",
        tipoAcao = TipoAcaoDica.IR_CARDAPIO
    )
)

@Composable
fun SecaoCardsDicas(
    onIrOrcamento: () -> Unit,
    onIrCardapio: () -> Unit,
    onIrPesquisa: () -> Unit,
    dicas: List<DicaOuNoticia> = listaDicasNoticiasPadrao
) {
    var dicaSelecionadaModal by remember { mutableStateOf<DicaOuNoticia?>(null) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp, bottom = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Dicas & Notícias Rápidas",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1F2937)
                )
                Text(
                    text = "Alimente-se bem gastando dentro do seu plano",
                    fontSize = 12.sp,
                    color = PratoCertoColors.TextGray
                )
            }
            Icon(
                imageVector = Icons.Default.TipsAndUpdates,
                contentDescription = null,
                tint = PratoCertoColors.TextGreen,
                modifier = Modifier.size(22.dp)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        LazyRow(
            contentPadding = PaddingValues(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            items(dicas, key = { it.id }) { item ->
                CardDicaItem(
                    dica = item,
                    onClick = { dicaSelecionadaModal = item }
                )
            }
        }
    }

    // Modal de Detalhes da Notícia/Dica
    dicaSelecionadaModal?.let { dica ->
        AlertDialog(
            onDismissRequest = { dicaSelecionadaModal = null },
            shape = RoundedCornerShape(20.dp),
            containerColor = Color.White,
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(dica.corFundoTag),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = dica.icone,
                            contentDescription = null,
                            tint = dica.corDestaque,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Column {
                        Text(
                            text = dica.categoria,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = dica.corDestaque
                        )
                        Text(
                            text = dica.titulo,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1F2937)
                        )
                    }
                }
            },
            text = {
                Column(modifier = Modifier.padding(top = 4.dp)) {
                    Text(
                        text = dica.detalheCompleto,
                        fontSize = 13.sp,
                        color = Color(0xFF4B5563),
                        lineHeight = 19.sp
                    )
                }
            },
            confirmButton = {
                if (dica.textoBotaoAcao != null && dica.tipoAcao != null) {
                    Button(
                        onClick = {
                            dicaSelecionadaModal = null
                            when (dica.tipoAcao) {
                                TipoAcaoDica.IR_ORCAMENTO -> onIrOrcamento()
                                TipoAcaoDica.IR_CARDAPIO -> onIrCardapio()
                                TipoAcaoDica.IR_PESQUISA -> onIrPesquisa()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = PratoCertoColors.Green),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(dica.textoBotaoAcao, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { dicaSelecionadaModal = null }) {
                    Text("Fechar", color = PratoCertoColors.TextGray, fontSize = 12.sp)
                }
            }
        )
    }
}

@Composable
private fun CardDicaItem(
    dica: DicaOuNoticia,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(268.dp)
            .height(192.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Tag Categoria + Ícone
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(dica.corFundoTag)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = dica.categoria,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = dica.corDestaque
                    )
                }

                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(dica.corFundoTag),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = dica.icone,
                        contentDescription = null,
                        tint = dica.corDestaque,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Título
            Text(
                text = dica.titulo,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1F2937),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Descrição resumida
            Text(
                text = dica.descricao,
                fontSize = 12.sp,
                color = Color(0xFF6B7280),
                lineHeight = 16.sp,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.weight(1f))

            // Link "Ler mais"
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "Saiba como aplicar",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = dica.corDestaque
                )
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    tint = dica.corDestaque,
                    modifier = Modifier.size(13.dp)
                )
            }
        }
    }
}
