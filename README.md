# Prato Certo

Aplicativo mobile de planejamento alimentar e controle financeiro para supermercado. O **Prato Certo** ajuda o usuário a montar cardápios equilibrados com base no orçamento disponível e nas metas nutricionais, cruzando informações nutricionais da tabela TACO e Open Food Facts com preços médios de alimentos (CONAB).

> *"Comer bem com o que você tem."*

---

## Integrantes

| Nome | Função |
|---|---|
| Vitória Mass | Mobile / UI-UX |
| José Jorge | Scrum Master / DevOps |
| Lucas Andrade | Back-end / Banco de Dados |
| Matheus Lampugnani | Back-end / Integrações |

---

## Tecnologias Utilizadas

### Mobile (`/mobile`)
- **Kotlin** & **Jetpack Compose** (Material Design 3)
- **Retrofit 2** & **OkHttp** (com fallback automático entre `10.0.2.2` e `127.0.0.1`)
- **Navigation Compose** & **ViewModel**
- **Google ML Kit Code Scanner** (leitura de código de barras)

### Backend (`/backend`)
- **Node.js** & **Express**
- **Supabase** (PostgreSQL)
- **JWT** (`jsonwebtoken`) & **Bcrypt** (`bcryptjs`) para autenticação segura
- **Dotenv** para variáveis de ambiente

### APIs e Bases Externas
- **[TACO](https://www.cfn.org.br/index.php/taco/)**: Dados nutricionais de alimentos *in natura*.
- **[Open Food Facts](https://world.openfoodfacts.org/data)**: Consulta de produtos industrializados por código de barras.
- **CONAB**: Referência de preços médios para cálculo de orçamento.

---

## Estrutura do Repositório

```text
prato-certo/
├── backend/    # API REST em Node.js + Express + Supabase
├── mobile/     # Aplicativo Android em Kotlin + Jetpack Compose
└── README.md
```

---

## Passo a Passo para Instalar e Rodar Localmente

### 1. Clonar o repositório
```bash
git clone https://github.com/MatheusTLampugnani/prato-certo.git
cd prato-certo
```

### 2. Configurar e rodar o Backend
#### Pré-requisitos
- **Node.js 18+** instalado
- Projeto no **Supabase** com o esquema de [`backend/schema.sql`](backend/schema.sql) executado

#### Instalação e execução
```bash
cd backend
npm install
```

Crie o arquivo `.env` dentro da pasta `backend/` baseado no `backend/.env.example`:
```env
SUPABASE_URL="https://seu-projeto.supabase.co"
SUPABASE_ANON_KEY="sua-chave-supabase"
JWT_SECRET="sua-chave-jwt-segura"
PORT=3000
```

Inicie o servidor:
```bash
npm run dev
```
> O terminal exibirá: `Backend rodando porta 3000`.

Para executar os testes automatizados da API (com o servidor rodando):
```bash
npm test
```

---

### 3. Configurar e rodar o Mobile (Android)
#### Pré-requisitos
- **Android Studio** (Hedgehog ou superior)
- **JDK 11+** (ou JDK configurado no projeto)
- **Emulador Android** ou dispositivo físico com depuração USB

#### Execução
1. Abra a pasta `mobile/` no **Android Studio**.
2. Aguarde a sincronização do **Gradle** terminar.
3. Selecione um emulador Android (ou dispositivo físico) e clique em **Run (▶)**.

#### Configuração de Rede (Mobile ↔ Backend)
- **Emulador Android**: O app conecta automaticamente em `http://10.0.2.2:3000/` e possui fallback automático para `http://127.0.0.1:3000/`.
- **Dispositivo Físico via USB** (ou se precisar usar `127.0.0.1` no emulador):
  ```bash
  adb reverse tcp:3000 tcp:3000
  ```

---

## Funcionalidades Principais
- Cadastro e autenticação de usuários (JWT + Bcrypt)
- Tela Inicial com resumo diário de orçamento, macros, lista de compras e carrossel de **Dicas & Notícias Rápidas**
- Pesquisa de alimentos e scanner de código de barras (Open Food Facts)
- Definição de orçamento (diário, semanal, quinzenal ou mensal)
- Cálculo de metas nutricionais e geração inteligente de cardápio
- Checklist interativo para compras de supermercado
