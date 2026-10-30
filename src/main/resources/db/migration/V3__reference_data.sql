INSERT INTO job_position (name, accesses)
VALUES ('ADMIN', '')
ON CONFLICT (name) DO NOTHING;

INSERT INTO company_plan (name, price, cycle)
SELECT 'Gratuito', 0, 'MONTHLY'
WHERE NOT EXISTS (SELECT 1 FROM company_plan WHERE name = 'Gratuito');

INSERT INTO permission (permission_name, name, description)
VALUES
    ('POST /api/offers', 'Criar ofertas', 'Cadastrar novas ofertas de placas solares'),
    ('PUT /api/offers/{id}', 'Editar ofertas', 'Atualizar ofertas já cadastradas'),
    ('DELETE /api/offers/{id}', 'Remover ofertas', 'Excluir ofertas cadastradas'),
    ('GET /api/offers/company/{companyId}', 'Ver ofertas', 'Listar as ofertas da empresa'),
    ('POST /api/models', 'Criar placas solares', 'Cadastrar novos modelos de placa solar'),
    ('PUT /api/models/{id}', 'Editar placas solares', 'Atualizar modelos de placa solar já cadastrados'),
    ('POST /api/access-codes', 'Convidar funcionários', 'Gerar código de acesso para novos funcionários'),
    ('GET /api/access-codes/company/{companyId}', 'Ver códigos de acesso', 'Listar códigos de acesso gerados pela empresa'),
    ('PATCH /api/user-companies/{id}/position', 'Editar cargo de funcionários', 'Alterar o cargo de um funcionário'),
    ('DELETE /api/user-companies/{id}', 'Remover funcionários', 'Desligar um funcionário da empresa'),
    ('GET /api/user-companies/company/{companyId}', 'Ver funcionários', 'Listar os funcionários da empresa'),
    ('POST /api/local-units', 'Criar unidades', 'Cadastrar novas unidades locais'),
    ('PUT /api/local-units/{id}', 'Editar unidades', 'Atualizar unidades locais já cadastradas'),
    ('GET /api/local-units/company/{companyId}', 'Ver unidades', 'Listar as unidades da empresa'),
    ('GET /api/users/{id}', 'Ver usuários', 'Consultar dados básicos de outro usuário'),
    ('GET /api/models', 'Ver modelos', 'Consultar o catálogo compartilhado de modelos'),
    ('DELETE /api/models/{id}', 'Remover modelos', 'Remover modelos próprios em análise'),
    ('DELETE /api/local-units/{id}', 'Remover unidades', 'Remover unidades da empresa'),
    ('DELETE /api/access-codes/{id}/company/{companyId}', 'Revogar códigos', 'Revogar códigos de acesso da empresa'),
    ('POST /api/positions', 'Criar cargos', 'Criar cargos para a empresa'),
    ('POST /api/company-positions', 'Vincular cargos', 'Disponibilizar cargos para a empresa'),
    ('GET /api/company-positions/company/{companyId}', 'Ver cargos', 'Listar cargos da empresa'),
    ('GET /api/permissions', 'Ver permissões disponíveis', 'Consultar o catálogo de permissões'),
    ('GET /api/position-permissions/position/{positionId}', 'Ver permissões de cargos', 'Consultar as permissões de cargos da empresa'),
    ('POST /api/position-permissions', 'Conceder permissões', 'Conceder permissões a cargos próprios'),
    ('DELETE /api/position-permissions/{id}', 'Revogar permissões', 'Revogar permissões de cargos próprios'),
    ('POST /auth/password/change', 'Alterar própria senha', 'Permitir alteração de senha de identidade empresarial'),
    ('POST /auth/password/recovery', 'Recuperar própria senha', 'Permitir recuperação de senha de identidade empresarial'),
    ('GET /api/suppliers/company/{companyId}', 'Ver dados de fornecedor', 'Consultar dados da empresa fornecedora'),
    ('GET /api/requesters/company/{companyId}', 'Ver dados de demandante', 'Consultar dados da empresa demandante'),
    ('GET /api/models/status/{status}', 'Ver modelos por situação', 'Consultar modelos por situação de análise'),
    ('POST /api/geolocations', 'Cadastrar localização', 'Salvar localização de uma unidade'),
    ('PATCH /api/offers/{id}/status', 'Alterar situação da oferta', 'Pausar, retomar ou encerrar ofertas próprias'),
    ('GET /api/offers/{id}/history', 'Ver histórico de ofertas', 'Consultar alterações de situação das ofertas próprias'),
    ('POST /api/professional-teams', 'Criar equipes profissionais', 'Criar equipes vinculadas à própria empresa'),
    ('PUT /api/professional-teams/{id}', 'Editar equipes profissionais', 'Atualizar integrantes e responsável de equipes próprias'),
    ('GET /api/professional-teams/company/{companyId}', 'Ver equipes profissionais', 'Consultar equipes da própria empresa'),
    ('POST /api/projects', 'Criar projetos', 'Criar projetos no contexto empresarial'),
    ('GET /api/cart', 'Ver carrinho', 'Consultar os itens do próprio carrinho'),
    ('PUT /api/cart/items/{offerId}', 'Alterar carrinho', 'Adicionar uma oferta ou mudar a quantidade no carrinho'),
    ('DELETE /api/cart/items/{offerId}', 'Remover item do carrinho', 'Tirar uma oferta do carrinho'),
    ('POST /api/orders', 'Comprar', 'Finalizar a compra dos itens do carrinho'),
    ('GET /api/orders', 'Ver pedidos', 'Consultar os próprios pedidos'),
    ('GET /api/orders/{id}', 'Ver pedido', 'Consultar o detalhe e o comprovante de um pedido próprio'),
    ('PATCH /api/orders/{id}/cancel', 'Cancelar pedido', 'Cancelar um pedido próprio e devolver o estoque'),
    ('GET /api/sales', 'Ver vendas', 'Consultar os itens vendidos pela empresa fornecedora'),
    ('GET /api/subscription', 'Ver plano da empresa', 'Consultar plano, status e cobranças da empresa fornecedora'),
    ('POST /api/charges/{id}/payment', 'Pagar cobrança do plano', 'Pagar, de forma simulada, uma cobrança pendente do plano')
ON CONFLICT (permission_name) DO NOTHING;
