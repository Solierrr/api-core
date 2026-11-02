<p align="center">
  <img src="../assets/readme/banners/springboot.png" alt="Spring Boot" width="100%">
</p>

# Execução local

O preparo comum está em [JAVA.md](https://github.com/Solierrr/docs-warehouse/blob/main/readme/running/JAVA.md). Este serviço usa JDK 21, Maven Wrapper, PostgreSQL e configuração do Cloudinary. Redis e os serviços externos têm efeitos diferentes conforme a operação.

## Pré-requisitos

Use JDK 21 e PostgreSQL compatível com o schema. O job de schema do CI usa PostgreSQL 16. Para os alvos do Makefile, use `make`, shell compatível com `sh` e Infisical CLI autenticado.

O banco vazio recebe as migrations Flyway durante a partida, mas um banco que já contém o schema antigo exige a adoção manual do baseline, porque o `baseline-on-migrate` está desativado. A sequência V1 a V3 não atualiza automaticamente uma instalação existente.

Para chamadas JWT, o `api-auth` precisa publicar seu JWKS. Credenciais de Cloudinary são lidas por propriedades obrigatórias. Sem PostgreSQL acessível, o Flyway e a validação JPA impedem a partida. A indisponibilidade de Redis é tratada pelo cache de busca, sem impedir o acesso direto ao banco.

## Variáveis

O `make vault` escreve o `.env` com as pastas `/database`, `/redis`, `/cloudinary`, `/google`, `/otel` e `/service-keys`, e o Spring importa esse arquivo como propriedades. O procedimento comum está no [guia de segredos](https://github.com/Solierrr/docs-warehouse/blob/main/readme/infisical/README.md).

| Nomes | Uso |
|---|---|
| `DB_POSTGRES_HOST`, `DB_POSTGRES_PORT`, `DB_POSTGRES_CORE`, `DB_POSTGRES_USER`, `DB_POSTGRES_PASSWORD` | conexão com o banco de negócio |
| `DB_POSTGRES_SSLMODE`, `DB_POSTGRES_HIKARI_MAX_POOL_SIZE` | transporte e pool de banco |
| `JWT_JWK_SET_URI`, `AUTH_SERVICE_URL` | chave pública e proxy de login do serviço de acesso |
| `SERVICE_KEY_API_AUTH`, `SERVICE_KEY_API_MESSENGER`, `SERVICE_KEY_MCP_DATABASE` | consumidores internos aceitos |
| `SERVICE_KEY` | identidade deste serviço nas chamadas externas que a usam |
| `UPSTASH_CORE_HOST`, `UPSTASH_CORE_PORT`, `UPSTASH_CORE_USERNAME`, `UPSTASH_CORE_PASSWORD` | Redis de cache |
| `CLOUDINARY_CLOUD_NAME`, `CLOUDINARY_API_KEY`, `CLOUDINARY_API_SECRET`, `CLOUDINARY_FOLDER_PREFIX` | armazenamento externo |
| `API_EMAIL_URL`, `API_EMAIL_KEY`, `WEB_APP_URL` | envio de convites e endereço usado nos links |
| `GOOGLE_REGISTRY_URL`, `REGISTRY_CONSUMER_TOKEN` | tradução de ofertas |
| `PROFESSIONAL_VALIDATION_ENABLED`, `AI_VALIDATION_BASE_URL`, `VALIDATION_SERVICE_TOKEN` | worker de validação profissional |
| `MOBILE_SERVICE_TOKEN`, `AUTH_BASE_URL`, `MOBILE_CLIENT_ID` | integração de acesso mobile |
| `RECOMMENDATION_API_URL`, `RECOMMENDATION_API_KEY`, `RECOMMENDATION_CACHE_TTL` | proxy de recomendações |
| `DOCS_ENABLED`, `CORS_ALLOWED_ORIGINS` | documentação e origens do navegador |
| `OTEL_EXPORTER_OTLP_ENDPOINT`, `SERVICE_VERSION`, `DEPLOYMENT_ENVIRONMENT` | observabilidade |

O conjunto retornado pelo vault precisa incluir as propriedades obrigatórias do serviço, como `AUTH_SERVICE_URL`.

## Comandos

Execute os comandos na raiz do checkout de desenvolvimento.

```bash
make doctor
make vault
make install
make dev
```

No Windows, os scripts do Makefile exigem ambiente compatível com Git Bash. O wrapper também pode ser executado diretamente como `mvnw.cmd` no PowerShell.

| Comando | Resultado |
|---|---|
| `make dev` | executa Spring Boot na porta 8080 |
| `make test` | executa a suíte Maven |
| `make lint` | Checkstyle com a configuração declarada no Makefile |
| `make build` | gera o pacote sem repetir os testes |
| `make openapi` | exporta o contrato pelo teste `OpenApiExportTests` |
| `make format` | informa que não há comando configurado |

O [Swagger local](http://localhost:8080/docs) usa o JWT informado no botão Authorize, e o contrato servido fica em `/openapi.json`, sem os caminhos internos, enquanto a [coleção Bruno](https://github.com/Solierrr/api-core/blob/main/http/README.md) acompanha as rotas por domínio. O token vem do fluxo de login do serviço de acesso, e o atalho `/dev/login` existe, mas é provisório e será substituído por um caminho oficial.

## Testes

O perfil padrão de testes usa H2 e criação do schema por JPA, com Flyway desligado. O CI também tem um job separado de PostgreSQL 16, que executa `ProjectPostgresIntegrationTest` para conferir entidades e schema real. Os dois caminhos cobrem aspectos diferentes.

`./mvnw test` executa a suíte padrão. O alvo de exportação usa a camada de contrato configurada pelos testes. Não é necessário iniciar as integrações externas para gerar o OpenAPI. Depois de modificar rotas ou DTOs, regenere o contrato e confira o diff.

## Verificação e problemas conhecidos

O `GET /actuator/health/liveness` verifica se o processo está no ar, o `GET /actuator/health/readiness` verifica o PostgreSQL e o `GET /actuator/info` expõe os metadados do build. O readiness não checa o Redis, a tradução, a recomendação nem o envio de e-mail.

O endpoint de e-mail local usado como padrão pelo worker de convites difere da porta local do serviço de e-mail. Configure `API_EMAIL_URL` explicitamente. O worker ainda depende da chave legada para iniciar, mesmo quando a chave de serviço está presente. A tradução também mantém sua condição de credencial legada.

O worker de validação profissional vem desligado por padrão, e sem habilitá-lo uma submissão em estado de fila não é processada automaticamente. A habilitação depende da rota profissional do serviço de validação.

Sem coletor configurado, os exportadores de observabilidade podem registrar falhas no destino local padrão. Isso é separado da saúde do PostgreSQL e das operações de negócio.
