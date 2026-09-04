# Finalidade do repositório

O `api-core` (nome interno do artefato Maven: `persistence`) é a API REST responsável pelo acesso ao banco relacional PostgreSQL da Solaria, concentrando o modelo de domínio central da organização: empresas, usuários, técnicos, fornecedores, propostas, contratos de serviço, faturas de energia, certificações e todo o cadastro que sustenta a operação de instalação e manutenção de sistemas de energia solar. O serviço expõe endpoints CRUD organizados por recurso, documentados via OpenAPI/Swagger, protegidos por dois esquemas de autenticação distintos (JWT de usuário emitido pelo `api-auth` e tokens de serviço M2M para comunicação interna entre APIs), e usa Redis como cache de leitura para consultas de busca de fornecedores com maior volume de acesso.

<p>

[![License](https://img.shields.io/github/license/Solierrr/api-core)](https://github.com/Solierrr/api-core/blob/main/LICENSE)
[![GitHub Last Commit](https://img.shields.io/github/last-commit/Solierrr/api-core)](https://github.com/Solierrr/api-core/commits)
[![GitHub Issues](https://img.shields.io/github/issues/Solierrr/api-core)](https://github.com/Solierrr/api-core/issues)
[![GitHub Pull Requests](https://img.shields.io/github/issues-pr/Solierrr/api-core)](https://github.com/Solierrr/api-core/pulls)
[![GitHub Contributors](https://img.shields.io/github/contributors/Solierrr/api-core)](https://github.com/Solierrr/api-core/graphs/contributors)
[![Release](https://img.shields.io/github/v/release/Solierrr/api-core)](https://github.com/Solierrr/api-core/releases)

</p>

<div align="center">

<p>
  <a href="https://github.com/syvixor/skills-icons">
    <img src="https://skills.syvixor.com/api/icons?i=java,springboot,spring,springsecurity,postgresql,redis,openapi,swagger,maven,docker" height="48" alt="Backend & Infraestrutura">
  </a>
</p>

<p>

[![Java](https://img.shields.io/badge/Java-ED8B00?logo=openjdk&logoColor=white)](https://www.java.com/)
[![Spring Boot](https://img.shields.io/badge/Spring_Boot-6DB33F?logo=springboot&logoColor=white)](https://spring.io/projects/spring-boot)
[![Spring Security](https://img.shields.io/badge/Spring_Security-6DB33F?logo=springsecurity&logoColor=white)](https://spring.io/projects/spring-security)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-4169E1?logo=postgresql&logoColor=white)](https://www.postgresql.org/)
[![Redis](https://img.shields.io/badge/Redis-DC382D?logo=redis&logoColor=white)](https://redis.io/)
[![OpenAPI](https://img.shields.io/badge/OpenAPI-6BA539?logo=openapiinitiative&logoColor=white)](https://www.openapis.org/)
[![Swagger](https://img.shields.io/badge/Swagger-85EA2D?logo=swagger&logoColor=black)](https://swagger.io/)
[![Maven](https://img.shields.io/badge/Maven-C71A36?logo=apachemaven&logoColor=white)](https://maven.apache.org/)
[![Docker](https://img.shields.io/badge/Docker-2496ED?logo=docker&logoColor=white)](https://www.docker.com/)

</p>

</div>

## Aprofunde-se no Projeto!

- [ARCHITECTURE.md](./ARCHITECTURE.md), estrutura de pastas, camadas e padrões usados no repositório.
- [RUNNING.md](./RUNNING.md), como rodar o projeto localmente.
- [DEPLOYMENT.md](https://github.com/Solierrr/.github/blob/main/.github/DEPLOYMENT.md), como funciona o pipeline de deploy da organização.

## Contribuindo

- [CONTRIBUTING.md](./.github/CONTRIBUTING.md), convenções de commit, branch e Pull Request.
- [CODE_OF_CONDUCT.md](https://github.com/Solierrr/.github/blob/main/.github/CODE_OF_CONDUCT.md), código de conduta do projeto.
- [SECURITY.md](https://github.com/Solierrr/.github/blob/main/.github/SECURITY.md), como reportar vulnerabilidades de segurança.
