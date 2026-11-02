# Prato Certo

Aplicativo mobile de planejamento de cardápios econômicos. O app ajuda o usuário a montar refeições equilibradas com base no orçamento disponível, cruzando informações nutricionais com preços de alimentos.

> "Com o orçamento que eu tenho, quais alimentos posso usar para montar meu cardápio?"

---

## Equipe

| Nome | Função |
|---|---|
| Vitória Mass | Mobile / UI-UX |
| José Jorge | Scrum Master / DevOps |
| Lucas Andrade | Back-end / Banco de Dados |
| Matheus Lampugnani | Back-end / Integrações |

---

## Estrutura do Repositório

```
prato-certo/
├── mobile/     # Aplicativo Android (Kotlin + Jetpack Compose)
└── backend/    # API REST (Node.js + Express ou Spring Boot)
```

---

## Mobile

Desenvolvido com **Kotlin** e **Jetpack Compose**.

### Pré-requisitos
- Android Studio Hedgehog ou superior
- JDK 17+
- Android SDK 26+

### Como rodar
```bash
# Abra a pasta mobile/ no Android Studio
# Aguarde o Gradle sincronizar
# Rode em um emulador ou dispositivo físico
```

---

## Backend

API REST responsável pela autenticação, regras de negócio e integração com APIs externas.

### Pré-requisitos
- Node.js 18+ (ou Java 17+ para Spring Boot)
- PostgreSQL ou Firebase Firestore

### Como rodar
```bash
cd backend
npm install
npm run dev
```

> Configure o arquivo `.env` com as variáveis necessárias (veja `.env.example`).

### Como executar os testes automatizados da API
```bash
npm test
```
*(Executa 16 testes automatizados validando autenticação, orçamentos, metas, geração de cardápio, checklists e Open Food Facts).*

### Configuração de Rede (Mobile x Backend)
- **Emulador Android**: Conecta em `http://10.0.2.2:3000/` (padrão configurado em `RetrofitInstance.kt`).
- **Dispositivo Físico via USB**: Use `http://127.0.0.1:3000/` e execute no terminal do PC: `adb reverse tcp:3000 tcp:3000`.
- **Dispositivo Físico via Wi-Fi**: Coloque o IP local do seu computador (ex: `http://192.168.x.x:3000/`).

---

## APIs Externas

| API | Uso |
|---|---|
| [TACO](https://www.cfn.org.br/index.php/taco/) | Dados nutricionais de alimentos in natura |
| [Open Food Facts](https://world.openfoodfacts.org/data) | Scanner de código de barras e preços |

---

## Funcionalidades

**Plano Gratuito**
- Cadastro e login
- Pesquisa de alimentos com dados nutricionais
- Definição de orçamento
- Montagem manual de cardápio
- Criação de lista de alimentos

**Plano Premium**
- Sugestão automática de cardápio por orçamento
- Scanner de código de barras
- Comparação entre produtos

---

## Gestão do Projeto

Tarefas e sprints gerenciados no **Jira**.

