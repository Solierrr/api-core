# api-core

## Como rodar local

1. Instale o [Infisical CLI](https://infisical.com/docs/cli/overview) e rode `infisical login`.
2. Na raiz do repo, rode `infisical init` (uma vez só) e escolha o projeto do time.
3. Suba o serviço com:

   ```bash
   infisical run --env=dev --path=/ --recursive -- ./mvnw spring-boot:run
   ```

Não é mais necessário copiar `.env.example` pra `.env` manualmente — o Infisical injeta as variáveis em runtime.
