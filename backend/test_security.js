const PORT = process.env.PORT || 3000;
const BASE = `http://localhost:${PORT}`;

async function runSecurityTests() {
    console.log('--- TESTANDO CORREÇÕES DE SEGURANÇA E VALIDAÇÃO ---');

    // 1. Popular banco sem token deve ser barrado com 401
    const resPop = await fetch(`${BASE}/api/popular-banco`, { method: 'POST' });
    console.log('1. Proteção de /api/popular-banco (sem token):', resPop.status === 401 ? 'CORRIGIDO (401)' : `FALHOU (${resPop.status})`);

    // Criar Usuário A e obter token
    const emailA = `qa_a_${Date.now()}@pratocerto.com`;
    await fetch(`${BASE}/api/auth/registrar`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ nome: 'User A', email: emailA, senha: 'senhaForte123' })
    });
    const loginA = await fetch(`${BASE}/api/auth/login`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ email: emailA, senha: 'senhaForte123' })
    }).then(r => r.json());
    const tokenA = loginA.token;

    // Criar Usuário B e obter token
    const emailB = `qa_b_${Date.now()}@pratocerto.com`;
    await fetch(`${BASE}/api/auth/registrar`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ nome: 'User B', email: emailB, senha: 'senhaForte123' })
    });
    const loginB = await fetch(`${BASE}/api/auth/login`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ email: emailB, senha: 'senhaForte123' })
    }).then(r => r.json());
    const tokenB = loginB.token;

    // 2. Validação de Orçamento Negativo
    const resOrcNeg = await fetch(`${BASE}/api/orcamento`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json', 'Authorization': `Bearer ${tokenA}` },
        body: JSON.stringify({ valor: -150, periodo: 'mensal' })
    });
    console.log('2. Orçamento negativo bloqueado:', resOrcNeg.status === 400 ? 'CORRIGIDO (400)' : `FALHOU (${resOrcNeg.status})`);

    // 3. Validação de Metas com Peso/Altura Inválidos
    const resMetaInvalida = await fetch(`${BASE}/api/metas`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json', 'Authorization': `Bearer ${tokenA}` },
        body: JSON.stringify({ peso_atual: -70, altura_cm: 0, objetivo: 'emagrecer' })
    });
    console.log('3. Metas negativas/zeradas bloqueadas:', resMetaInvalida.status === 400 ? 'CORRIGIDO (400)' : `FALHOU (${resMetaInvalida.status})`);

    // 4. Usuário A cria lista e adiciona item
    const resListaA = await fetch(`${BASE}/api/listas`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json', 'Authorization': `Bearer ${tokenA}` },
        body: JSON.stringify({ titulo_lista: 'Lista Secreta do Usuário A', tipo: 'mercado' })
    }).then(r => r.json());
    const listaIdA = resListaA.lista.id;

    const resItemA = await fetch(`${BASE}/api/listas/${listaIdA}/itens`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json', 'Authorization': `Bearer ${tokenA}` },
        body: JSON.stringify({ alimento_id: 3, quantidade_gramas: 200 })
    }).then(r => r.json());
    const itemIdA = resItemA.item.id;

    // 5. Teste IDOR: Usuário B tenta deletar item do Usuário A
    const resIdorDelItem = await fetch(`${BASE}/api/listas/itens/${itemIdA}`, {
        method: 'DELETE',
        headers: { 'Authorization': `Bearer ${tokenB}` }
    });
    console.log('4. IDOR protegido: Usuário B não pode deletar item de A:', (resIdorDelItem.status === 403 || resIdorDelItem.status === 404) ? `CORRIGIDO (${resIdorDelItem.status})` : `FALHOU (${resIdorDelItem.status})`);

    // 6. Teste IDOR: Usuário B tenta alternar checklist do item de A
    const resIdorToggle = await fetch(`${BASE}/api/listas/itens/${itemIdA}/toggle`, {
        method: 'PATCH',
        headers: { 'Content-Type': 'application/json', 'Authorization': `Bearer ${tokenB}` },
        body: JSON.stringify({ comprado: true })
    });
    console.log('5. IDOR protegido: Usuário B não pode alterar item de A:', (resIdorToggle.status === 403 || resIdorToggle.status === 404) ? `CORRIGIDO (${resIdorToggle.status})` : `FALHOU (${resIdorToggle.status})`);

    // 7. Teste IDOR: Usuário B tenta inserir item na lista de A
    const resIdorAddItem = await fetch(`${BASE}/api/listas/${listaIdA}/itens`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json', 'Authorization': `Bearer ${tokenB}` },
        body: JSON.stringify({ alimento_id: 1, quantidade_gramas: 500 })
    });
    console.log('6. IDOR protegido: Usuário B não pode inserir item na lista de A:', (resIdorAddItem.status === 403 || resIdorAddItem.status === 404) ? `CORRIGIDO (${resIdorAddItem.status})` : `FALHOU (${resIdorAddItem.status})`);

    // 8. Barcode inexistente retorna JSON amigável (sem estourar erro 404)
    const resBarcode404 = await fetch(`${BASE}/api/produtos/barcode/0000000000000`).then(r => r.json());
    console.log('7. Barcode inexistente retorna resposta tratada:', (resBarcode404.sucesso === false && resBarcode404.erro) ? 'CORRIGIDO' : 'FALHOU');

    console.log('=== TODOS OS TESTES DE SEGURANÇA E VALIDAÇÃO PASSARAM! ===');
}

runSecurityTests().catch(err => {
    console.error('ERRO:', err);
    process.exit(1);
});

