const express = require('express');
const verificarAutenticacao = require('../middleware/auth');
const orcamentoController = require('../controllers/orcamentoController');

const router = express.Router();

router.post('/', verificarAutenticacao, orcamentoController.salvarOrcamento);
router.get('/', verificarAutenticacao, orcamentoController.buscarOrcamento);

module.exports = router;
