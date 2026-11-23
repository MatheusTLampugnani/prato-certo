package com.example.pratocerto.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.example.pratocerto.ui.theme.PratoCertoColors

enum class NavTab { HOME, PESQUISA, CARDAPIO, LISTA, PERFIL }

@Composable
fun NavFloating(
    selecao: NavTab,
    onHome: () -> Unit,
    onPesquisa: () -> Unit,
    onCardapio: () -> Unit,
    onLista: () -> Unit,
    onPerfil: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 24.dp, vertical = 12.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        // Barra principal arredondada e suave
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(
                    elevation = 16.dp,
                    shape = RoundedCornerShape(32.dp),
                    spotColor = Color(0x3D46531B),
                    ambientColor = Color(0x24000000)
                )
                .clip(RoundedCornerShape(32.dp))
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            Color(0xFF86943C),
                            Color(0xFF7A8834)
                        )
                    )
                )
                .padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Lado Esquerdo: Cardápio e Lista
            NavBtn(
                icon = Icons.AutoMirrored.Filled.MenuBook,
                label = "Cardápio",
                selecionado = selecao == NavTab.CARDAPIO,
                onClick = onCardapio
            )

            NavBtn(
                icon = Icons.Default.ShoppingCart,
                label = "Lista",
                selecionado = selecao == NavTab.LISTA,
                onClick = onLista
            )

            // Botão Central de Destaque: HOME
            val homeScale by animateFloatAsState(
                targetValue = if (selecao == NavTab.HOME) 1.05f else 1.0f,
                animationSpec = spring(dampingRatio = 0.6f),
                label = "homeScale"
            )

            Box(
                modifier = Modifier
                    .offset(y = (-12).dp)
                    .scale(homeScale)
                    .size(56.dp)
                    .shadow(
                        elevation = 10.dp,
                        shape = CircleShape,
                        spotColor = Color(0x40000000)
                    )
                    .clip(CircleShape)
                    .background(Color.White)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onHome
                    ),
                contentAlignment = Alignment.Center
            ) {
                // Pequeno anel interno decorativo se estiver selecionado
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(if (selecao == NavTab.HOME) Color(0xFFF1F5E0) else Color.Transparent),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Home,
                        contentDescription = "Home",
                        tint = if (selecao == NavTab.HOME) PratoCertoColors.TextGreen else Color(0xFF63732B),
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            // Lado Direito: Pesquisa e Perfil
            NavBtn(
                icon = Icons.Default.Search,
                label = "Pesquisa",
                selecionado = selecao == NavTab.PESQUISA,
                onClick = onPesquisa
            )

            NavBtn(
                icon = Icons.Default.Person,
                label = "Perfil",
                selecionado = selecao == NavTab.PERFIL,
                onClick = onPerfil
            )
        }
    }
}

@Composable
private fun NavBtn(
    icon: ImageVector,
    label: String,
    selecionado: Boolean,
    onClick: () -> Unit
) {
    val bgIndicatorColor by animateColorAsState(
        targetValue = if (selecionado) Color.White.copy(alpha = 0.22f) else Color.Transparent,
        label = "bgIndicator"
    )
    val iconColor by animateColorAsState(
        targetValue = if (selecionado) Color.White else Color.White.copy(alpha = 0.65f),
        label = "iconColor"
    )

    Box(
        modifier = Modifier
            .size(46.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(bgIndicatorColor)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = iconColor,
            modifier = Modifier.size(23.dp)
        )
    }
}
