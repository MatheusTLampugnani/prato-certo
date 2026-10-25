const express = require('express');
const bcrypt = require('bcryptjs');
const jwt = require('jsonwebtoken');
const supabase = require('../config/db');

const router = express.Router();
const JWT_SECRET = process.env.JWT_SECRET;

router.post('/registrar', async (req, res) => {
    const { nome, email, senha } = req.body;
    if (!nome || !email || !senha) return res.status(400).json({ sucesso: false, erro: 'Preencha todos os campos.' });

    try {
        const { data: usuarioExistente } = await supabase
            .from('usuarios')
            .select('id')
            .eq('email', email)
            .maybeSingle();

        if (usuarioExistente) return res.status(400).json({ sucesso: false, erro: 'Este e-mail já está cadastrado.' });

        const salt = await bcrypt.genSalt(10);
        const senha_hash = await bcrypt.hash(senha, salt);

        const { data, error } = await supabase
            .from('usuarios')
            .insert([{ nome, email, senha_hash }])
            .select('id, nome, email')
            .single();

        if (error) throw error;
        res.status(201).json({ sucesso: true, mensagem: 'Usuário cadastrado com sucesso!', usuario: data });
    } catch (error) {
        console.error("Erro no registo:", error);
        res.status(500).json({ sucesso: false, erro: error.message });
    }
});

router.post('/login', async (req, res) => {
    const { email, senha } = req.body;
    if (!email || !senha) return res.status(400).json({ sucesso: false, erro: 'Informe e-mail e senha.' });

    try {
        const { data: usuario, error } = await supabase
            .from('usuarios')
            .select('*')
            .eq('email', email)
            .maybeSingle();

        if (error || !usuario) return res.status(401).json({ sucesso: false, erro: 'E-mail ou senha inválidos.' });

        const senhaValida = await bcrypt.compare(senha, usuario.senha_hash);
        if (!senhaValida) return res.status(401).json({ sucesso: false, erro: 'E-mail ou senha inválidos.' });

        const token = jwt.sign({ id: usuario.id, email: usuario.email }, JWT_SECRET, { expiresIn: '7d' });

        res.json({
            sucesso: true,
            mensagem: 'Login realizado com sucesso!',
            token,
            usuario: { id: usuario.id, nome: usuario.nome, email: usuario.email }
        });
    } catch (error) {
        res.status(500).json({ sucesso: false, erro: error.message });
    }
});

module.exports = router;
