# Runbook — preparação local e publicação na VPS

Créditos: **oEnzoRibas**. Status: **parcial; não executar deploy até completar o inventário**.

## Arquitetura conhecida

Frontend/domínio Podiss; conta Hostinger com Business Web Hosting e uma VPS Ubuntu 24.04 LTS. MariaDB 11.8.9 visto no phpMyAdmin. O usuário não sabe se esse banco está no Ubuntu ou é gerenciado pelo hPanel. PHPMyAdmin é cliente, não comprova localização do banco. Há indicação de backup diário no painel, sem restore verificado.

Local: Docker reproduz app Java 21, MariaDB e migration. Remoto: Docker/systemd/JAR/proxy ainda desconhecidos. Não copiar compose.dev.yaml para VPS.

## Gate 0 — descoberta somente leitura

Siga [vps-access.md](vps-access.md). Preencha somente .local/vps.ssh.config. Nenhuma credencial deve ser enviada em chat ou commit.

TODO / INFORMATION REQUIRED:

- Identificação do processo e usuário Java, versão, caminho do JAR/imagem.
- Serviço systemd ou contêiner/comando de restart existente.
- Proxy, hostname API, terminação TLS e renovação do certificado.
- Banco efetivamente usado pelo processo, URL sem senha, conectividade e privilégios.
- Cota de banco paralelo, backup/restauração, retenção e janela.
- SSH verificado e responsável autorizado pela operação.

## PRE-DEPLOY

- [ ] Gate 0 respondido e ADR de deployment aprovada.
- [ ] Release identificada por commit/hash/digest; diff revisado, sem secrets.
- [ ] Java 21, Maven verify e testes MariaDB; build Docker e smoke local.
- [ ] Migrations revisadas; somente db/fresh-migration; validate/info conferidos.
- [ ] Base nova dedicada e alvo confirmado; não assumir upgrade do legado.
- [ ] Credenciais novas fora do Git; profile prod; ddl-auto validate; Flyway desligado no app.
- [ ] CORS, JWT, primeiro ADMIN, TLS/proxy e política de tokens conferidos.
- [ ] Exportação/backup conforme decisão e restore ensaiado se usado no retorno.
- [ ] Artefato/configuração antigos guardados e retorno compatível definido.
- [ ] Janela e responsável aprovados.

## DEPLOY — ordem lógica, não script executável remoto

1. Preparar base nova no local confirmado (hPanel ou VPS), sem excluir origem.
2. Validar/aplicar/validar a cadeia com executor único e credenciais do alvo novo. CLI local/CI ou tarefa na VPS precisa de conectividade aprovada. Comandos Flyway da [etapa 04](../ADRS/0002/relatorios/etapa04-instalacao-limpa.md) devem ser adaptados ao alvo real, sem secrets em argumentos.
3. Transferir/promover JAR ou imagem **já testados**, conferir hash. Não enviar .env.dev, .local, volume Docker ou chave privada.
4. Configurar variáveis no mecanismo remoto confirmado, fora do repositório.
5. Interromper/esvaziar escritores antigos e iniciar artefato novo no runtime confirmado.
6. Monitorar logs redigidos e validar health/smoke antes de abrir tráfego.

TODO: comandos exatos de upload/restart/proxy dependem do Gate 0. Não fornecer comandos genéricos que sobrescrevam instalação desconhecida. Porta interna atual é 8080; porta pública, firewall e TLS dependem do proxy confirmado.

## POST-DEPLOY

- [ ] Startup sem falha Hibernate/schema/datasource.
- [ ] Histórico Flyway com somente as versões esperadas, sem pendências/falhas.
- [ ] GET /episodes e /posts respondem; vazio é esperado em base nova.
- [ ] Login ADMIN e acesso administrativo; usuário comum sem permissões indevidas.
- [ ] Contato/sugestão de smoke controlados; criação/edição de post e imagem; episódio com YouTube.
- [ ] Frontend real funciona com CORS, cookies/headers e HTTPS conforme contrato.
- [ ] Logs sem secrets/stack traces indevidos; recursos/latência observados.
- [ ] Aceite e janela de observação; só depois discutir exclusão da origem.

Não há Actuator instalado. Healthcheck local faz GET /episodes?size=1 e detecta HTTP/consulta, não saúde completa de integrações. Métricas remotas e coletor ainda não inventariados.

## ROLLBACK / RECOVERY

Falha antes do corte: cancelar promoção, manter origem. Falha depois: retornar binário + configuração + banco compatíveis; binário antigo não conhece tabelas novas. Retorno ao antigo pode continuar infuncional.

Migration parcial: não executar clean/repair automaticamente. Inspecionar efeitos; corrigir com versão nova quando houver dados úteis. Recriar base nova descartável exige alvo e autorização específicos.

Restore: apenas com backup validado e aprovação da perda de gravações posteriores; parar escritores e coordenar artefato/schema. Falha de conexão/configuração: revisar URL, rede, TLS, usuário e profile sem imprimir senha; não editar migrations para contornar.

## Critério de conclusão

Este runbook só será executável de ponta a ponta após o Gate 0. A autorização para concluir as demais etapas quando houver procedimento definido não elimina essa pendência. Não houve deploy, login SSH ou alteração na Hostinger nesta entrega.
