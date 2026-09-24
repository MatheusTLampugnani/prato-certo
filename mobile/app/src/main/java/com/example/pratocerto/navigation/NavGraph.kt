package com.example.pratocerto.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.pratocerto.ui.*

object Rotas {
    const val LOGIN = "login"
    const val MENU = "menu"
    const val ORCAMENTO = "orcamento"
    const val PESQUISA = "pesquisa"
    const val CARDAPIO = "cardapio"
}

@Composable
fun NavGraph() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = Rotas.LOGIN) {

        composable(Rotas.LOGIN) {
            TelaCadastroLogin(
                onLoginSucesso = {
                    navController.navigate(Rotas.MENU) {
                        popUpTo(Rotas.LOGIN) { inclusive = true }
                    }
                }
            )
        }

        composable(Rotas.MENU) {
            TelaMenuPrincipal(
                onIrOrcamento = { navController.navigate(Rotas.ORCAMENTO) },
                onIrPesquisa = { navController.navigate(Rotas.PESQUISA) },
                onIrCardapio = { navController.navigate(Rotas.CARDAPIO) },
                onLogout = {
                    navController.navigate(Rotas.LOGIN) {
                        popUpTo(Rotas.MENU) { inclusive = true }
                    }
                }
            )
        }

        composable(Rotas.ORCAMENTO) {
            TelaOrcamento(onVoltar = { navController.popBackStack() })
        }

        composable(Rotas.PESQUISA) {
            TelaPesquisaAlimentos(onVoltar = { navController.popBackStack() })
        }

        composable(Rotas.CARDAPIO) {
            TelaCardapio(onVoltar = { navController.popBackStack() })
        }
    }
}
