<p align="center">
  <img src="./assets/readme/banners/springboot.png" alt="Spring Boot" width="100%">
</p>

<p align="center">
  <img src="./assets/readme/stacks/java.png" width="100" hspace="8" alt="Java" title="Java">
  <img src="./assets/readme/stacks/spring-security.png" width="100" hspace="8" alt="Spring Security" title="Spring Security">
  <img src="./assets/readme/stacks/postgresql.png" width="100" hspace="8" alt="PostgreSQL" title="PostgreSQL">
  <img src="./assets/readme/stacks/redis.png" width="100" hspace="8" alt="Redis" title="Redis">
  <img src="./assets/readme/stacks/upstash.png" width="100" hspace="8" alt="Upstash" title="Upstash">
  <img src="./assets/readme/stacks/cloudinary.png" width="100" hspace="8" alt="Cloudinary" title="Cloudinary">
  <img src="./assets/readme/stacks/swagger.png" width="100" hspace="8" alt="Swagger" title="Swagger">
  <img src="./assets/readme/stacks/bruno.png" width="100" hspace="8" alt="Bruno" title="Bruno">
</p>

# Cadastro central da plataforma

[![License](https://img.shields.io/github/license/Solierrr/api-core)](https://github.com/Solierrr/api-core/blob/main/LICENSE)
[![GitHub Last Commit](https://img.shields.io/github/last-commit/Solierrr/api-core)](https://github.com/Solierrr/api-core/commits)
[![GitHub Pull Requests](https://img.shields.io/github/issues-pr/Solierrr/api-core)](https://github.com/Solierrr/api-core/pulls)
[![Release](https://img.shields.io/github/v/release/Solierrr/api-core)](https://github.com/Solierrr/api-core/releases)

Este serviço reúne os cadastros que sustentam a operação de energia solar. Pessoas e empresas usam esses dados para encontrar profissionais e produtos, cadastrar unidades consumidoras e faturas, negociar propostas e acompanhar projetos, contratos e serviços técnicos. O mesmo domínio mantém carrinho, pedidos, vendas, planos, assinaturas e cobranças. O acesso aos recursos depende da identidade, do cargo e da participação da pessoa na operação.

Os aplicativos e os outros serviços consultam esse cadastro por HTTP. O JWT emitido pelo serviço de acesso identifica a pessoa, e chaves próprias identificam os consumidores internos. PostgreSQL guarda os dados de negócio, Redis mantém o cache de buscas e Cloudinary armazena parte dos arquivos. Integrações atendem tradução, recomendação, validação profissional e envio de convites, com limites específicos para cada uma.

## Aprofunde-se no Projeto!

- [OVERVIEW.md](./docs/OVERVIEW.md), capacidades e regras do produto.
- [ARCHITECTURE.md](./docs/ARCHITECTURE.md), módulos, segurança, integrações e decisões técnicas.
- [RUNNING.md](./docs/RUNNING.md), dependências, configuração e testes locais.
- [API.md](./docs/API.md), contrato, autenticação, grupos de rotas e exemplos.
- [DEPLOY.md](./docs/DEPLOY.md), ambientes e publicação.
- [STATUS.md](./docs/STATUS.md), limitações e divergências verificadas.
- [Fluxos](./flows/), negociação, execução, aprovação profissional, convites, arquivos e traduções.
- [Banco de dados](./database/), schema PostgreSQL e cache Redis.
- [PENDENCIAS.md](./PENDENCIAS.md), diferenças e decisões que dependem do responsável.

## Contribuindo

- [CONTRIBUTING.md](https://github.com/Solierrr/.github/blob/main/.github/CONTRIBUTING.md), convenções de commit, branch e Pull Request.
- [CODE_OF_CONDUCT.md](https://github.com/Solierrr/.github/blob/main/CODE_OF_CONDUCT.md), código de conduta.
- [SECURITY.md](https://github.com/Solierrr/.github/blob/main/SECURITY.md), como reportar uma vulnerabilidade.
