# Etapa 09 — OpenAPI

Créditos: **oEnzoRibas**.

Integração proposta antes da implementação e registrada em [ADR 0006](../../0006-api-documentation.md): springdoc 2.8.9 compatível com Boot 3.4.x, sem atualizar Boot. Documentação habilitada em dev, desabilitada por padrão/prod, URLs negadas quando desligada.

Cobertura: 18 operações, nove DTOs explícitos, respostas de entidades existentes e schema compartilhado de erro. Controllers recebem Operation; DTOs recebem Schema; metadata, Bearer, ADMIN, headers e erros ficam centralizados. [Inventário e instruções](../../../api/README.md).

Nenhum endpoint, payload, código HTTP de negócio ou tabela foi alterado. Normalização documental do placeholder de episódios resolve templates duplicados mantendo GET por YouTube ID e DELETE por UUID.

Verificação: geração via MockMvc, cobertura de operações, schemas principais, respostas, segurança, HTML/configuração Swagger e bloqueio quando desligado. Resultado final da suíte completa no relatório 10.

Lacunas: ausência de validação visual em navegador e de testes de todos os exemplos/erros; não há garantia de atualização segura de dependências apenas pela compatibilidade declarada. Specs geradas não são copiadas manualmente para o Git.
