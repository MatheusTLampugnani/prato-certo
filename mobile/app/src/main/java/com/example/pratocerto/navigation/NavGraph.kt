package com.example.pratocerto.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.pratocerto.ui.TelaCadastroLogin
import com.example.pratocerto.ui.TelaCardapio
import com.example.pratocerto.ui.TelaMenuPrincipal
import com.example.pratocerto.ui.TelaOrcamento
import com.example.pratocerto.ui.TelaPesquisaAlimentos

@Composable
fun NavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = "auth"
    ) {
        composable("auth") {
            TelaCadastroLogin(
                onLoginSucesso = {
                    navController.navigate("menu_principal") {
                        popUpTo("auth") { inclusive = true }
                    }
                }
            )
        }

        composable("menu_principal") {
            TelaMenuPrincipal(
                onIrCardapio = { navController.navigate("cardapio") },
                onIrOrcamento = { navController.navigate("orcamento") },
                onIrPesquisa = { navController.navigate("pesquisa") },
                onLogout = {
                    navController.navigate("auth") {
                        popUpTo("menu_principal") { inclusive = true }
                    }
                },
                onNavigate = { destino -> navController.navigate(destino) }
            )
        }

        composable("cardapio") {
            TelaCardapio(
                onVoltar = { navController.popBackStack() }
            )
        }

        composable("orcamento") {
            TelaOrcamento(
                onVoltar = { navController.popBackStack() }
            )
        }

        composable("pesquisa") {
            TelaPesquisaAlimentos(
                onVoltar = { navController.popBackStack() }
            )
        }

        composable("lista_compras") {
            TelaCardapio(
                onVoltar = { navController.popBackStack() }
            )
        }
    }
}