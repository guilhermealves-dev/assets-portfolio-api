# language: pt
Funcionalidade: Gerenciamento Integrado de Portfólio de Ativos Financeiros
  Como um cliente da API de Gerenciamento de Portfólio
  Eu quero poder cadastrar e gerenciar usuários, ativos, carteiras e transações
  Para que eu possa acompanhar meus investimentos com consistência

  Cenário: Ciclo de vida completo e integrado de ponta a ponta (E2E)
    # 1. Usuário
    Dado que eu crio um novo usuário de teste com nome "Investidor Cucumber"
    Quando eu busco o usuário criado pelo identificador
    Então os dados do usuário devem ser retornados com sucesso
    E eu atualizo o nome do usuário para "Investidor Cucumber Atualizado"

    # 2. Ativo
    Dado que eu cadastro um novo ativo com símbolo "BTC_CUCUMBER", nome "Bitcoin Test" e tipo "CRYPTO"
    Quando eu busco o ativo criado pelo identificador
    Então os dados do ativo devem ser retornados com sucesso
    E a lista de ativos deve conter o ativo cadastrado
    E eu atualizo o nome do ativo para "Bitcoin Network Atualizado"

    # 3. Carteira
    Dado que eu crio uma nova carteira chamada "Carteira Fria Cripto" vinculada ao usuário
    Quando eu busco a carteira criada pelo identificador
    Então os dados da carteira devem ser retornados com sucesso
    E a lista de carteiras deve conter a carteira criada
    E eu atualizo o nome da carteira para "Carteira Fria Cripto VIP"

    # 4. Transação
    Dado que eu registro uma transação de "BUY" com 2.5 unidades a 65000.00
    Quando eu busco a transação criada pelo identificador
    Então os dados da transação devem ser retornados com sucesso
    E eu consulto as transações filtrando pela carteira criada
    E eu atualizo a transação com novo preço 68000.00 e montante 170000.00

    # 5. Limpeza / Exclusão de dados (Clean up)
    Quando eu deleto a transação de teste
    Então a transação não deve mais ser encontrada
    Quando eu deleto a carteira de teste
    Então a carteira não deve mais ser encontrada
    Quando eu deleto o ativo de teste
    Então o ativo não deve mais ser encontrado
    Quando eu deleto o usuário de teste
    Então o usuário não deve mais ser encontrado
