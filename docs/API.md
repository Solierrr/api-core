<p align="center">
  <img src="../assets/readme/banners/springboot.png" alt="Spring Boot" width="100%">
</p>

# API de negócio

O contrato completo está no [`openapi.yaml`](https://github.com/Solierrr/api-core/blob/main/openapi.yaml), gerado a partir das anotações e dos DTOs, e a revisão analisada tem 254 caminhos. O [Swagger de QA](https://api-core-cqfn.onrender.com/docs) usa o `/openapi.json`, e as rotas internas aparecem no arquivo exportado, mas são removidas do contrato servido em execução normal.

## Autenticação e autorização

As rotas de negócio recebem `Authorization: Bearer <access-token>` emitido pelo `api-auth`. O serviço valida assinatura, emissor e tipo de token. A presença de JWT não concede acesso a toda empresa ou projeto. O interceptor e os serviços conferem cargo, permissão e vínculo com o recurso.

As rotas internas usam `X-Service-Key`. Os escopos são separados por chamador, conforme [ARCHITECTURE.md](./ARCHITECTURE.md). A consulta interna de elegibilidade mobile ainda exige, além da chave do `api-auth`, um Bearer de serviço validado no controller. Essa exigência adicional é uma diferença do contrato de chave única entre serviços.

Os catálogos públicos de empresas, ofertas e técnicos são liberados em rotas específicas de `/api/catalog/**`. Health e info são públicos pela cadeia do Actuator. `/dev/login` continua público como proxy de conveniência, sem restrição de perfil no controller. Seu caráter provisório está registrado.

## Grupos de rotas

Os detalhes de parâmetros, campos e respostas ficam no contrato gerado.

| Grupo | Prefixos principais | Finalidade |
|---|---|---|
| Identidade e acesso | `/api/users`, `/api/persons`, `/api/user-companies`, `/api/positions`, `/api/permissions`, `/api/access-codes` | perfil, vínculos e permissões |
| Empresas e cobranças | `/api/companies`, `/api/company-plans`, `/api/subscriptions`, `/api/subscription`, `/api/charges` | empresas, planos e transações de assinatura |
| Catálogo | `/api/catalog`, `/api/brands`, `/api/brand-requests`, `/api/models`, `/api/offers`, `/api/inventories`, `/api/suppliers` | descoberta, moderação e estoque |
| Unidades | `/api/local-units`, `/api/energy-bills`, `/api/unit-specifications` | unidades consumidoras, faturas e requisitos |
| Negociação | `/api/proposals`, `/api/proposal-items`, `/api/proposal-units` | termos comerciais, alocações e aceite |
| Projetos | `/api/projects`, `/api/technical-projects` | gestão colaborativa e cadastro técnico |
| Execução | `/api/technical-services`, `/api/service-executors`, `/api/service-contracts` | serviço, executores e contrato |
| Profissionais | `/api/professional-onboarding`, `/api/professional-review`, `/api/technicians`, `/api/professions`, `/api/professional-teams` | documentos, aprovação e equipes |
| Pedidos | `/api/cart`, `/api/orders`, `/api/sales` | compra, pagamento registrado, cancelamento e vendas |
| Recomendações | `/api/feeds` | proxy e fallback de profissionais, ofertas e fornecedores |
| Interno | `/internal/users`, `/internal/projects`, `/internal/technical-projects`, `/internal/catalog` | provisionamento e consultas entre serviços |

Fotos, endereços, contatos, certificados e agenda têm recursos próprios no contrato, e as listas variam entre arrays simples e respostas paginadas, por isso o formato de cada operação deve ser conferido antes do uso.

## Exemplos

A consulta de perfil usa a identidade do token, sem receber um identificador de pessoa no caminho.

```http
GET /api/users/me HTTP/1.1
Host: localhost:8080
Authorization: Bearer <access-token>
```

O aceite é feito pela parte que está com a vez na negociação e reserva o estoque da proposta, por isso o identificador do exemplo deve ser trocado pelo de uma proposta a que o usuário tenha acesso.

```http
POST /api/proposals/<proposal-id>/acceptance HTTP/1.1
Host: localhost:8080
Authorization: Bearer <access-token>
```

O provisionamento interno é uma operação do serviço de acesso. O identificador abaixo é de demonstração. Repetir uma solicitação já provisionada retorna o cadastro correspondente, sem criar outro usuário para o mesmo `authId`.

```http
POST /internal/users HTTP/1.1
Host: localhost:8080
X-Service-Key: <chave do api-auth>
Content-Type: application/json

{"authId":"00000000-0000-4000-8000-000000000001"}
```

O fluxo completo de criação está em [USER_PROVISIONING.md](https://github.com/Solierrr/docs-warehouse/blob/main/docs/flows/USER_PROVISIONING.md). A entrada de arquivos usa multipart nas operações correspondentes, com limites por domínio descritos em [MEDIA_UPLOAD.md](../flows/MEDIA_UPLOAD.md).

## Erros

As respostas de erro usam `application/problem+json` e o [contrato comum](https://github.com/Solierrr/docs-warehouse/blob/main/docs/standards/ERRORS.md), com o `errorCode` estável, o `traceId` para correlacionar o erro com os traces e o `errors` com os detalhes de validação. O [catálogo](https://github.com/Solierrr/docs-warehouse/blob/main/docs/standards/ERROR_CODES.md) concentra os significados compartilhados.

| Código | Uso neste domínio |
|---|---|
| `UNAUTHENTICATED`, `FORBIDDEN`, `USER_NOT_PROVISIONED` | identidade, permissão e cadastro central |
| `VALIDATION_FAILED`, `MALFORMED_REQUEST` | entrada inválida, com HTTP 422 |
| `BUSINESS_RULE_VIOLATION`, `INVALID_FIELD` | regra de negócio ou campo incompatível, com HTTP 400 |
| `PROPOSAL_CONFLICT` | negociação fora da vez, estado terminal ou saldo conflitante |
| `INSUFFICIENT_STOCK` | conflito de compra, com itens insuficientes em `items` |
| `INVALID_SERVICE_TRANSITION` | mudança de estado de serviço não permitida |
| `DATA_INTEGRITY_VIOLATION`, `CONCURRENT_MODIFICATION` | integridade e concorrência |
| `STORAGE_ERROR` | falha de armazenamento externo |

A chain interna ainda pode converter uma negativa de autorização em 401. As rotas de onboarding também usam `ResponseStatusException` em alguns caminhos. Confira o contrato e a resposta efetiva antes de assumir que todos os erros do domínio seguem o mesmo mapeamento específico.
