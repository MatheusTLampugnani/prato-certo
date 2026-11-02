require('dotenv').config();
const jwt = require('jsonwebtoken');

const JWT_SECRET = process.env.JWT_SECRET;

function verificarAutenticacao(req, res, next) {
    const authHeader = req.headers['authorization'];

    if (!authHeader) {
        return res.status(401).json({ sucesso: false, erro: 'Token de acesso não fornecido.' });
    }

    const token = authHeader.startsWith('Bearer ') || authHeader.startsWith('bearer ')
        ? authHeader.slice(7).trim()
        : authHeader.trim();

    try {
        const decoded = jwt.verify(token, JWT_SECRET);
        req.usuarioId = decoded.id;
        next();
    } catch (error) {
        return res.status(403).json({ sucesso: false, erro: 'Token inválido ou expirado.' });
    }
}

module.exports = verificarAutenticacao;