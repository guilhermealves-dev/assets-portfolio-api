# language: pt
Funcionalidade: Integração com Cotações de Mercado em Tempo Real (Market Data)
  Como um investidor ou cliente da API
  Eu quero obter cotações atualizadas em tempo real de provedores de alta disponibilidade (Binance, Coinbase, Bybit, Brapi, Yahoo)
  Para acompanhar o valor justo de mercado dos meus ativos em múltiplas moedas (USD, BRL) com alta performance via cache

  Cenário: Consulta de cotação de criptoativo em USD com dados em tempo real
    Quando eu consulto a cotação em tempo real da criptomoeda "BTC" na moeda "USD"
    Então a cotação deve retornar com status 200
    E o preço retornado em "USD" deve ser positivo
    E o provedor da cotação deve ser uma exchange reconhecida

  Cenário: Conversão cambial e cotação de criptoativo em BRL
    Quando eu consulto a taxa de câmbio de "USD" para "BRL"
    Então a taxa de câmbio deve ser retornada com valor positivo
    Quando eu consulto a cotação em tempo real da criptomoeda "ETH" na moeda "BRL"
    Então a cotação deve retornar com status 200
    E o preço retornado em "BRL" deve ser positivo

  Cenário: Consulta em lote de múltiplos ativos (Batch Tickers)
    Quando eu consulto a cotação em lote para os ativos "BTC,ETH,SOL" na moeda "USD"
    Então a resposta deve conter cotações para os 3 ativos

  Cenário: Cotação de ações brasileiras via mercado tradicional
    Quando eu consulto a cotação da ação brasileira "PETR4"
    Então a cotação deve retornar com status 200
    E a moeda da cotação deve ser "BRL"
    E o provedor da cotação deve ser "BRAPI"

  Cenário: Busca e autocomplete de ativos com cotação e ícone
    Quando eu pesquiso ativos no mercado pelo termo "BTC"
    Então a busca deve retornar pelo menos 1 ativo com símbolo "BTC"
    E o ativo encontrado deve conter nome descritivo e preço atual
