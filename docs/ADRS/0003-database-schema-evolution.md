# ADR-0003 — Database schema evolution

Status: **Accepted** para a instalação limpa aprovada nas etapas 03/04. Créditos: **oEnzoRibas**. Data: 20/09/2026.

## Context / Problem

MariaDB atual informado como 11.8.9. Dados antigos declarados descartáveis; aplicação infuncional. A cadeia antiga possui V1 SQL e V2 Java, com histórico remoto desconhecido. Preservar conversões do legado não traz benefício para uma instalação vazia.

## Decision

Flyway é a fonte de versionamento do schema novo, com cadeia isolada em db/fresh-migration. V1 cria diretamente users/posts/episodes/contact_messages/topic_suggestions. Migrations históricas permanecem imutáveis e não são descobertas por essa location.

Migrations aplicadas não são reescritas; evoluções recebem novas versões. Hibernate usa validate, nunca update/create em dev/prod. Executor externo aplica e valida migrations antes do runtime; app não migra ao iniciar.

Java e nomes físicos em inglês, mantendo a API existente em português onde necessário. Nova instalação não é upgrade in-place nem preserva IDs/dados antigos. Criar outra base antes de excluir a antiga é a estratégia preferida; exclusão exige autorização específica.

## Alternatives considered

Manter nomes físicos, rename imediato no legado e expand-and-contract foram avaliados. Dados descartáveis favorecem criação limpa. Para futuras bases com dados úteis, voltar a analisar expansão compatível, backfill e contract em releases distintas; não perpetuar política de reset.

## Consequences / Migration considerations

Binário antigo não funciona com schema novo. Rollback deve coordenar binário, configuração e banco. Para futuras renomeações, preferir @Column explícito ou expand-and-contract quando houver benefício real, preservando leitura/escrita durante a janela de retorno.

Não usar baseline em banco vazio. Importação manual no phpMyAdmin exige procedimento separado de reconciliação; não fingir execução no histórico.

## Security / Operational considerations

Alvo dedicado, credentials externas, clean-disabled=true e baseline-on-migrate=false. Backup/restauração verificados antes de alterações em dados úteis; exportação do legado como precaução, com retenção decidida. Migrations destrutivas exigem aprovação, janela e plano de recuperação.

DDL MariaDB pode ficar parcialmente aplicado: capturar estado e preferir forward-fix com revisão; repair não desfaz DDL. Banco novo ainda descartável pode ser recriado apenas com autorização do alvo. Testes reais MariaDB e contratos HTTP são gates, além de smoke tests da implantação.

## References

[Etapa 03](0002/relatorios/etapa03-estrategia-banco-e-migracoes.md), [etapa 04](0002/relatorios/etapa04-instalacao-limpa.md).
