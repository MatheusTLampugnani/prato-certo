-- ====================================================================
-- Prato Certo - Esquema do Banco de Dados PostgreSQL (Supabase)
-- ====================================================================

-- 1. Usuários
CREATE TABLE IF NOT EXISTS usuarios (
    id SERIAL PRIMARY KEY,
    nome VARCHAR(100) NOT NULL,
    email VARCHAR(150) UNIQUE NOT NULL,
    senha_hash VARCHAR(255) NOT NULL,
    criado_em TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- 2. Alimentos (Base TACO e Open Food Facts)
CREATE TABLE IF NOT EXISTS alimentos (
    id SERIAL PRIMARY KEY,
    nome_descricao TEXT NOT NULL,
    calorias NUMERIC DEFAULT 0,
    proteinas NUMERIC DEFAULT 0,
    carboidratos NUMERIC DEFAULT 0,
    gorduras NUMERIC DEFAULT 0,
    porcao_base VARCHAR(20) DEFAULT '100g'
);

-- 3. Preços médios de referência (CONAB / Mercado)
CREATE TABLE IF NOT EXISTS precos (
    id SERIAL PRIMARY KEY,
    alimento_id INT REFERENCES alimentos(id) ON DELETE CASCADE,
    preco_medio NUMERIC NOT NULL,
    fonte_dado VARCHAR(50) DEFAULT 'CONAB'
);

-- 4. Orçamento definido pelo usuário
CREATE TABLE IF NOT EXISTS orcamentos (
    id SERIAL PRIMARY KEY,
    usuario_id INT REFERENCES usuarios(id) ON DELETE CASCADE,
    valor NUMERIC NOT NULL,
    periodo VARCHAR(20) DEFAULT 'mensal',
    atualizado_em TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- 5. Metas nutricionais e biométricas do usuário
CREATE TABLE IF NOT EXISTS metas_usuario (
    id SERIAL PRIMARY KEY,
    usuario_id INT REFERENCES usuarios(id) ON DELETE CASCADE,
    peso_atual NUMERIC NOT NULL,
    altura_cm NUMERIC NOT NULL,
    peso_meta NUMERIC,
    objetivo VARCHAR(30) NOT NULL, -- 'emagrecer', 'manter', 'engordar'
    calorias_dia INT,
    proteinas_dia INT,
    carboidratos_dia INT,
    gorduras_dia INT,
    criado_em TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- 6. Listas (Cardápios e Listas de Compras)
CREATE TABLE IF NOT EXISTS listas (
    id SERIAL PRIMARY KEY,
    usuario_id INT REFERENCES usuarios(id) ON DELETE CASCADE,
    titulo_lista VARCHAR(100) NOT NULL,
    tipo VARCHAR(20) DEFAULT 'mercado', -- 'cardapio' ou 'mercado'
    criado_em TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- 7. Itens das listas com status de compra
CREATE TABLE IF NOT EXISTS lista_itens (
    id SERIAL PRIMARY KEY,
    lista_id INT REFERENCES listas(id) ON DELETE CASCADE,
    alimento_id INT REFERENCES alimentos(id),
    quantidade_gramas NUMERIC NOT NULL,
    preco_calculado NUMERIC NOT NULL,
    comprado BOOLEAN DEFAULT FALSE
);
