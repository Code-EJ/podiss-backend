# Etapas 05/06 — decisões, ambiente local e preparação da VPS

Créditos: **oEnzoRibas**. Data: 20/09/2026.

## Estado

- **DONE:** ADR-0003 (schema) e ADR-0004 (Docker local), Accepted.
- **DONE:** templates e tutorial de .env/.env.dev/SSH; README como entrada.
- **DONE:** Dockerfile Java 21, Compose com MariaDB/Flyway, script PowerShell sem reset e CI de verificação sem deploy.
- **PROPOSED:** ADR-0005 de deployment na VPS.
- **PARTIALLY DONE:** runbook remoto; faltam processo, proxy, banco e mecanismo de restart.
- **PENDING DECISION / INFORMATION REQUIRED:** topologia real e plano de corte.
- Atualização: o responsável autorizou explicitamente continuar até a etapa 10. Relatórios 07–10 registram a continuação local; isso não remove os gates de deploy nem torna completo o runbook remoto.

## Novas informações

O responsável confirmou VPS Ubuntu 24.04 LTS do projeto. Portanto o bloqueio absoluto baseado somente no Business Web Hosting não se aplica à conta toda. A limitação de Java no Business continua verdadeira, mas há outro ambiente possível. O responsável não sabe se o MariaDB visto no phpMyAdmin está na VPS nem como Java é iniciado.

Foram preparados comandos SSH de leitura, sem conexão remota executada. .local/vps.ssh.config contém somente placeholders, ignorado pelo Git. Senhas SSH não têm arquivo de armazenamento; preferir prompt ou chave pessoal fora do repositório.

## Ambiente e validação

Compose local está ativo em http://localhost:18080, banco sem porta publicada, volume podiss-dev_database. A tarefa migrate terminou com exit 0; app e banco estão healthy.

- Build Docker em Java 21: nove testes H2 aprovados.
- Maven clean verify com **JDK 21.0.9** e MariaDB 11.8.9: **22 testes, zero falhas/erros/ignorados**. Isso complementa o teste Java 23 da etapa 04.
- Flyway CLI 10.20.1 em contêiner: migration inicial executada e validate aprovado.
- Smoke real HTTP: login de dev_admin com JWT, POST /contatos e GET administrativo confirmando o registro.
- Um contato sintético e o administrador de desenvolvimento permanecem no volume local; nenhum dado real importado.
- Git ignora .env, .env.dev e .local/vps.ssh.config; os modelos/tutoriais não estão ignorados.
- ssh -G validou a sintaxe do template local sem estabelecer conexão.
- Compose recusou template com secrets vazios, antes de iniciar recursos.
- Docker build context exclui .env*, .local, chaves e keystores. Nenhum secret é passado no build.

O workflow GitHub foi criado, não executado remotamente nem feito push. Aviso Flyway para MariaDB mais novo que a faixa declarada testada persiste; dependências não foram atualizadas.

## Documentação e configuração

docs/configuration/environment.md diferencia arquivo privado/modelo, quem carrega, variáveis, formatos e defaults. .env.example foi corrigido: JWT/senhas vazios em vez de exemplos inválidos/utilizáveis; pool completo; keystore externo sem apontar ao certificado versionado. A .env real e .run foram preservados.

.env.dev foi gerado com credenciais aleatórias somente locais. Scripts nunca imprimem config expandida. Gitignore protege .local e arquivos reais. O usuário pediu explicitamente tutoriais no Git; modelos, guias e automação são incluídos nos commits, nunca secrets locais.

ADR-0001 é histórico e sua política de update/profile foi parcialmente substituída pela configuração atual e ADR-0003. Consulte a revisão de segurança da etapa 07; TLS/deploy não foram certificados em produção.

## Organização Git

Por pedido do responsável, commits locais serão organizados na mesma branch por responsabilidade: baseline/documentos de análise; aplicação + schema novo/configuração; testes; Docker/CI e configuração local; documentação operacional/ADRs. Mudanças de aplicação anteriores e naming interdependentes são agrupadas para evitar commits com imports/classes ausentes; não foi fabricada uma sequência histórica de implementações que não existia no Git.

A alteração pré-existente em src/main/resources/db/migration/V1__database.sql permanece **fora dos commits**, sem ser descartada, pois o histórico aplicado não foi reconciliado. A V2 Java antiga é preservada como snapshot na cadeia inativa. Nenhum commit novo deve autorizar execução dessas migrations históricas.

Datas e autoria Git configuradas não serão forjadas. Créditos oEnzoRibas nos documentos e corpos de commits; não há push automático.

## Próximo passo do responsável

Preencher .local/vps.ssh.config conforme docs/deployment/vps-access.md e executar somente o diagnóstico de leitura. Compartilhar versões/nomes de serviços/portas, não credenciais. Fechar topologia e procedimento de deploy antes das demais etapas e de qualquer publicação.
