const express = require('express');
const verificarAutenticacao = require('../middleware/auth');
const metasController = require('../controllers/metasController');

const router = express.Router();

router.get('/', verificarAutenticacao, metasController.buscarMetas);
router.post('/', verificarAutenticacao, metasController.salvarMetas);

module.exports = router;
