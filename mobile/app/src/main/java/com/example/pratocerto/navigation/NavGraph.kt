package com.example.pratocerto.navigation

import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.example.pratocerto.ui.*
import com.example.pratocerto.ui.components.NavFloating
import com.example.pratocerto.ui.components.NavTab

object Rotas {
    const val LOGIN    = "login"
    const val HOME     = "home"
    const val ORCAMENTO = "orcamento"
    const val PESQUISA  = "pesquisa"
    const val CARDAPIO  = "cardapio"
    const val LISTA     = "lista"
    const val METAS     = "metas"
}

@Composable
fun NavGraph() {
    var telaAtual by remember { mutableStateOf(Rotas.LOGIN) }

    val nav: @Composable () -> Unit = {
        NavFloating(
            selecao = when (telaAtual) {
                Rotas.HOME      -> NavTab.HOME
                Rotas.PESQUISA  -> NavTab.PESQUISA
                Rotas.CARDAPIO  -> NavTab.CARDAPIO
                Rotas.LISTA     -> NavTab.LISTA
                else            -> NavTab.HOME
            },
            onHome     = { telaAtual = Rotas.HOME },
            onPesquisa = { telaAtual = Rotas.PESQUISA },
            onCardapio = { telaAtual = Rotas.CARDAPIO },
            onLista    = { telaAtual = Rotas.LISTA },
            onPerfil   = { telaAtual = Rotas.LOGIN }
        )
    }

    when (telaAtual) {
        Rotas.LOGIN -> TelaCadastroLogin(onLoginSucesso = { telaAtual = Rotas.HOME })

        Rotas.HOME -> TelaHome(
            onIrOrcamento = { telaAtual = Rotas.ORCAMENTO },
            onIrCardapio  = { telaAtual = Rotas.CARDAPIO },
            onIrLista     = { telaAtual = Rotas.LISTA },
            onIrPesquisa  = { telaAtual = Rotas.PESQUISA },
            navContent    = nav
        )

        Rotas.ORCAMENTO -> TelaOrcamento(onVoltar = { telaAtual = Rotas.HOME })

        Rotas.PESQUISA -> TelaPesquisaAlimentos(
            onVoltar   = { telaAtual = Rotas.HOME },
            navContent = nav
        )

        Rotas.CARDAPIO -> TelaCardapio(
            onVoltar      = { telaAtual = Rotas.HOME },
            onIrPesquisa  = { telaAtual = Rotas.PESQUISA },
            navContent    = nav
        )

        Rotas.LISTA -> TelaLista(
            onVoltar     = { telaAtual = Rotas.HOME },
            onIrPesquisa = { telaAtual = Rotas.PESQUISA },
            navContent   = nav
        )

        Rotas.METAS -> TelaMetasUsuario(onVoltar = { telaAtual = Rotas.HOME })
    }
}