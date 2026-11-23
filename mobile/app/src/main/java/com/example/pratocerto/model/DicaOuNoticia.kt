package com.example.pratocerto.model

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

data class DicaOuNoticia(
    val id: String,
    val categoria: String,
    val titulo: String,
    val descricao: String,
    val detalheCompleto: String,
    val icone: ImageVector,
    val corDestaque: Color,
    val corFundoTag: Color,
    val textoBotaoAcao: String? = null,
    val tipoAcao: TipoAcaoDica? = null
)

enum class TipoAcaoDica {
    IR_ORCAMENTO,
    IR_CARDAPIO,
    IR_PESQUISA
}
