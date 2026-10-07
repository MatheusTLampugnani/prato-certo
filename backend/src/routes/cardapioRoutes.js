const express = require('express');
const verificarAutenticacao = require('../middleware/auth');
const cardapioController = require('../controllers/cardapioController');

const router = express.Router();

router.get('/', verificarAutenticacao, cardapioController.buscarCardapios);
router.post('/gerar', verificarAutenticacao, cardapioController.gerarCardapio);

module.exports = router;
