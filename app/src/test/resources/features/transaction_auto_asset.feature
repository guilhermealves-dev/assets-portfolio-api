# language: pt
Funcionalidade: Provisionamento Automático de Ativos ao Criar Transação (Get or Create)
  Como um investidor gerenciando minha carteira
  Eu quero lançar transações informando diretamente o código de negociação do ativo (ex: NVDA, PETR4, BTC)
  Para que o sistema crie o ativo automaticamente caso ele ainda não esteja cadastrado na base local

  Contexto:
    Dado que eu crio um usuário autenticado para lançamento de transações
    E eu crio uma carteira de investimentos para o usuário

  Cenário: Auto-provisionamento de ação americana via símbolo na transação
    Quando eu registro uma compra informando o assetId "NVDA", quantidade 10.0 e preço 140.00
    Então a transação deve ser criada com status 201
    E o ativo retornado na transação deve ter símbolo "NVDA" e tipo "STOCK_US"
    E o ativo retornado na transação deve ter um identificador gerado
    E o ativo retornado na transação deve ter um ícone funcional

  Cenário: Reuso de ativo já provisionado sem duplicação no banco de dados
    Dado que uma transação com o ativo "NVDA" já foi registrada
    Quando eu registro uma segunda compra informando o assetId "NVDA", quantidade 5.0 e preço 145.00
    Então a transação deve ser criada com status 201
    E a segunda transação deve apontar para o mesmo identificador de ativo da primeira transação

  Cenário: Auto-provisionamento de ação brasileira via assetSymbol
    Quando eu registro uma compra informando o assetSymbol "PETR4", quantidade 100.0 e preço 38.50
    Então a transação deve ser criada com status 201
    E o ativo retornado na transação deve ter símbolo "PETR4" e tipo "STOCK_BR"

  Cenário: Rejeição ao tentar registrar transação sem ativo especificado
    Quando eu tento registrar uma transação sem informar assetId e sem assetSymbol
    Então a tentativa deve ser rejeitada com status 400
