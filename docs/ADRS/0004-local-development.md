# ADR-0004 — Desenvolvimento local isolado em Docker

Status: **Accepted** para desenvolvimento, conforme solicitação do responsável. Créditos: **oEnzoRibas**. Data: 20/09/2026.

## Context / Problem

O projeto precisa validar mudanças sem usar a Hostinger. Execução na IDE depende de JDK/banco locais e não reproduz automaticamente migrations. Testes H2 não substituem MariaDB.

## Decision

Compose local com MariaDB 11.8.9, Flyway 10.20.1 como tarefa separada e imagem Java 21. Banco saudável → migration concluída → app com ddl-auto=validate. Porta HTTP vinculada a loopback; banco sem porta publicada.

.env permanece para IDE; .env.dev separa secrets do Docker. Modelos e tutoriais são versionáveis; arquivos reais são ignorados e excluídos da imagem. .local/vps.ssh.config é separado dos ambientes da aplicação e não contém senha.

Build executa testes rápidos; Maven verify executa Testcontainers. CI sem secrets valida build/testes, sem deploy automático.

## Alternatives considered

Somente H2 não reproduz SQL MariaDB. Banco remoto para desenvolvimento arrisca produção. Uma única .env para IDE/Docker/prod aumenta chance de conectar ao alvo errado. Manter dois arquivos locais explícitos evita esse acoplamento.

## Consequences / Security

Requer Docker e espaço para imagens/volume. .env.dev e acesso ao daemon devem ser restritos. Volume persiste; não há reset automático. Containers não garantem ausência de vulnerabilidades. Tags de imagens devem ter digest registrado na promoção.

App sem root, filesystem read-only e sem capabilities. Chaves/keystores são excluídos do build context. HTTP local não substitui TLS de produção.

## Operational / Migration considerations

Up reconstrói a aplicação e aplica apenas migrations pendentes; nunca editar versão aplicada. Down preserva volume. Mudanças de senha no arquivo não reconfiguram banco/bootstrap já existentes.

Esse Compose é exclusivamente local e não é o deploy da VPS.

## References

[Guia local](../development/local-environment.md), [ambientes](../configuration/environment.md), [Docker startup order](https://docs.docker.com/compose/how-tos/startup-order/).
