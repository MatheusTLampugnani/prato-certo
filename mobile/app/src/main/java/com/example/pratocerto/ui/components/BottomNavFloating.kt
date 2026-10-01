package com.example.pratocerto.ui.components

import androidx.compose.foundation.background
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
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
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
            .padding(horizontal = 20.dp, vertical = 20.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(12.dp, RoundedCornerShape(30.dp))
                .clip(RoundedCornerShape(30.dp))
                .background(PratoCertoColors.NavBg)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            NavBtn(Icons.AutoMirrored.Filled.MenuBook, selecao == NavTab.CARDAPIO, onCardapio)
            NavBtn(Icons.Default.ShoppingCart, selecao == NavTab.LISTA, onLista)

            Box(
                modifier = Modifier
                    .offset(y = (-16).dp)
                    .size(56.dp)
                    .shadow(8.dp, CircleShape)
                    .clip(CircleShape)
                    .background(Color.White),
                contentAlignment = Alignment.Center
            ) {
                IconButton(onClick = onHome) {
                    Icon(
                        imageVector = Icons.Default.Home,
                        contentDescription = "Home",
                        tint = PratoCertoColors.Green,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }

            NavBtn(Icons.Default.Search, selecao == NavTab.PESQUISA, onPesquisa)
            NavBtn(Icons.Default.Person, selecao == NavTab.PERFIL, onPerfil)
        }
    }
}

@Composable
private fun NavBtn(icon: ImageVector, selecionado: Boolean, onClick: () -> Unit) {
    IconButton(onClick = onClick, modifier = Modifier.size(48.dp)) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (selecionado) Color.White else Color.White.copy(alpha = 0.5f),
            modifier = Modifier.size(26.dp)
        )
    }
}