<p align="center">
  <img src="../assets/readme/banners/springboot.png" alt="Spring Boot" width="100%">
</p>

# Visão de produto

O cadastro central atende empresas compradoras e fornecedoras, profissionais e pessoas que acompanham projetos de energia solar. Ele reúne a informação de negócio usada pelos aplicativos, preservando quem pode consultar, negociar, administrar e executar cada operação.

## Capacidades

O domínio cobre os grupos de tarefas abaixo.

- Manter pessoas, empresas, cargos, permissões e códigos de acesso à empresa.
- Cadastrar unidades consumidoras, faturas de energia, endereços e especificações.
- Consultar empresas, profissionais aprovados, marcas, modelos e ofertas.
- Negociar propostas com itens, quantidades, preços e distribuição por unidade.
- Administrar projetos, participantes, convites, agenda, histórico e fotos.
- Criar serviços técnicos, indicar executores, aceitar a execução e registrar sua conclusão.
- Manter contratos com garantia, prazo, seguro e aprovação da concessionária.
- Comprar pelo carrinho, consultar pedidos e vendas e cancelar pedidos próprios pagos.
- Administrar planos, assinaturas e cobranças.
- Submeter documentos profissionais e acompanhar a revisão do cadastro.

## Empresas e acesso

A decisão de produto permite que uma pessoa pertença a mais de uma empresa, com cargos diferentes. A implementação atual ainda limita o vínculo a uma empresa por pessoa, e a adequação a essa regra está pendente.

O cargo de administrador concede acesso de administração da empresa. Outros cargos dependem das permissões concedidas. A empresa deve manter ao menos um administrador. O acesso a projetos também considera a participação da pessoa, com papéis de proprietário, gestor, participante e observador.

## Fluxo principal

O fluxo principal do produto começa pela unidade consumidora e sua fatura, passa pela negociação e aceite da proposta e segue para projeto, contrato e execução por técnico. No código atual, o aceite reserva o estoque e encerra a negociação. A criação do projeto e do serviço ocorre em operações separadas, sem conversão automática da proposta.

Uma proposta envolve uma empresa compradora e uma fornecedora, que alternam a vez de responder aos termos até o aceite. Criar a proposta não reserva estoque, e por isso o aceite confere o saldo do momento e pode ser recusado quando a disponibilidade mudou desde a criação.

Serviços técnicos começam abertos, passam a andamento no aceite e podem ser concluídos depois. O contrato só pode ser criado para um serviço em andamento, e cada serviço tem no máximo um contrato. A identidade de quem aceita é obtida da sessão, sem aceitar que outra pessoa seja indicada como autora do aceite.

## Profissionais e documentos

O profissional só aparece no catálogo de clientes depois de aprovado. O cadastro permite rascunho, envio de documentos, acompanhamento da análise e revisão administrativa. Uma nova submissão registra uma cópia da versão enviada, sem substituir imediatamente o perfil aprovado.

A política da profissão define os documentos e os critérios exigidos, e os documentos fora do caminho automático e as políticas não configuradas seguem para revisão manual. O processamento automático depende da ativação da integração de validação.

## Transações e catálogos

O pedido registra os preços, descontos e quantidades usados na compra. O cancelamento de um pedido pago restaura a disponibilidade das ofertas ainda existentes e registra o reembolso do pagamento. O domínio impede a compra de uma oferta da própria empresa.

Uma oferta pode ser ativa, pausada ou encerrada. Traduções são processadas depois da gravação por uma tarefa durável. Quando o serviço de recomendação está indisponível, o cadastro central pode devolver uma seleção calculada com seus dados relacionais.

## Onde aparece

Os aplicativos web e mobile apresentam os cadastros e os serviços conforme a identidade e as permissões da pessoa. O portal de QA permite testar as operações. Os convites de projeto chegam por e-mail, e o serviço de conversas consulta a participação no projeto antes de conceder o acesso correspondente.
