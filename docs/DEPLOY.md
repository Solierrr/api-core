<p align="center">
  <img src="../assets/readme/banners/springboot.png" alt="Spring Boot" width="100%">
</p>

# Deploy

O processo comum está em [QA_RENDER.md](https://github.com/Solierrr/docs-warehouse/blob/main/readme/deploy/QA_RENDER.md) e [PROD_GKE_ARGOCD.md](https://github.com/Solierrr/docs-warehouse/blob/main/readme/deploy/PROD_GKE_ARGOCD.md). A imagem compila com JDK 21 e executa com JRE 21.

## Ambientes

Os endereços são declarados no contrato e na infraestrutura. A disponibilidade não foi testada durante esta documentação.

| Ambiente | Onde | Endereço |
|---|---|---|
| Local | processo Java | [API local](http://localhost:8080) |
| QA | Render | [API de QA](https://api-core-cqfn.onrender.com) |
| Produção declarada | GKE e Kong | [API de produção](https://core.solarianetwork.site) |

O QA, no Render, e a produção, no cluster, estão publicados com as variáveis de cada ambiente.

## Partida e banco

A imagem de runtime inclui o Infisical CLI, e o `entrypoint.sh` usa Universal Auth quando recebe as duas credenciais correspondentes e, sem elas, inicia com as variáveis já injetadas, sem que nenhum segredo entre na imagem.

A partida aplica o Flyway e valida as entidades, e num banco novo a V1 a V3 criam o schema e as referências. Num banco existente, o `baseline-on-migrate` está desativado, por isso a adoção do baseline é um procedimento manual feito antes da promoção da revisão.

PostgreSQL e as propriedades obrigatórias de integração precisam estar acessíveis na partida. Redis é usado como cache com fallback. Cloudinary recebe credenciais próprias, e as integrações HTTP leem nomes e chaves descritos em [RUNNING.md](./RUNNING.md).

## Infraestrutura declarada

O [Deployment](https://github.com/Solierrr/infra-gitops/blob/main/services/api-core/deployment.yaml) usa uma réplica, porta 8080, Secret `api-core-secrets` e ajustes de pool para reduzir conexões PostgreSQL. JWKS e e-mail têm destinos internos declarados. O [Ingress](https://github.com/Solierrr/infra-gitops/blob/main/services/api-core/ingress.yaml) aponta ao Service pelo Kong.

O manifesto ainda usa `latest` e sondas TCP para startup, liveness e readiness, que verificam a abertura da porta sem reproduzir o readiness PostgreSQL da aplicação. Os caminhos HTTP do serviço são `/actuator/health/liveness` e `/actuator/health/readiness`.

O manifesto consultado não define `DOCS_ENABLED` explicitamente, e essa configuração pode vir do Secret, cujo conteúdo não foi consultado. Desligar o Swagger também não remove o controller `/dev/login`, e a oficialização desse atalho está pendente.

## Publicação e observabilidade

Os workflows reutilizam a publicação de imagem e o release-please da organização, e a versão do `pom.xml` acompanha o OpenAPI e o build info. O fluxo geral está em [RELEASE_AND_DEPLOY.md](https://github.com/Solierrr/docs-warehouse/blob/main/docs/flows/RELEASE_AND_DEPLOY.md).

`environment-status.yml` consulta readiness no QA e verifica o cluster no job de produção. A existência do cluster não comprova a prontidão deste serviço. O endpoint OTLP, a identidade de ambiente e a versão de telemetria vêm da configuração do processo.

O rollback de imagem não desfaz migrations nem dados de negócio, e o repositório não fornece uma reversão automática do baseline, por isso a recuperação do banco segue o backup diário.
