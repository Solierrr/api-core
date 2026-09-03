# Padrões e Regras para Contribuições internas

> É esperado, que todos os membros e contribuidores sigam as diretrizes abaixo antes de enviar qualquer revisão ou contribuição em qualquer repostório da organização.

## Convenções

> *O repositório deve **obrigatoriamente** seguir todas as convenções, padrões e diretrizes estabelecidos nesta documentação. **É esperado** que seja garantido sua estrutura, organização, nomenclatura e implementação. O cumprimento dessas convenções é **imprescindível** para manter a **consistência**, a **legibilidade** e a **qualidade do projeto**.*

### Convenção de Commits

Commits devem ser **atômicos** e **bem distribuídos**. Nunca agrupe múltiplas mudanças não relacionadas em um único commit, nem faça commits redundantes. A mensagem **deve seguir obrigatoriamente** o padrão `{pattern}: {message}`, sempre em `lowercase`. Não há convenção fixa de idioma para o commit, mas mantenha consistência dentro do repositório, normalmente definida pelo *CODEOWNER*: se o histórico já está em inglês, continue em inglês.

#### Exemplos de Commits Patterns

- `fix:` para solução de bugs ou problemas. *Exemplo:* `fix: resolve login validation bug`
- `feat:` para adição de nova funcionalidade. *Exemplo:* `feat: introduce user profile system`
- `docs:` para mudanças em documentação. *Exemplo:* `docs: update installation guide`
- `style:` para alterações visuais ou formatação sem impacto funcional. *Exemplo:* `style: adjust code indentation`
- `refactor:` para reestruturação interna sem alterar comportamento. *Exemplo:* `refactor: simplify authentication logic`
- `build:` para mudanças no sistema de build ou dependências. *Exemplo:* `build: update gradle dependencies`
- `test:` para adição ou modificação de testes. *Exemplo:* `test: add unit tests for login service`
- `ci:` normalmente utilizado para teste ou ajustes de pipeline. *Exemplo:* `ci: implementing docker-hub push workflow`
- `chore:` para tarefas de manutenção e configuração. *Exemplo:* `chore: update project configuration`

### Convenção de Branches

> *Sem exceção, **todas** as branches devem **obrigatoriamente** seguir os padrões e convenções estabelecidos abaixo. **É esperado** que seu uso seja garantido de forma consistente, mantendo a **organização**, a **padronização** e a **qualidade** do fluxo de desenvolvimento.*

Branches de trabalho ficam sempre em `lowercase`, sem espaços ou caracteres especiais, no formato `{pattern}/{descricao-curta}`.

> Branches padrão de repositórios: `main` *&* `qa` (para serviços com deploy no render)

#### Exemplos de Branch Patterns

- `feat/` para nova funcionalidade. *Exemplo:* `feat/user-profile-system`
- `fix/` para correção de bug. *Exemplo:* `fix/login-validation`
- `docs/` para documentação. *Exemplo:* `docs/installation-guide`
- `style/` para formatação/visual. *Exemplo:* `style/code-formatting`
- `refactor/` para refatoração. *Exemplo:* `refactor/authentication-logic`
- `build/` para build/dependências. *Exemplo:* `build/gradle-update`
- `test/` para testes. *Exemplo:* `test/login-service`
- `chore/` para manutenção/configuração. *Exemplo:* `chore/project-configuration`
- `ci/` para pipeline de CI. *Exemplo:* `ci/github-workflow`
- `hotfix/` para correção urgente em produção. *Exemplo:* `hotfix/payment-crash`
- `release/` para preparação de release. *Exemplo:* `release/v1.0.0`

Pull Requests devem sempre ser mergeadas inicialmente em `qa`, nunca abra Pull Request direto para `main`. Caso uma Pull Request seja feita diretamente na `main`, será aberto automaticamente um Pull Request de sincronização, que deverá ser analisado.

## Pull Requests

- Preencha o template de Pull Request (`.github/pull_request_template.md`) por
  completo.
- A descrição da Pull Request deve ser em português.
- Só abra o Pull Request quando o CI estiver passando.
- É esperado que antes do merge, o CI esteja passando.

## Code Owners

- Mudanças em caminhos cobertos pelo [`.github/CODEOWNERS`](./CODEOWNERS)
  exigem aprovação do(s) dono(s) listado(s) antes do merge.

## Regras de Código

- Siga o padrão de código já existente no repositório (nomenclatura,
  estrutura de pastas, estilo de formatação).
- Evite complexidade arquitetural desnecessária, não introduza camadas,
  abstrações ou padrões de design sem uma necessidade concreta e imediata.
- Priorize soluções simples e diretas sobre soluções "genéricas" ou
  "escaláveis" que não foram pedidas.

## Referência

Este documento resume as convenções oficiais de Git da organização. Em caso
de dúvida ou divergência, a fonte da verdade é o Confluence:

- [Convenções](https://interdesctruction.atlassian.net/wiki/spaces/Arquitetur/pages/76283905/Conven+es) (página-índice com todas as convenções do projeto)
- [Commit Pattern](https://interdesctruction.atlassian.net/wiki/spaces/Arquitetur/pages/28901387/Commit+Pattern) (padronização das mensagens de commit)
- [Branches Pattern](https://interdesctruction.atlassian.net/wiki/spaces/Arquitetur/pages/33128466/Branches+Pattern) (padronização de nomes de branch)
- [Convenção de código](https://interdesctruction.atlassian.net/wiki/spaces/Arquitetur/pages/36339713/Conven+o+de+c+digo) (nomenclatura de variáveis, classes e constantes por linguagem)
- [Nomenclatura de repositórios](https://interdesctruction.atlassian.net/wiki/spaces/Arquitetur/pages/75890706/Nomeclatura+de+reposit+rios) (prefixos usados para nomear novos repositórios)
