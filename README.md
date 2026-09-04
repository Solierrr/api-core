<div align="center">

# api-core

API principal de persistência da Solaria — cadastro de fornecedores/planos, upload de mídia e o núcleo de dados que outros serviços internos consomem.

<p>
  <a href="https://github.com/Solierrr/api-core/releases">
    <img alt="Release" src="https://img.shields.io/github/v/release/Solierrr/api-core?style=flat-square">
  </a>
  <a href="https://github.com/Solierrr/api-core/blob/main/LICENSE">
    <img alt="License" src="https://img.shields.io/badge/license-MIT-blue?style=flat-square">
  </a>
  <a href="https://github.com/Solierrr/api-core/actions/workflows/release.yml">
    <img alt="Build" src="https://img.shields.io/github/actions/workflow/status/Solierrr/api-core/release.yml?branch=main&style=flat-square">
  </a>
</p>

<p>
  <img src="https://skillicons.dev/icons?i=java,spring,postgres,redis,docker" alt="Stack" />
</p>

</div>

## Índice

- [Sobre](#sobre)
- [Stack](#stack)
- [Como rodar local](#como-rodar-local)
- [Variáveis de ambiente](#variáveis-de-ambiente)
- [Testes](#testes)
- [Ambientes e deploy](#ambientes-e-deploy)
- [Contribuindo](#contribuindo)
- [Licença](#licença)

## Sobre

O `api-core` é o serviço de persistência principal da Solaria: expõe o
cadastro de fornecedores e planos, upload de mídia (via Cloudinary) e
tradução (via Google Translate). Valida JWT de usuário emitido pelo
`api-auth` (JWKS) e expõe tokens de serviço M2M pra outros serviços internos
consumirem seus dados — hoje o `api-recommendation` lê o Postgres dele
somente-leitura pra montar recomendações.

## Stack

| Camada | Tecnologia |
|---|---|
| Linguagem | Java 21 |
| Framework | Spring Boot (Web, Data JPA, Security, Validation) |
| Banco | PostgreSQL |
| Cache | Redis (Upstash) |
| Mídia | Cloudinary |
| Observabilidade | OpenTelemetry -> Collector |
| Deploy | Docker + GKE (prod) / Render (QA) |

## Como rodar local

Pré-requisitos: JDK 21, [Infisical CLI](https://infisical.com/docs/cli/overview).

1. Instale o Infisical CLI e rode `infisical login`.
2. Na raiz do repo, rode `infisical init` (uma vez só) e escolha o projeto do time.
3. Suba o serviço com:

   ```bash
   infisical run --env=dev --path=/ --recursive -- ./mvnw spring-boot:run
   ```

Não é preciso copiar `.env.example` pra `.env` manualmente — o Infisical injeta as variáveis em runtime.

## Variáveis de ambiente

Fonte de verdade é o [Infisical](https://infisical.com) (ambientes `dev`/`qa`/`prod`). O [`.env.example`](.env.example) deste repo documenta quais variáveis existem, agrupadas pelas mesmas pastas do Infisical (`/database`, `/redis`, `/cloudinary`, `/google`, `/auth`, `/otel`).

## Testes

```bash
./mvnw test
```

## Ambientes e deploy

| Ambiente | Onde roda | Branch |
|---|---|---|
| Local | máquina do dev | qualquer |
| QA | Render | `qa` |
| Produção | GKE (GCP) | `main` |

Merge sempre pra `qa` primeiro — nunca direto pra `main` (ver
[orientações de Git da org](https://github.com/Solierrr/docs-warehouse)).

## Contribuindo

1. Crie uma branch a partir de `qa`: `tipo/descricao-curta` (ex: `feat/nome-da-feature`).
2. Commits no padrão `tipo: mensagem` (inglês, minúsculo, sem escopo).
3. Abra PR pra `qa` preenchendo o [template de PR](.github/pull_request_template.md).

## Licença

Distribuído sob a licença MIT. Ver [`LICENSE`](LICENSE) pra mais detalhes.
