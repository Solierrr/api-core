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
- **Duas cadeias de segurança segregadas por `@Order`**, definidas em `security/SecurityConfig`: a chain `@Order(1)` cobre `/internal/**` com token de serviço M2M assinado via JJWT (HS256, segredo compartilhado), e a chain `@Order(2)` cobre o restante das rotas como resource server OAuth2, validando JWT de usuário (RS256) emitido pelo `api-auth` via JWKS.
- **Autorização RBAC própria**, `security/rbac/EndpointAuthorizationInterceptor` e `RbacAuthorizationService` aplicam controle de acesso por permissão/posição sobre as rotas de usuário, complementando a autenticação da chain `@Order(2)`.
- **Erros padronizados via RFC 7807**, `exception/handler/GlobalExceptionHandler` e `ProblemDetailFactory` centralizam a tradução das exceções de negócio (`BusinessException`, `ResourceNotFoundException`, `DuplicateResourceException`, entre outras) para respostas `ProblemDetail`, mesmo formato usado pelos handlers de segurança (`ProblemDetailAuthenticationEntryPoint`, `ProblemDetailAccessDeniedHandler`).
- **Cache de leitura seletivo com Redis**, `service/SupplierCacheService` e `util/CacheKeyGenerator` cacheiam apenas buscas de fornecedor elegíveis (páginas/tamanhos pequenos) e só promovem uma consulta ao cache depois que ela ultrapassa um limiar de popularidade contado via `INCR` no Redis (`redis.supplier-search.*` em `application.properties`), evitando cachear qualquer busca de baixo valor.
- **Persistência validada, não migrada pela aplicação**, `spring.jpa.hibernate.ddl-auto=validate` indica que o schema do PostgreSQL é administrado fora do ciclo de vida da aplicação {a confirmar — não há pasta de migrations (Flyway/Liquibase) no repositório}.

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
│   │   │   │   ├── rbac/          # Interceptor e serviço de autorização por permissão
│   │   │   │   └── service/       # Autenticação de token de serviço M2M
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
