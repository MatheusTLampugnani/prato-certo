const express = require('express');
const verificarAutenticacao = require('../middleware/auth');
const listasController = require('../controllers/listasController');

const router = express.Router();

router.post('/', verificarAutenticacao, listasController.criarLista);
router.post('/:id/itens', verificarAutenticacao, listasController.adicionarItem);
router.get('/:id', verificarAutenticacao, listasController.buscarListaPorId);

module.exports = router;
