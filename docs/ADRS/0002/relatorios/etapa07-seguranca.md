# Etapa 07 — revisão de segurança

Créditos: **oEnzoRibas**. Revisão estática em 2026-09-20; não é pentest nem inventário completo de CVEs. Nenhuma conexão com produção.

## Dependências e vulnerabilidades

Boot 3.4.1 resolve Tomcat 10.1.34. Essa versão está na faixa afetada por CVE-2025-24813 (até 10.1.34; correção 10.1.35). O advisory exige condições específicas, incluindo escrita no DefaultServlet; não encontramos habilitação dessa escrita. O upload próprio do controller não prova essa condição. **HIGH: componente afetado confirmado, exploração nesta aplicação não demonstrada**.

CVE-2025-48988 também inclui 10.1.34 (até 10.1.41): consumo excessivo de memória por multipart. **HIGH: investigar exposição no proxy e atualizar conjunto Boot/Tomcat em mudança dedicada**. Limite de bytes não equivale a limite de partes.

Fonte primária: [Apache Tomcat security advisories](https://tomcat.apache.org/security-10.html). Não atualizar apenas para a primeira versão corrigida: há advisories posteriores. Atualização de BOM exige matriz de compatibilidade e regressão, não foi feita silenciosamente.

JWT 0.11.5, Hibernate 6.6.4, Spring Security 6.4.2, Framework 6.2.1 e Flyway 10.20.1 merecem análise contínua. Idade isoladamente não prova vulnerabilidade. Não foi executado scanner completo/SBOM nesta rodada.

Scopes: impl/Jackson JWT runtime; H2, testes e Testcontainers test; Lombok optional e excluído do JAR; devtools runtime/optional. Driver MariaDB compile pode ser runtime, mas sem benefício funcional urgente. Não há evidência de dependência direta redundante; não removida nenhuma.

## Configuração e riscos

| Severidade | Evidência | Decisão |
| --- | --- | --- |
| HIGH | server.p12 já rastreado no Git e empacotado pelo Maven | Verificar titularidade/uso, substituir material se contiver chave válida; retirada e rotação coordenadas. Não imprimir conteúdo nem remover de instalação desconhecida. Docker exclui keystores do contexto. |
| HIGH | Login sem throttling próprio | Planejar rate limit no proxy/aplicação com política de bloqueio; não introduzir bloqueios arbitrários nesta etapa. |
| MEDIUM | JWT dura 5h, sem revogação individual | Chave exige Base64 com 64 bytes, issuer e expiração verificados. Rotação invalida sessões; planejar. |
| MEDIUM | Rate limit de submissões por getRemoteAddr e memória do processo | Proxy pode agrupar usuários; múltiplas réplicas não compartilham quota. Não confiar indiscriminadamente em X-Forwarded-For. |
| MEDIUM | Imagem validada por assinatura, não decodificação completa | Tipos restritos, 5MB arquivo/6MB request; considerar decodificação e limites de pixels em evolução planejada. |
| MEDIUM | TLS e privilégios do banco remoto desconhecidos | Gate de deploy; TLS do app opcional não significa HTTPS público validado. |
| LOW | Hikari 10/5 default | Ajustar após conhecer quota de conexões e número de réplicas. |
| INFORMATIONAL | Flyway externo, clean disabled, baseline false, JPA validate | Manter. Nova cadeia somente em base vazia. |

## Controles observados

Rotas administrativas protegidas na cadeia e por método; registro não é público. BCrypt e limite de 72 bytes UTF-8. CORS por lista explícita, sem credenciais. Stateless/Bearer explica CSRF desabilitado; migrar autenticação para cookie exigiria revisão. Erros conhecidos usam ProblemDetail sem SQL/senha; SQL logging desligado. Não há Actuator. Não foram coletados logs reais da VPS.

.env e .env.dev são privados/ignorados; modelos sem secrets reais. Não se afirma que todo histórico Git esteja livre de credenciais: material antigo deve ser auditado e, se necessário, rotacionado. Evitar logs de Authorization, comandos Docker config sem --quiet e dumps de ambiente.

## Alterações, testes e próximos passos

Nenhuma atualização de dependência ou mudança de autorização de negócio nesta etapa. Controles já implementados foram revisados; recomendações potencialmente breaking ficaram propostas. Verificação final de regressão está no relatório 10. OpenAPI será decisão explícita da etapa 09: desligado por padrão e em produção.

Bloquear promoção até tratar/aceitar formalmente riscos HIGH, confirmar TLS/proxy e avaliar advisories atualizados. Não há conclusão de que produção esteja segura apenas porque os testes passam.
