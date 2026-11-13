# language: pt
Funcionalidade: Fase 5 - Gráficos Históricos, Métricas de Performance e Novos Tipos de Transação
  Como um investidor gerenciando meu portfólio
  Eu quero visualizar gráficos de evolução patrimonial por períodos, métricas de Best/Worst Performer e registrar proventos (dividendos)
  Para acompanhar visualmente e financeiramente meus rendimentos

  Contexto:
    Dado que eu crio um usuário autenticado para testes da Fase 5
    E eu crio uma carteira de investimentos para a Fase 5

  Cenário: Identificação de Best Performer e Worst Performer no resumo analítico
    Dado que eu registro uma compra de 1.0 cotas do ativo "BTC" ao preço de 50000.00
    E eu registro uma compra de 10.0 cotas do ativo "NVDA" ao preço de 100.00
    Quando eu consulto o resumo da carteira na moeda "USD"
    Então o resumo da carteira deve conter o ativo de melhor performance "bestPerformer"
    E o resumo da carteira deve conter o ativo de pior performance "worstPerformer"

  Cenário: Registro de Dividendo financeiro e crédito em lucro realizado
    Quando eu registro o recebimento de dividendos de 500.00 do ativo "NVDA"
    Então a transação de dividendo deve ser criada com status 201
    E o lucro realizado total da carteira deve incluir o valor de 500.00

  Cenário: Consulta de gráfico de evolução patrimonial da carteira
    Dado que eu registro uma compra de 1.0 cotas do ativo "BTC" ao preço de 50000.00
    Quando eu consulto o gráfico histórico da carteira para o período "30D" na moeda "USD"
    Então a resposta do gráfico deve retornar status 200
    E o gráfico deve conter o período "30D" e a moeda "USD"
    E o gráfico deve conter uma série de pontos com timestamp e saldo positivo

  Cenário: Consulta de gráfico de evolução patrimonial do portfólio consolidado
    Dado que eu registro uma compra de 1.0 cotas do ativo "BTC" ao preço de 50000.00
    Quando eu consulto o gráfico histórico do portfólio para o período "7D" na moeda "USD"
    Então a resposta do gráfico deve retornar status 200
    E o gráfico consolidado deve conter pontos históricos e saldo atual compatível
