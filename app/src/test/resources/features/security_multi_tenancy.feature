# language: pt
Funcionalidade: Autenticação Firebase e Isolamento Multi-tenancy por Usuário
  Como uma aplicação de rastreamento de portfólio de ativos
  Eu quero garantir que rotas privadas exijam autenticação Bearer JWT
  E que nenhum usuário consiga visualizar, criar ou alterar dados de outros usuários (isolamento estrito)

  Cenário: Tentativa de acesso a rotas privadas sem token de autenticação deve retornar 401
    Quando uma requisição anônima sem token é feita para "/wallets"
    Então o status da resposta deve ser 401
    E a resposta deve conter detalhe de erro indicando necessidade de autenticação

  Cenário: Usuário autenticado obtém seu próprio perfil com sucesso
    Dado que o usuário "Ana Trader" está cadastrado no sistema
    Quando o usuário "Ana Trader" consulta o endpoint "/users/me"
    Então o status da resposta deve ser 200
    E os dados do perfil retornado devem corresponder a "Ana Trader"

  Cenário: Rotas públicas de mercado e catálogo de ativos são acessíveis sem token
    Quando uma requisição anônima sem token é feita para "/market/exchange-rate"
    Então o status da resposta deve ser 200
    Quando uma requisição anônima sem token é feita para "/assets"
    Então o status da resposta deve ser 200

  Cenário: Isolamento estrito de carteiras e transações entre usuários distintos (Multi-tenancy)
    Dado que o usuário "Alice Silva" possui uma carteira chamada "Carteira Segura Alice"
    E que o usuário "Bob Santos" está cadastrado no sistema
    Quando o usuário "Bob Santos" tenta consultar a carteira da "Alice Silva"
    Então o status da resposta deve ser 403
    Quando o usuário "Bob Santos" tenta atualizar o nome da carteira da "Alice Silva"
    Então o status da resposta deve ser 403
    Quando o usuário "Bob Santos" tenta deletar a carteira da "Alice Silva"
    Então o status da resposta deve ser 403
    Quando o usuário "Bob Santos" tenta criar uma transação na carteira da "Alice Silva"
    Então o status da resposta deve ser 403
    Quando o usuário "Bob Santos" tenta consultar o resumo analítico do portfólio da "Alice Silva"
    Então o status da resposta deve ser 403
    Quando o usuário "Alice Silva" lista suas carteiras
    Então a lista deve conter apenas as carteiras da "Alice Silva"

  Cenário: Controle de acesso RBAC entre perfis ADMIN e NORMAL para usuários e migrações
    Dado que o usuário "Admin Master" está cadastrado com perfil "ADMIN"
    E que o usuário "Usuario Comum" está cadastrado no sistema
    Quando o usuário "Usuario Comum" tenta executar a migração "/admin/migrate-data"
    Então o status da resposta deve ser 403
    Quando o usuário "Usuario Comum" tenta listar todos os usuários do sistema
    Então o status da resposta deve ser 403
    Quando o usuário "Usuario Comum" tenta consultar os dados cadastrais do "Admin Master"
    Então o status da resposta deve ser 403
    Quando o usuário "Usuario Comum" tenta deletar o usuário "Admin Master"
    Então o status da resposta deve ser 403
    Quando o usuário "Admin Master" executa a migração "/admin/migrate-data"
    Então o status da resposta deve ser 200
    E a migração deve reportar status de sucesso
    Quando o usuário "Admin Master" lista todos os usuários do sistema
    Então o status da resposta deve ser 200
    E a lista de usuários deve conter tanto "Admin Master" quanto "Usuario Comum"
