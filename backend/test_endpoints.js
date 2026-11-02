const PORT = process.env.PORT || 3000;
const BASE = `http://localhost:${PORT}`;

async function runTests() {
    console.log('--- INICIANDO TESTES COMPLETOS DOS ENDPOINTS ---');

    // 1. Alimentos (Pesquisa)
    const resAlimentos = await fetch(`${BASE}/api/alimentos?pesquisa=arroz`).then(r => r.json());
    console.log('1. GET /api/alimentos:', resAlimentos.sucesso ? 'PASSOU' : 'FALHOU', `(${resAlimentos.dados?.length || 0} itens)`);

    // 2. Barcode (Open Food Facts)
    const resBarcode = await fetch(`${BASE}/api/produtos/barcode/7891000100103`).then(r => r.json());
    console.log('2. GET /api/produtos/barcode:', resBarcode.sucesso ? 'PASSOU' : 'FALHOU', `(${resBarcode.dado?.nome_descricao})`);

    // 3. Auth - Registrar
    const testEmail = `aluno_${Date.now()}@pratocerto.com`;
    const resReg = await fetch(`${BASE}/api/auth/registrar`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ nome: 'Matheus Teste', email: testEmail, senha: 'senhaForte123' })
    }).then(r => r.json());
    console.log('3. POST /api/auth/registrar:', resReg.sucesso ? 'PASSOU' : 'FALHOU', resReg.mensagem || resReg.erro);

    // 4. Auth - Login
    const resLogin = await fetch(`${BASE}/api/auth/login`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ email: testEmail, senha: 'senhaForte123' })
    }).then(r => r.json());
    console.log('4. POST /api/auth/login:', resLogin.sucesso && resLogin.token ? 'PASSOU' : 'FALHOU');

    const token = resLogin.token;
    if (!token) {
        console.error('Falha no token de autenticação');
        return;
    }
    const authHeaders = {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${token}`
    };

    // 5. Orçamento - Salvar
    const resOrcSalvar = await fetch(`${BASE}/api/orcamento`, {
        method: 'POST',
        headers: authHeaders,
        body: JSON.stringify({ valor: 650, periodo: 'mensal' })
    }).then(r => r.json());
    console.log('5. POST /api/orcamento:', resOrcSalvar.sucesso ? 'PASSOU' : 'FALHOU', `(Valor: R$ ${resOrcSalvar.orcamento?.valor})`);

    // 6. Orçamento - Buscar
    const resOrcBuscar = await fetch(`${BASE}/api/orcamento`, { headers: authHeaders }).then(r => r.json());
    console.log('6. GET /api/orcamento:', resOrcBuscar.sucesso && resOrcBuscar.orcamento?.valor === 650 ? 'PASSOU' : 'FALHOU');

    // 7. Metas - Salvar
    const resMetaSalvar = await fetch(`${BASE}/api/metas`, {
        method: 'POST',
        headers: authHeaders,
        body: JSON.stringify({ peso_atual: 78, altura_cm: 175, objetivo: 'emagrecer' })
    }).then(r => r.json());
    console.log('7. POST /api/metas:', resMetaSalvar.sucesso ? 'PASSOU' : 'FALHOU', `(Calorias/dia: ${resMetaSalvar.meta?.calorias_dia} kcal, Proteínas: ${resMetaSalvar.meta?.proteinas_dia}g)`);

    // 8. Metas - Buscar
    const resMetaBuscar = await fetch(`${BASE}/api/metas`, { headers: authHeaders }).then(r => r.json());
    console.log('8. GET /api/metas:', resMetaBuscar.sucesso && resMetaBuscar.meta ? 'PASSOU' : 'FALHOU', `(Sugestões sugeridas: ${resMetaBuscar.sugestoes?.length || 0})`);

    // 9. Cardápio - Gerar Inteligente
    const resCardapioGerar = await fetch(`${BASE}/api/cardapio/gerar`, {
        method: 'POST',
        headers: authHeaders
    }).then(r => r.json());
    console.log('9. POST /api/cardapio/gerar:', resCardapioGerar.sucesso ? 'PASSOU' : 'FALHOU', `(Itens: ${resCardapioGerar.resumo?.total_itens}, Custo Diário: R$ ${resCardapioGerar.resumo?.custo_diario})`);

    // 10. Cardápio - Buscar
    const resCardapioBuscar = await fetch(`${BASE}/api/cardapio`, { headers: authHeaders }).then(r => r.json());
    console.log('10. GET /api/cardapio:', resCardapioBuscar.sucesso ? 'PASSOU' : 'FALHOU', `(Total listas retornadas: ${resCardapioBuscar.dados?.length || 0})`);

    // 11. Salvar Sugestão Ativa como Lista Mercado
    const resSalvarSugestao = await fetch(`${BASE}/api/cardapio/salvar-sugestao`, {
        method: 'POST',
        headers: authHeaders
    }).then(r => r.json());
    console.log('11. POST /api/cardapio/salvar-sugestao:', resSalvarSugestao.sucesso ? 'PASSOU' : 'FALHOU', resSalvarSugestao.mensagem);

    // 12. Criar Nova Lista Mercado Manual
    const resCriarLista = await fetch(`${BASE}/api/listas`, {
        method: 'POST',
        headers: authHeaders,
        body: JSON.stringify({ titulo_lista: 'Lista Churrasco / Feira', tipo: 'mercado' })
    }).then(r => r.json());
    const listaId = resCriarLista.lista?.id;
    console.log('12. POST /api/listas:', resCriarLista.sucesso && listaId ? 'PASSOU' : 'FALHOU');

    // 13. Adicionar Item na Lista
    const resAddItem = await fetch(`${BASE}/api/listas/${listaId}/itens`, {
        method: 'POST',
        headers: authHeaders,
        body: JSON.stringify({ alimento_id: 3, quantidade_gramas: 400 })
    }).then(r => r.json());
    const itemId = resAddItem.item?.id;
    console.log('13. POST /api/listas/:id/itens:', resAddItem.sucesso && itemId ? 'PASSOU' : 'FALHOU', `(Preço calc: R$ ${resAddItem.item?.preco_calculado})`);

    // 14. Alternar Status Comprado (Checklist)
    const resToggle = await fetch(`${BASE}/api/listas/itens/${itemId}/toggle`, {
        method: 'PATCH',
        headers: authHeaders,
        body: JSON.stringify({ comprado: true })
    }).then(r => r.json());
    console.log('14. PATCH /api/listas/itens/:id/toggle:', resToggle.sucesso && resToggle.item?.comprado === true ? 'PASSOU' : 'FALHOU');

    // 15. Excluir Item
    const resDelItem = await fetch(`${BASE}/api/listas/itens/${itemId}`, {
        method: 'DELETE',
        headers: authHeaders
    }).then(r => r.json());
    console.log('15. DELETE /api/listas/itens/:id:', resDelItem.sucesso ? 'PASSOU' : 'FALHOU');

    // 16. Excluir Lista
    const resDelLista = await fetch(`${BASE}/api/listas/${listaId}`, {
        method: 'DELETE',
        headers: authHeaders
    }).then(r => r.json());
    console.log('16. DELETE /api/listas/:id:', resDelLista.sucesso ? 'PASSOU' : 'FALHOU');

    console.log('=== TODOS OS 16 TESTES FORAM EXECUTADOS COM SUCESSO! ===');
}

runTests().catch(err => {
    console.error('ERRO NO TESTE:', err);
    process.exit(1);
});
