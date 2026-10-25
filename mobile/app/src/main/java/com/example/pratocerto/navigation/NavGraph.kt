package com.example.pratocerto.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.pratocerto.ui.*
import com.example.pratocerto.ui.components.NavFloating
import com.example.pratocerto.ui.components.NavTab
import com.example.pratocerto.util.SessionManager

object Rotas {
    const val LOGIN     = "login"
    const val HOME      = "home"
    const val ORCAMENTO = "orcamento"
    const val PESQUISA  = "pesquisa"
    const val CARDAPIO  = "cardapio"
    const val LISTA     = "lista"
    const val METAS     = "metas"
    const val PERFIL    = "perfil"
}

@Composable
fun NavGraph() {
    val navController = rememberNavController()
    val rotaInicial = if (SessionManager.logado()) Rotas.HOME else Rotas.LOGIN

    fun navFloating(tab: NavTab): @Composable () -> Unit = {
        NavFloating(
            selecao = tab,
            onHome     = {
                navController.navigate(Rotas.HOME) {
                    popUpTo(Rotas.HOME) { saveState = true }
                    launchSingleTop = true
                    restoreState = true
                }
            },
            onPesquisa = {
                navController.navigate(Rotas.PESQUISA) {
                    launchSingleTop = true
                    restoreState = true
                }
            },
            onCardapio = {
                navController.navigate(Rotas.CARDAPIO) {
                    launchSingleTop = true
                    restoreState = true
                }
            },
            onLista    = {
                navController.navigate(Rotas.LISTA) {
                    launchSingleTop = true
                    restoreState = true
                }
            },
            onPerfil   = {
                navController.navigate(Rotas.PERFIL) {
                    launchSingleTop = true
                    restoreState = true
                }
            }
        )
    }

    NavHost(navController = navController, startDestination = rotaInicial) {
        composable(Rotas.LOGIN) {
            TelaCadastroLogin(onLoginSucesso = {
                navController.navigate(Rotas.HOME) {
                    popUpTo(Rotas.LOGIN) { inclusive = true }
                }
            })
        }

        composable(Rotas.HOME) {
            TelaHome(
                onIrOrcamento = { navController.navigate(Rotas.ORCAMENTO) },
                onIrCardapio  = { navController.navigate(Rotas.CARDAPIO) },
                onIrLista     = { navController.navigate(Rotas.LISTA) },
                onIrPesquisa  = { navController.navigate(Rotas.PESQUISA) },
                onIrPerfil    = { navController.navigate(Rotas.PERFIL) },
                navContent    = navFloating(NavTab.HOME)
            )
        }

        composable(Rotas.ORCAMENTO) {
            TelaOrcamento(onVoltar = { navController.popBackStack() })
        }

        composable(Rotas.PESQUISA) {
            TelaPesquisaAlimentos(
                onVoltar   = { navController.popBackStack() },
                navContent = navFloating(NavTab.PESQUISA)
            )
        }

        composable(Rotas.CARDAPIO) {
            TelaCardapio(
                onVoltar      = { navController.popBackStack() },
                onIrPesquisa  = { navController.navigate(Rotas.PESQUISA) },
                onIrMetas     = { navController.navigate(Rotas.METAS) },
                onIrLista     = { navController.navigate(Rotas.LISTA) },
                navContent    = navFloating(NavTab.CARDAPIO)
            )
        }

        composable(Rotas.LISTA) {
            TelaLista(
                onVoltar     = { navController.popBackStack() },
                onIrPesquisa = { navController.navigate(Rotas.PESQUISA) },
                navContent   = navFloating(NavTab.LISTA)
            )
        }

        composable(Rotas.METAS) {
            TelaMetasUsuario(onVoltar = { navController.popBackStack() })
        }

        composable(Rotas.PERFIL) {
            TelaMenuPrincipal(
                onIrOrcamento = { navController.navigate(Rotas.ORCAMENTO) },
                onIrPesquisa  = { navController.navigate(Rotas.PESQUISA) },
                onIrCardapio  = { navController.navigate(Rotas.CARDAPIO) },
                onIrMetas     = { navController.navigate(Rotas.METAS) },
                onLogout      = {
                    SessionManager.limpar()
                    navController.navigate(Rotas.LOGIN) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
    }
}