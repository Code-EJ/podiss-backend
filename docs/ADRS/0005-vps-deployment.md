# ADR-0005 — Deployment strategy na VPS

Status: **Proposed**. Créditos: **oEnzoRibas**. Data: 20/09/2026.

## Context / Problem

O responsável confirmou VPS Ubuntu 24.04 LTS do Podiss, além de Business Web Hosting e phpMyAdmin. A ausência de suporte Java no Business não significa ausência de VPS. A hipótese de impossibilidade total de deploy na conta está superada; o runtime real ainda não foi identificado.

## Decision proposed

Promover artefato testado para runtime Java 21 na VPS, com TLS no proxy e aplicação sem migrations automáticas. Preferir imagem imutável se Docker já existir/for aprovado; alternativamente JAR sob serviço gerenciado. Não instalar Docker, sobrescrever proxy ou substituir serviço existente sem inventário.

TODO / INFORMATION REQUIRED: banco gerenciado versus VPS; processo atual; proxy/domínio/certificado; usuário operacional; portas; Docker/systemd; backup e recursos. Não foi tomada decisão de mover o banco para a VPS.

## Alternatives considered

Business Web Hosting sozinho não executa Java segundo a documentação do provedor. VPS já existente evita presumir nova contratação. Novo serviço externo é alternativa somente se a VPS não atender. Reescrever em PHP/Node não está autorizado.

## Consequences / Security considerations

Credenciais SSH pessoais ficam fora do Git. Acesso é separado da autorização de alterar serviço. Não expor 3306/8080 publicamente sem desenho e restrição; não confiar em headers encaminhados sem política de proxies.

JWT existente é documentado como mecanismo atual, não certificado como arquitetura final; TTL, revogação, rate limiting e proxy serão avaliados na etapa de segurança. Nenhuma decisão TLS final foi inventada.

## Operational / Migration considerations

Aplicar cadeia nova em base nova, validar, promover binário + datasource juntos. Backup/restore, smoke e rollback compatível obrigatórios. Sem deploy automático. Não transportar volume ou secrets de desenvolvimento.

## References

[Runbook preliminar](../deployment/runbook.md), [diagnóstico SSH](../deployment/vps-access.md), [Hostinger Java](https://www.hostinger.com/support/which-programming-languages-and-frameworks-are-supported-at-hostinger/).
