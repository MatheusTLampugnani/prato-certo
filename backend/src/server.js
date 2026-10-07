const express = require('express');
require('dotenv').config();

const authRoutes = require('./routes/authRoutes');
const alimentosRoutes = require('./routes/alimentosRoutes');
const orcamentoRoutes = require('./routes/orcamentoRoutes');
const cardapioRoutes = require('./routes/cardapioRoutes');
const listasRoutes = require('./routes/listasRoutes');
const metasRoutes = require('./routes/metasRoutes');

const app = express();
app.use(express.json());

app.use('/api', alimentosRoutes);
app.use('/api/auth', authRoutes);
app.use('/api/orcamento', orcamentoRoutes);
app.use('/api/cardapio', cardapioRoutes);
app.use('/api/listas', listasRoutes);
app.use('/api/metas', metasRoutes);

const PORT = process.env.PORT || 3000;
app.listen(PORT, () => {
    console.log(`Backend rodando porta ${PORT}`);
});