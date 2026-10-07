const express = require('express');
const alimentosController = require('../controllers/alimentosController');

const router = express.Router();

router.get('/teste-banco', alimentosController.testeBanco);
router.post('/popular-banco', alimentosController.popularBanco);
router.get('/alimentos', alimentosController.buscarAlimentos);

module.exports = router;
