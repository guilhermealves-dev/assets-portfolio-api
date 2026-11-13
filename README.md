# Assets Portfolio API 📈

REST API em **Spring Boot** para gerenciamento de portfólio de ativos financeiros multi-mercado (Criptomoedas, Ações B3, Ações Internacionais, ETFs, etc.), projetada para fornecer todos os dados analíticos necessários para um frontend tracker semelhante ao [CoinMarketCap Portfolio Tracker](https://coinmarketcap.com/portfolio-tracker/).

---

## 🛠️ Tecnologias & Arquitetura

* **Linguagem & Framework:** Java 17, Spring Boot 3.3.1
* **Persistência de Dados:** Google Firebase (Cloud Firestore)
* **Padrão Arquitetural:** Arquitetura Hexagonal (Ports & Adapters)
* **Bibliotecas:** Lombok, MapStruct, Spring Validation, Firebase Admin SDK

---

## 🚀 Requisitos e Configuração

1. **Java 17+** instalado.
2. **Maven 3.8+** (ou utilizar o wrapper `./mvnw` dentro de `app/`).
3. **Execução dos Testes:**
   ```bash
   cd app
   ./mvnw test
   ```
4. **Credenciais do Firebase:**
   * Obter a chave de conta de serviço no Firebase Console (`serviceAccountKey.json`).
   * Configurar o arquivo no diretório de recursos da aplicação (`app/src/main/resources/serviceAccountKey.json`) ou apontar a variável no `application.properties`.

---

## 🗺️ Roadmap de Desenvolvimento

Acompanhamento do progresso das funcionalidades e melhorias técnicas da API.

### 📦 Fase 0: Estrutura Base & CRUDs Iniciais
- [x] Configuração inicial do projeto Spring Boot e dependências
- [x] Estrutura da Arquitetura Hexagonal (Ports & Adapters)
- [x] Integração e configuração do Firebase Admin SDK
- [x] CRUD de Usuários (`/users`)
- [x] CRUD de Carteiras / Wallets (`/wallets`)
- [x] CRUD de Ativos / Assets (`/assets`)
- [x] CRUD de Transações / Transactions (`/transactions`)

---

### ⚙️ Fase 1: Fundação & Qualidade da API
- [x] **Documentação Interativa com Swagger/OpenAPI:** Configurar `springdoc-openapi-starter-webmvc-ui` e documentação de rotas
- [x] **Tratamento Global de Exceções:** Implementar `@RestControllerAdvice` padronizado com RFC 7807 (`ProblemDetail`)
- [x] **Ajuste de Códigos HTTP RESTful:** Padronizar `200 OK` em atualizações e `204 No Content` em deleções
- [x] **Enums Fortemente Tipados:**
  - [x] `TransactionType` (`BUY`, `SELL`, `TRANSFER_IN`, `TRANSFER_OUT`, `SWAP`, `REWARD`, `STAKING`, `DIVIDEND`, `AIRDROP`)
  - [x] `AssetType` (`CRYPTO`, `STOCK_US`, `STOCK_BR`, `ETF`, `FII`, `COMMODITY`, `CASH`)
- [x] **Paginação e Filtros nas Transações:** Suporte a filtros (`walletId`, `assetId`) e paginação (`page`, `size`)
- [x] **Otimização de Consultas Firestore:** Eliminação do gargalo N+1 nas listagens através de batch queries (`collection.get()`)
- [x] **Testes Integrados com Cucumber BDD:** Cenários em Gherkin (`.feature`) cobrindo ciclo de vida completo de Usuário, Ativo, Carteira e Transação (Criação, Consulta, Atualização, Deleção e Limpeza) com relatórios HTML gerados

---

### 📊 Fase 2: Motor de Cálculos Financeiros (Portfolio Analytics)
- [x] **Cálculo de Posições (Holdings):** Saldo consolidado por ativo ($\sum \text{compras} - \sum \text{vendas}$)
- [x] **Preço Médio Ponderado de Compra:** Cálculo dinâmico do break-even price por ativo
- [x] **Total Investido:** Montante financeiro líquido aportado por ativo e por carteira
- [x] **Lucro / Prejuízo Não Realizado (Unrealized PnL):** Valor absoluto e percentual em relação à cotação atual
- [x] **Lucro / Prejuízo Realizado (Realized PnL):** Histórico de ganhos/perdas obtidos em vendas concluídas
- [x] **Alocação Percentual da Carteira:** Percentual de cada ativo e classe em relação ao total da carteira
- [x] **Endpoints Analíticos:**
  - [x] `GET /wallets/{walletId}/summary` (Resumo geral da carteira: saldo, total investido, PnL não realizado, PnL realizado, PnL histórico, alocação por classe)
  - [x] `GET /wallets/{walletId}/holdings` (Lista de ativos em custódia com quantidade, preço médio, lucro não realizado/realizado e alocação)
  - [x] `GET /portfolio/summary` (Visão consolidada de todas as carteiras do usuário)
  - [x] `GET /portfolio/holdings` (Visão consolidada de todos os ativos em custódia do usuário)
- [x] **Testes de Aceitação BDD (Cucumber):** Cenário financeiro automatizado validando a precisão matemática de compras, vendas parciais, preço médio, lucros e resumos globais

---

### 🌐 Fase 3: Integração com Cotações de Mercado (Market Data)
- [x] **Serviço de Cotação de Criptoativos com Alta Disponibilidade:**
  - [x] Integração principal com **Binance Public API** (`/api/v3/ticker/24hr`)
  - [x] Fallback 1: **Coinbase Public API** (`/v2/prices/{pair}/spot`)
  - [x] Fallback 2: **Bybit Public API** (`/v5/market/tickers`)
- [x] **Serviço de Cotação de Ações e Ativos Tradicionais:**
  - [x] Ações Brasileiras e FIIs via **Brapi** (`/api/quote/{ticker}`)
  - [x] Ações Americanas e ETFs via **Yahoo Finance** (`/v8/finance/chart/{ticker}`)
- [x] **Camada de Cache de Alta Performance com Caffeine:**
  - [x] Cache de cotações em tempo real com TTL de 30 segundos
  - [x] Cache de taxas de câmbio com TTL de 5 minutos
  - [x] Cache de busca e autocomplete de ativos com TTL de 10 minutos
- [x] **Suporte a Multi-moeda de Exibição (USD e BRL):**
  - [x] Cotação de taxa de câmbio USD/BRL em tempo real via par USDT/BRL
  - [x] Conversão automática nos endpoints analíticos de carteira (`/wallets/{id}/summary?currency=BRL`) e portfólio (`/portfolio/summary?currency=BRL`)
- [x] **Endpoints Dedicados de Mercado:**
  - [x] `GET /market/prices/{symbol}` (Cotação individual com variação 24h e conversão)
  - [x] `GET /market/prices?symbols=BTC,ETH,SOL` (Cotações em lote / Batch tickers)
  - [x] `GET /market/exchange-rate` (Taxa cambial USD/BRL)
  - [x] `GET /market/search?query=btc` e `GET /assets/search?query=btc` (Busca e autocomplete de ativos)
- [x] **Testes de Aceitação BDD (Cucumber):** Suíte automatizada com 5 cenários cobrindo cotação em tempo real, fallback de provedores, câmbio USD/BRL, batch tickers, ações brasileiras e busca de ativos

---

### 🔒 Fase 4: Autenticação & Isolamento por Usuário (Multi-tenancy)
- [x] **Integração com Firebase Authentication:** Validação de token JWT Bearer via Spring Security Filter (`FirebaseAuthFilter`) com respostas RFC 7807 (`401 Unauthorized` e `403 Forbidden`)
- [x] **Remoção de Usuário Mockado:** Extração determinística do `uid` e perfil do usuário a partir do token da requisição (`SecurityCore`)
- [x] **Endpoint de Perfil do Usuário Autenticado:** `GET /users/me` retornando perfil e vínculo com Firestore
- [x] **Garantia Estrita de Isolamento de Dados (Multi-tenancy):**
  - [x] Carteiras (`/wallets/**`): criação vinculada ao usuário autenticado, listagem filtrada exclusivamente pelas carteiras do proprietário, e bloqueio com `403 Forbidden` para qualquer tentativa de consulta, alteração ou deleção por terceiros
  - [x] Transações (`/transactions/**`): validação de propriedade da carteira em criação, atualização, deleção e busca (`403 Forbidden` em operações cruzadas entre usuários)
  - [x] Portfólio Analytics (`/portfolio/**`): impedimento de consulta não autorizada do resumo e posições de outros usuários
- [x] **Rotas Públicas Preservadas:** Catálogo público de ativos (`GET /assets/**`), cotações e taxas de mercado (`/market/**`), documentação Swagger (`/swagger-ui/**`) e cadastro de usuários (`POST /users`)
- [x] **Controle de Acesso Baseado em Perfis (RBAC - UserRole):**
  - [x] Criação do perfil `UserRole` (`ADMIN` e `NORMAL`) com fallback automático para `NORMAL`
  - [x] Endpoint de listagem global de usuários (`GET /users`) restrito exclusivamente para `ADMIN`
  - [x] Proteção de dados cadastrais (`GET /users/{id}`, `PATCH /users/{id}`, `DELETE /users/{id}`): permitido apenas para o próprio usuário ou perfil `ADMIN`
  - [x] Endpoint administrativo de migração (`POST /admin/migrate-data`): protegido exclusivamente para `ADMIN`, normalizando e preenchendo o atributo `role: "NORMAL"` em todos os usuários legados do Firestore
- [x] **Testes de Aceitação BDD (Cucumber):** Suíte automatizada (`security_multi_tenancy.feature`) validando rejeição de requisições anônimas (401), integridade de rotas públicas, perfil próprio (`/users/me`), tentativas cruzadas de invasão (403), restrição de migração e listagem de usuários apenas para `ADMIN`

---

### 📈 Fase 5: Gráficos Históricos & Métricas Avançadas
- [x] **Evolução Patrimonial:** Reconstrução e amostragem de dados temporais de saldo, valor investido e lucro/prejuízo ao longo do tempo
- [x] **Endpoints de Gráficos de Desempenho:**
  - [x] `GET /wallets/{walletId}/chart?range=24H,7D,30D,90D,1Y,ALL&currency=USD,BRL` (Evolução histórica por carteira)
  - [x] `GET /portfolio/chart?range=24H,7D,30D,90D,1Y,ALL&currency=USD,BRL` (Evolução histórica consolidada do usuário)
- [x] **Métricas de Performance (*Best/Worst Performer*):** Identificação automática do ativo de maior e menor rentabilidade nos resumos analíticos (`/wallets/{id}/summary` e `/portfolio/summary`)
- [x] **Suporte a Rendimentos e Proventos:** Tratamento de transações do tipo `DIVIDEND` (creditando lucros realizados diretamente sem afetar quantidade de cotas em custódia) e flexibilização de `@PositiveOrZero` em transações para suportar eventos a custo zero (`AIRDROP`, `STAKING`, `DIVIDEND`)
- [x] **Padronização e Resolução de Logotipos de Ativos (Multi-Market CDNs):**
  - [x] Resolução inteligente de ícones em alta resolução (`AssetIconResolver`): CoinCap (`@2x.png`) para criptoativos, Financial Modeling Prep para ações/ETFs dos EUA, Brapi Icons (`.svg`) para B3/FIIs e avatares vetoriais
  - [x] Migração e saneamento de logotipos e dados legados no Firestore via `POST /admin/migrate-data`
- [x] **Lançamento Inteligente de Transações por Símbolo (`assetSymbol`):** Registro de compras/vendas informando apenas o ticker do ativo (ex: `BTC`, `PETR4`, `NVDA`), com resolução e cadastro automático em tempo real consultando os provedores de mercado
- [x] **Testes de Aceitação BDD (Cucumber):** Suíte automatizada com 26 cenários cobrindo gráficos temporais, cálculo de performers, transações de dividendos, isolamento multi-tenancy e registro automático de ativos

---

### 🌟 Fase 6: Funcionalidades Extras
- [x] **Watchlist de Ativos Favoritos (CoinMarketCap Tracker):**
  - [x] `POST /watchlist`: Adiciona ativos aos favoritos por `assetId` ou `assetSymbol` com resolução inteligente
  - [x] `GET /watchlist?currency=USD,BRL`: Listagem dos favoritos do usuário com cotações em tempo real, variação 24h e conversão cambial
  - [x] `DELETE /watchlist/{assetIdentifier}`: Remoção de ativos da lista de favoritos
  - [x] `GET /watchlist/check/{assetIdentifier}`: Verificação rápida de presença na watchlist
- [x] **Importação e Exportação de Transações em CSV:**
  - [x] `GET /transactions/export` e `GET /wallets/{walletId}/transactions/export`: Exportação em formato CSV padronizado (RFC 4180 com BOM UTF-8) compatível com Excel e Google Sheets
  - [x] `POST /transactions/import` e `POST /wallets/{walletId}/transactions/import`: Upload de arquivo CSV (`multipart/form-data`) para lançamento em lote com auto-resolução de ativos
- [x] **Transferências entre Carteiras & Trocas Diretas (SWAP):**
  - [x] `POST /wallets/transfer`: Transferência de ativos entre carteiras do mesmo usuário preservando o custo médio histórico sem gerar lucros ou prejuízos fictícios
  - [x] `POST /wallets/{walletId}/swap`: Troca direta entre ativos na mesma carteira, liquidando a ponta de saída com PnL realizado e abrindo a nova posição
  - [x] Bloqueio estrito de segurança multi-tenancy (`403 Forbidden`) para movimentações envolvendo carteiras de terceiros
- [x] **Testes de Aceitação BDD (Cucumber):** Suíte automatizada com 31 cenários executados com 100% de sucesso cobrindo ciclo de vida de carteiras, ativos, mercado, analytics, isolamento multi-tenancy, gráficos, watchlist, CSV e transferências

---

## 👨‍💻 Autor

Desenvolvido por **[Guilherme Alves](https://github.com/guilhermealves-dev)**

