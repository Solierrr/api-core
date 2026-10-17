# Rodando o Projeto Localmente

Este repositório é Java + Maven (Spring Boot). O processo local é sempre o mesmo: clonar, abrir na IDE, injetar as variáveis de ambiente via Infisical e subir a aplicação com o `mvnw`. Antes de iniciar, verifique a seção de impedimentos abaixo — o `api-core` depende de um PostgreSQL, de um Redis (Upstash) e, para autenticação de usuário final, do serviço `api-auth` publicando o JWKS.

<p>
  <a href="https://github.com/syvixor/skills-icons">
    <img src="https://skills.syvixor.com/api/icons?i=java,springboot,spring,postgresql,redis,github" height="48" alt="Rodando o Projeto — api-core">
  </a>
</p>

## Possíveis Impedimentos

- **JDK 21 instalado localmente**, a mesma versão usada no `Dockerfile` do repositório (`eclipse-temurin:21`) — rodar fora do container exige essa versão instalada e configurada como `JAVA_HOME`.
- **Secrets via Infisical**, a fonte de verdade das variáveis de ambiente é o [Infisical](https://infisical.com), não um `.env` local — veja `.env.example` só para saber quais variáveis existem (banco Postgres compartilhado com o `api-auth`, credenciais Redis/Upstash, segredos de JWT, Cloudinary, google-registry e OpenTelemetry).
- **PostgreSQL acessível**, `spring.datasource.url` aponta por padrão para `localhost:5432/dbsolier` (`DB_POSTGRES_*`); sem um Postgres rodando localmente ou credenciais válidas via Infisical, a aplicação sobe mas as requisições que tocam o banco falham. `spring.jpa.hibernate.ddl-auto=validate` — a aplicação **não cria nem migra o schema**, ele precisa já existir {a confirmar — não há Flyway/Liquibase no repositório, então a origem do schema local não está documentada aqui}.
- **Redis/Upstash acessível**, `spring.data.redis.url` aponta por padrão para `localhost:6379` (`UPSTASH_CORE_*`); sem Redis, o cache de busca de fornecedores (`SupplierCacheService`) falha silenciosamente (os erros são apenas logados) e a aplicação continua respondendo sem cache.
- **`api-auth` publicando o JWKS**, rotas de usuário final (tudo fora de `/internal/**`) são validadas como resource server OAuth2 contra `app.jwt.jwk-set-uri` (padrão `http://localhost:8081/.well-known/jwks.json`) — sem o `api-auth` rodando localmente nesse endereço, qualquer chamada autenticada de usuário retorna 401.
- **`SERVICE_JWT_SECRET` com no mínimo 32 bytes**, exigido pelas rotas `/internal/**` (autenticação M2M via JJWT/HS256); sem essa variável a aplicação não sobe.

## Instalação do Projeto

### Iniciando o repositório com o Github

<p>
  <a href="https://github.com/syvixor/skills-icons">
    <img src="https://skills.syvixor.com/api/icons?i=github,intellij" height="48" alt="Frameworks">
  </a>
</p>

Clone o repositório e abra no IntelliJ IDEA.

```Comandos para clonar o repositório
git clone https://github.com/Solierrr/api-core.git
cd ./api-core
idea .
```

### Instalando dependências necessárias para rodar o projeto localmente

<p>
  <a href="https://github.com/syvixor/skills-icons">
    <img src="https://skills.syvixor.com/api/icons?i=maven,apache" height="48" alt="Frameworks">
  </a>
</p>

Use sempre o wrapper (`mvnw`/`mvnw.cmd`) em vez de um Maven instalado globalmente, para garantir a mesma versão usada no CI.

```Comandos para instalação de dependências
./mvnw dependency:go-offline
```

### Subindo a aplicação com as variáveis do Infisical

<p>
  <a href="https://github.com/syvixor/skills-icons">
    <img src="https://skills.syvixor.com/api/icons?i=springboot" height="48" alt="Runtime">
  </a>
</p>

Instale o [Infisical CLI](https://infisical.com/docs/cli/overview), autentique e injete as variáveis de ambiente em runtime — não é necessário copiar `.env.example` para `.env` manualmente.

```Comandos para subir o serviço
infisical login
infisical init
infisical run --env=dev --path=/ --recursive -- ./mvnw spring-boot:run
```

Com o serviço no ar, a documentação interativa da API fica disponível em `http://localhost:8080/swagger-ui.html`.

### Rodando os testes

Os testes usam H2 em memória (`src/test/resources/application.properties`), não dependem de PostgreSQL, Redis ou Infisical.

```Comandos para rodar os testes
./mvnw test
```

## Traducoes e documentos

As traducoes de ofertas usam `GOOGLE_REGISTRY_URL` e `REGISTRY_CONSUMER_TOKEN` (o mesmo token consumidor configurado no registry). `GOOGLE_REGISTRY_TIMEOUT_SECONDS` limita a espera. A chave Google Translation fica somente no google-registry.

As fotos de modelos usam as credenciais `CLOUDINARY_*` existentes. Documentos profissionais entram temporariamente no banco, seguem para ai-validation e so sao arquivados como assets autenticados depois da aprovacao. A leitura continua autenticada pelo api-core, com verificacao SHA-256 e sem expor URLs assinadas.

Antes de iniciar com `JPA_DDL_AUTO=validate`, aplicar as migrations do database-console conforme seu README. O antigo deeplink.sql nao aplica mais SQL: onboarding/documentos pertencem a V12 e integridade a V13. Bytes legados continuam legiveis.

Ofertas gravam um job de traducao na mesma transacao dos textos. O worker usa lease de 300 segundos, ate 5 tentativas e backoff de 30 segundos ate 1 hora; configuracoes `app.offer-translation.poll-ms`, `max-attempts` e `lease-seconds` podem ajustar esses limites. Falhas terminais permanecem registradas; editar os textos agenda um novo job; alterar apenas preco/estoque nao reinicia trabalho pago, mesmo apos falha. Registros de jobs nao sao apagados automaticamente.

`PATCH /api/offers/{id}/status` recebe, por exemplo, `{"status":"PAUSED"}`; aceita ACTIVE, PAUSED ou CLOSED, sendo CLOSED definitivo. `GET /api/offers/{id}/history` retorna a trilha autorizada. Pausar nao altera estoque ou textos; expiração continua pela data existente. Novos itens de proposta exigem oferta ativa; propostas anteriores nao sao encerradas automaticamente. Ofertas com historico nao podem ser excluidas: o encerramento preserva a auditoria.

Equipes usam POST `/api/professional-teams`, PUT `/api/professional-teams/{id}`, GET `/api/professional-teams/company/{companyId}` e GET publico `/api/catalog/teams`. O payload contem companyId, name, responsibleId, technicianIds e active. O responsavel deve constar nos integrantes; integrantes devem estar aprovados, ativos e vinculados a empresa. Um tecnico pode participar de varias equipes. Desativacao ocorre pelo PUT; nao ha exclusao destrutiva ou convites neste contrato.
