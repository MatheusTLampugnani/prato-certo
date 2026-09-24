require('dotenv').config();
const jwt = require('jsonwebtoken');

const JWT_SECRET = process.env.JWT_SECRET || '472FuXFoFWtjkSZFwAerJ3ZR9O3PKnwpG/3sOh2kMwT/2yVUa3rlHJboLX4QCpOJVveVxOKki+HhMFfwoGMVHA==';

function verificarAutenticacao(req, res, next) {
    const authHeader = req.headers['authorization'];
    
    if (!authHeader) {
        return res.status(401).json({ sucesso: false, erro: 'Token de acesso não fornecido.' });
    }

    const token = authHeader.split(' ')[1]; 

    try {
        const decoded = jwt.verify(token, JWT_SECRET);
        req.usuarioId = decoded.id; 
        next();
    } catch (error) {
        return res.status(403).json({ sucesso: false, erro: 'Token inválido ou expirado.' });
    }
}

module.exports = verificarAutenticacao;