# language: pt
Funcionalidade: Motor de Cálculos Financeiros e Portfolio Analytics
  Como um investidor usando a API
  Eu quero que o sistema calcule automaticamente meu saldo em custódia, preço médio ponderado, lucros realizados e não realizados
  Para que eu tenha visibilidade analítica completa da performance dos meus investimentos

  Cenário: Cálculo preciso de Preço Médio, Posições (Holdings), Lucro Realizado e Lucro Não Realizado
    Dado que eu crio um usuário para testes analíticos
    E que eu crio uma carteira de investimentos analítica
    E eu cadastro um ativo com símbolo "BTC_FIN" e nome "Bitcoin Finance"
    E eu cadastro um ativo com símbolo "ETH_FIN" e nome "Ethereum Finance"

    # Operações em BTC:
    # 1. Compra 1.0 BTC a R$ 50.000
    Quando eu registro uma compra de 1.0 cotas do ativo "BTC_FIN" ao preço unitário de 50000.00
    # 2. Compra 1.0 BTC a R$ 70.000 -> Preço Médio deve ir para R$ 60.000 (Custo: 120.000 / 2.0)
    E eu registro uma compra de 1.0 cotas do ativo "BTC_FIN" ao preço unitário de 70000.00
    # 3. Venda de 0.5 BTC a R$ 80.000 -> Realiza lucro de R$ 10.000 (0.5 * (80.000 - 60.000)). Saldo restante: 1.5 BTC
    E eu registro uma venda de 0.5 cotas do ativo "BTC_FIN" ao preço unitário de 80000.00

    # Operações em ETH:
    # 4. Compra 10.0 ETH a R$ 10.000 -> Custo: 100.000
    E eu registro uma compra de 10.0 cotas do ativo "ETH_FIN" ao preço unitário de 10000.00

    # Validação dos Holdings da Carteira (GET /wallets/{id}/holdings)
    Quando eu consulto os holdings da carteira
    Então a posição do ativo "BTC_FIN" deve ter quantidade 1.5 e preço médio 60000.00
    E a posição do ativo "BTC_FIN" deve ter lucro realizado de 10000.00 e lucro não realizado de 30000.00
    E a posição do ativo "ETH_FIN" deve ter quantidade 10.0 e preço médio 10000.00

    # Validação do Resumo da Carteira (GET /wallets/{id}/summary)
    Quando eu consulto o resumo da carteira
    Então o resumo da carteira deve apresentar saldo total de 220000.00 e total investido de 190000.00
    E o lucro não realizado da carteira deve ser 30000.00 e o lucro realizado deve ser 10000.00
    E o lucro histórico total (allTimeProfitLoss) da carteira deve ser 40000.00

    # Validação dos Endpoints Globais do Portfólio (GET /portfolio/summary e GET /portfolio/holdings)
    Quando eu consulto o resumo global do portfólio
    Então o portfólio deve apresentar saldo total de 220000.00 e lucro total de 40000.00
    Quando eu consulto os holdings globais do portfólio
    Então a lista global de holdings deve conter 2 ativos sob custódia

    # Limpeza dos dados
    Quando eu finalizo e deleto todos os dados de teste analíticos
    Então todos os dados analíticos devem ter sido removidos com sucesso
