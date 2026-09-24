package com.example.pratocerto.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.ListAlt
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.pratocerto.ui.theme.PratoCertoColors

@Composable
fun BottomNavFloating(
    telaAtual: String,
    onNavigate: (String) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 16.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp)
                .shadow(10.dp, RoundedCornerShape(30.dp))
                .background(PratoCertoColors.PrimaryGreen, RoundedCornerShape(30.dp)),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { onNavigate("cardapio") }) {
                Icon(
                    Icons.Default.ListAlt,
                    contentDescription = "Cardápio",
                    tint = if (telaAtual == "cardapio") Color.White else Color.White.copy(0.6f)
                )
            }

            IconButton(onClick = { onNavigate("lista_compras") }) {
                Icon(
                    Icons.Default.ShoppingCart,
                    contentDescription = "Lista",
                    tint = if (telaAtual == "lista_compras") Color.White else Color.White.copy(0.6f)
                )
            }

            Spacer(modifier = Modifier.width(56.dp))

            IconButton(onClick = { onNavigate("pesquisa") }) {
                Icon(
                    Icons.Default.Search,
                    contentDescription = "Pesquisar",
                    tint = if (telaAtual == "pesquisa") Color.White else Color.White.copy(0.6f)
                )
            }

            IconButton(onClick = { onNavigate("auth") }) {
                Icon(
                    Icons.Default.Person,
                    contentDescription = "Perfil",
                    tint = if (telaAtual == "auth") Color.White else Color.White.copy(0.6f)
                )
            }
        }

        IconButton(
            onClick = { onNavigate("menu_principal") },
            modifier = Modifier
                .offset(y = (-10).dp)
                .size(56.dp)
                .shadow(8.dp, CircleShape)
                .background(Color.White, CircleShape)
        ) {
            Icon(
                Icons.Default.Home,
                contentDescription = "Home",
                tint = PratoCertoColors.TextGreen,
                modifier = Modifier.size(28.dp)
            )
        }
    }
}