# Arquitetura do Repositório

O `api-core` segue uma arquitetura em camadas típica de uma aplicação Spring Boot, separando responsabilidades por pacote em vez de por módulo Maven, já que se trata de um serviço único (monólito modular). O fluxo de uma requisição passa por `controller` (contrato HTTP), `service` (regra de negócio), `repository` (acesso a dados via Spring Data JPA) e `domain/entity` (mapeamento objeto-relacional), com `dto` isolando o modelo exposto pela API do modelo persistido. A segurança é resolvida por duas cadeias de filtro independentes registradas em `security/SecurityConfig`, o que permite que rotas internas (`/internal/**`, M2M) e rotas de usuário final convivam no mesmo serviço com mecanismos de autenticação diferentes sem se misturar.

<p>
  <a href="https://github.com/syvixor/skills-icons">
    <img src="https://skills.syvixor.com/api/icons?i=java,springboot,springsecurity,postgresql,redis" height="48" alt="Arquitetura — api-core">
  </a>
</p>

- **Arquitetura em camadas (layered architecture)**, separação entre `controller`, `service`, `repository` e `domain`, cada camada só conhece a camada imediatamente abaixo dela.
- **DTOs dedicados por operação**, pacotes `dto/request`, `dto/response` e `dto/patch` evitam expor entidades JPA diretamente na API e permitem atualizações parciais tipadas por campo (ex. `UpdateActiveDTO`, `UpdateQuantityDTO`) em vez de um único DTO genérico de update.
- **Documentação OpenAPI desacoplada dos controllers**, cada recurso tem uma interface espelho em `openapi/*OpenApi.java` carregando as anotações do Swagger/Springdoc, mantendo os `controller/*Controller.java` livres de anotações de documentação.
- **Duas cadeias de segurança segregadas por `@Order`**, definidas em `security/SecurityConfig`: a chain `@Order(1)` cobre `/internal/**` com chave de serviço no header `X-Service-Key` (`ServiceKeyAuthFilter`; uma chave por serviço chamador em `app.service-keys.*`, comparada em tempo constante, com no mínimo 32 caracteres, e cada prefixo de rota liberado só para o serviço dono dele; o `mcp-database` lê `GET /internal/catalog/professions`, `/internal/catalog/infrastructure-offers` e `/internal/catalog/accredited-technicians` com `SERVICE_KEY_MCP_DATABASE`), e a chain `@Order(2)` cobre o restante das rotas como resource server OAuth2, validando JWT de usuário (RS256) emitido pelo `api-auth` via JWKS.
- **Autorização RBAC própria**, `security/rbac/EndpointAuthorizationInterceptor` e `RbacAuthorizationService` aplicam controle de acesso por permissão/posição sobre as rotas de usuário, complementando a autenticação da chain `@Order(2)`.
- **Erros padronizados via RFC 9457 (ProblemDetail)**, com `type` = `about:blank`, `title` = `errorCode` e `traceId` quando há span ativo; `exception/handler/GlobalExceptionHandler` e `ProblemDetailFactory` centralizam a tradução das exceções de negócio (`BusinessException`, `ResourceNotFoundException`, `DuplicateResourceException`, entre outras) para respostas `ProblemDetail`, mesmo formato usado pelos handlers de segurança (`ProblemDetailAuthenticationEntryPoint`, `ProblemDetailAccessDeniedHandler`).
- **Cache de leitura seletivo com Redis**, `service/SupplierCacheService` e `util/CacheKeyGenerator` cacheiam apenas buscas de fornecedor elegíveis (páginas/tamanhos pequenos) e só promovem uma consulta ao cache depois que ela ultrapassa um limiar de popularidade contado via `INCR` no Redis (`redis.supplier-search.*` em `application.properties`), evitando cachear qualquer busca de baixo valor.
- **Feeds de recomendação por proxy**, `GET /api/feeds/{professionals|offers|suppliers}` (`controller/recommendation`, `service/recommendation/RecommendationFeedService`, `integration/recommendation/RecommendationClient`) repassam o `api-recommendation` para qualquer usuário autenticado: a chave `X-Recommendation-Key` (`RECOMMENDATION_API_KEY`) fica só no servidor, a empresa e a unidade local vêm do usuário autenticado (a unidade informada precisa ser da empresa dele; sem informar, vale a única unidade da empresa), há uma nova tentativa em falha de rede ou 502/503/504 e cache em memória de `RECOMMENDATION_CACHE_TTL` (60 s). Sem grafo (sem `RECOMMENDATION_API_URL`, `503 FEED_UNAVAILABLE`, falha de rede ou outra indisponibilidade do `api-recommendation`, como em QA, onde o Neo4j não existe) o `RecommendationFallbackService` devolve um ranking simples calculado no Postgres, no mesmo formato do proxy (`source: "fallback"`, `context`, `items`, `warnings` avisando que o ranking é simples): ofertas ativas de fornecedores aprovados por menor custo por Wp, fornecedores por número de ofertas ativas e técnicos aprovados e ativos por avaliação, com `professionId` respeitado; só campos públicos do catálogo, `strategy` e `localUnitId` não alteram o resultado, e `RECOMMENDATION_RESULT_LIMIT` (padrão 10, máximo 50) limita os itens. Com o grafo funcionando nada muda; erros de contexto (404 e 422) continuam sendo repassados.
- **Schema versionado com Flyway**, o `api-core` é o dono do DDL do `coredb`: as migrations ficam em `src/main/resources/db/migration` (`V1__baseline.sql` com tabelas, constraints, índices, gatilhos e a procedure `generate_subscription_charge`; `V2__export_views.sql` com o schema `export` lido pelos consumidores em lote; `V3__reference_data.sql` com os dados de referência exigidos em runtime) e são aplicadas na subida; `spring.jpa.hibernate.ddl-auto=validate` confere as entidades contra o schema resultante. Cada usuário tem no máximo um vínculo de empresa (`uq_user_company_user`); várias empresas por usuário é funcionalidade futura.

```Tree do Repositório
├── .github/
│   ├── workflows/
│   │   ├── ci.yml
│   │   ├── quality.yml
│   │   ├── release.yml
│   │   └── sonarqube.yml
│   ├── CODEOWNERS
│   ├── CONTRIBUTING.md
│   └── pull_request_template.md
├── src/
│   ├── main/
│   │   ├── java/com/solaria/persistence/
│   │   │   ├── config/            # RedisConfig, RedisProperties, OpenApiConfig, WebConfig
│   │   │   ├── controller/        # Endpoints REST por recurso (Company, User, Supplier, Proposal, ...)
│   │   │   ├── domain/
│   │   │   │   ├── entity/        # Entidades JPA
│   │   │   │   └── enums/         # Enumerações de domínio (BillingStatus, ServiceStatus, ...)
│   │   │   ├── dto/
│   │   │   │   ├── patch/         # DTOs de atualização parcial por campo
│   │   │   │   ├── request/       # DTOs de entrada
│   │   │   │   └── response/      # DTOs de saída
│   │   │   ├── exception/
│   │   │   │   └── handler/       # GlobalExceptionHandler, ProblemDetailFactory
│   │   │   ├── openapi/           # Interfaces de documentação Swagger por recurso
│   │   │   ├── repository/
│   │   │   │   └── specification/ # Specifications JPA (ex. SupplierSpecification)
│   │   │   ├── security/
│   │   │   │   └── rbac/          # Interceptor e serviço de autorização por permissão
│   │   │   ├── service/           # Regras de negócio por recurso + SupplierCacheService
│   │   │   ├── util/              # CacheKeyGenerator, RegexValidator
│   │   │   └── PersistenceApplication.java
│   │   └── resources/
│   │       ├── META-INF/additional-spring-configuration-metadata.json
│   │       └── application.properties
│   └── test/
│       ├── java/com/solaria/persistence/PersistenceApplicationTests.java
│       └── resources/application.properties
├── .env.example
├── Dockerfile
├── LICENSE
├── README.md
├── ARCHITECTURE.md
├── RUNNING.md
├── pom.xml
├── sonar-project.properties
└── mvnw / mvnw.cmd
```
