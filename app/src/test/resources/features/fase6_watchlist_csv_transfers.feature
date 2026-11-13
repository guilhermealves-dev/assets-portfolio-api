# language: pt
Funcionalidade: Fase 6 - Watchlist, Importação e Exportação CSV, e Transferências entre Carteiras
  Como um investidor autenticado
  Eu quero gerenciar minha Watchlist de ativos favoritos
  Exportar e importar transações via arquivos CSV
  E realizar transferências e swaps de ativos entre minhas carteiras com segurança e isolamento multi-tenancy

  Contexto:
    Dado que a API de gestão de portfólio está em execução

  Cenário: Gerenciamento completo da Watchlist de ativos favoritos
    Dado que eu crio um usuário autenticado para testes da Fase 6
    Quando eu adiciono o ativo "BTC" à minha watchlist
    Então o ativo deve ser adicionado com status 201 e conter dados de cotação atual
    E a verificação da presença de "BTC" na watchlist deve retornar verdadeiro
    Quando eu consulto minha watchlist na moeda "BRL"
    Então a watchlist deve conter o ativo "BTC" com cotação na moeda "BRL"
    Quando eu removo o ativo "BTC" da minha watchlist
    Então a remoção deve ser realizada com status 204
    E a verificação da presença de "BTC" na watchlist deve retornar falso

  Cenário: Exportação e importação de transações via arquivo CSV
    Dado que eu crio um usuário autenticado para testes da Fase 6
    E eu crio uma carteira de investimentos "Carteira CSV" para o usuário da Fase 6
    E eu registro uma compra de 1.5 cotas do ativo "PETR4" ao preço de 38.00 na carteira
    Quando eu solicito a exportação de transações em formato CSV
    Então o arquivo retornado deve ter status 200, tipo "text/csv" e conter cabeçalho e os dados de "PETR4"
    Quando eu realizo a importação de um arquivo CSV com novas transações de "ETH" e "VALE3"
    Então o resultado da importação deve ter 2 transações processadas com sucesso e 0 falhas

  Cenário: Transferência de ativos entre carteiras do mesmo usuário
    Dado que eu crio um usuário autenticado para testes da Fase 6
    E eu crio uma carteira de investimentos "Carteira Origem" para o usuário da Fase 6
    E eu crio uma segunda carteira de investimentos "Carteira Destino" para o usuário da Fase 6
    E eu registro uma compra de 2.0 cotas do ativo "SOL" ao preço de 150.00 na carteira de origem
    Quando eu realizo uma transferência de 0.8 cotas de "SOL" da carteira de origem para a carteira de destino
    Então a transferência deve ser concluída com sucesso status 200
    E a carteira de origem deve possuir saldo de 1.2 cotas de "SOL"
    E a carteira de destino deve possuir saldo de 0.8 cotas de "SOL"

  Cenário: Rejeição de transferência de ativo para carteira de outro usuário (Multi-tenancy 403)
    Dado que eu crio um usuário autenticado para testes da Fase 6
    E eu crio uma carteira de investimentos "Carteira Usuário A" para o usuário da Fase 6
    E existe um segundo usuário no sistema com uma carteira própria "Carteira Usuário B"
    Quando o primeiro usuário tenta transferir cotas para a carteira do segundo usuário
    Então a operação de transferência deve ser rejeitada com status 403

  Cenário: Execução de troca direta de ativos (SWAP) em uma carteira
    Dado que eu crio um usuário autenticado para testes da Fase 6
    E eu crio uma carteira de investimentos "Carteira Swap" para o usuário da Fase 6
    E eu registro uma compra de 1.0 cotas do ativo "ETH" ao preço de 3000.00 na carteira
    Quando eu realizo uma troca de 0.5 cotas de "ETH" por 10.0 cotas de "SOL"
    Então a operação de swap deve retornar status 200 com transações de compra e venda geradas
    E o saldo restante de "ETH" na carteira deve ser 0.5 cotas
