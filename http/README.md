# Coleção Bruno: api-core

Abra esta pasta `http/` como uma coleção Bruno e selecione o ambiente `local`.
A URL base é `http://localhost:8080`. Credenciais, tokens e chaves de API ficam vazios no ambiente versionado; preencha-os localmente no Bruno e não faça commit desses valores.

Os IDs usam UUIDs de exemplo sintaticamente válidos. Troque-os por IDs existentes no banco local quando a operação depender de dados prévios. Requests que criam, atualizam, removem, iniciam fluxos ou chamam rotas internas estão marcados no nome ou na documentação; confira seus efeitos antes de enviar. A coleção não executa requests automaticamente.

`api-core` and `api-messenger` both default to port 8080. Run only one at a time on that port, or change this collection's `baseUrl` in the local Bruno environment.
