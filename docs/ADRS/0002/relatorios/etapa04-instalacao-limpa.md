# Etapa 04 — Instalação limpa com schema em inglês

Créditos/responsável: **oEnzoRibas**. Data: 20/09/2026.

## 1. Decisão e escopo

Implementação da alternativa **D1** do [relatório revisado da etapa 03](etapa03-estrategia-banco-e-migracoes.md): preparar uma base nova, sem importar dados legados, validar e depois realizar corte coordenado. Não foi feito deploy nem acesso à Hostinger.

A instrução original da etapa 04 de preservar/converter registros foi supersedida, para esta instalação, pela declaração do responsável de que os dados são descartáveis e pela aprovação da reconstrução limpa. **Isso não autoriza excluir qualquer banco nesta execução.** O banco antigo e suas migrations permaneceram intactos.

A conclusão anterior da etapa 03 foi reconsiderada e documentada: não há benefício em renomear/converter dados antigos antes de descartá-los. Criar diretamente o schema final elimina backfill e coexistência de schemas. A causa da aplicação estar infuncional continua sem diagnóstico de produção.

## 2. Migration criada e isolamento da linhagem

Arquivo novo: [V1__fresh_schema.sql](../../../../src/main/resources/db/fresh-migration/V1__fresh_schema.sql).

Localização ativa: `classpath:db/fresh-migration`, deliberadamente **fora** de `db/migration`. Isso evita que a descoberta recursiva da cadeia histórica encontre outra V1. A localização sugerida na etapa 03 era ilustrativa; o caminho implementado oferece isolamento mais claro.

As migrations históricas continuam nos caminhos originais:

- `src/main/resources/db/migration/V1__database.sql`;
- `src/main/java/db/migration/V2__functional_schema.java`.

Nenhuma delas foi editada nesta etapa. A nova V1 pertence a **outra linhagem**, válida apenas para banco vazio. Não é uma substituição de checksum no histórico antigo, nem upgrade de instalações antigas. Não executar com locations combinadas ou apontando para um diretório pai comum.

### SQL executado pela nova V1

Cinco CREATE TABLE, com PK UUID, engine InnoDB e charset utf8mb4:

| Tabela | Colunas | Constraints/índices |
| --- | --- | --- |
| users | id, username, password, email, role | PK id; uk_users_username; uk_users_email |
| posts | id, title, description, tags, created_at, image, image_content_type | PK id; idx_posts_created_at_id |
| episodes | id, title, description, youtube_id, video_url, thumbnail_url, created_at | PK id; uk_episodes_youtube_id; idx_episodes_created_at_id |
| contact_messages | id, sender_name, email, subject, message, created_at | PK id; idx_contact_messages_created_at_id |
| topic_suggestions | id, sender_name, email, topic, created_at | PK id; idx_topic_suggestions_created_at_id |

O SQL integral está no arquivo linkado, sem duplicação neste relatório. Os quatro índices temporais usam (created_at,id). Não há FKs: o domínio atual não declara relacionamentos. Não foram adicionados novos relacionamentos, tabelas de negócio, seeds ou senhas.

Estado anterior esperado: banco dedicado vazio, sem schema de aplicação/histórico. Estado posterior: cinco tabelas finais e histórico criado pelo Flyway. O Flyway executa adicionalmente seu DDL/DML de controle de histórico.

Regras preservadas: TEXT para descrições/conteúdo, LONGBLOB para imagem, tags VARCHAR(2048) NN default vazio, role VARCHAR(16) NN default USER, timestamps com microssegundos, campos opcionais de imagem/thumbnail. UUIDs são gerados pela aplicação; não foi introduzido default SQL de geração.

Collation geral: utf8mb4_unicode_ci; unicidade de username/email segue essa collation, incluindo sua comparação de caixa/acentos. youtube_id usa ascii_bin para diferenciar IDs YouTube pela caixa. Isso é uma definição explícita da instalação nova, não uma conversão de dados antigos. A versão MariaDB e regras de comparação do ambiente real devem ser confirmadas antes de publicação.

A migration não contém DROP, TRUNCATE, DELETE, transformação de dados, IF NOT EXISTS ou comandos de administração global. Não é idempotente se colada repetidamente em phpMyAdmin; repetibilidade é controlada pelo histórico Flyway.

## 3. Mapeamentos e compatibilidade

- ContactMessage: tabela contact_messages; senderName/subject/message mapeados para sender_name/subject/message.
- TopicSuggestion: tabela topic_suggestions; senderName/topic mapeados para sender_name/topic.
- Todas as cinco entidades agora possuem nomes físicos explícitos em @Column, inclusive IDs e campos camelCase.
- Services usam os novos setters internos.
- Nomes lógicos JPA Contato/Sugestao/Video e nomes legados dos beans permanecem.
- DTOs de entrada continuam aceitando nome/assunto/mensagem/tema. @JsonProperty nas entidades preserva a saída com essas mesmas propriedades; não foram acrescentados campos JSON ingleses duplicados.
- Endpoints, parâmetros, códigos HTTP, projeção PostResponse, tags e identificação dos episódios permanecem.
- JPQL do repository de posts, derivados de usuários/episódios e ordenação createdAt/id continuam funcionando.
- Nenhum rename de video_url/videoUrl; nenhuma tradução de USER/ADMIN.
- POM e dependências não foram modificados.

**Compatível com a API atual, incompatível com o schema físico antigo.** O binário desta etapa exige a base nova; não apontá-lo para o banco legado e não voltar o binário antigo sobre o banco novo.

## 4. Configurações

`application.yaml`:

- Flyway desativado no startup (`enabled: false`), pois a migration será executada externamente.
- Locations explícitas em db/fresh-migration.
- baseline-on-migrate=false e clean-disabled=true preservados.
- ddl-auto=validate preservado: Hibernate não cria o schema.

`application-dev.yaml`:

- Flyway também desativado.
- baseline-on-migrate=false; removida a configuração de baseline-version que não se aplica à criação de uma base vazia.

`.run/BackApplication.run.xml` foi preservado byte a byte. Continua usando BackApplication, dev e EnvFile. Seu startup agora herda a política sem migrations automáticas. Ainda pode conectar ao alvo incorreto se .env estiver incorreto; não usar credenciais de produção na IDE.

Os testes MariaDB habilitam explicitamente Flyway e validate **somente contra as URLs dos contêineres criados pelo próprio teste**. Não leem DB_URL da Hostinger. As antigas migrations continuam empacotadas, mas excluídas pela localização ativa; conferir overrides de ambiente antes de qualquer release.

## 5. Testes e evidências

Fontes:

- [NamingCompatibilityTests.java](../../../../src/test/java/br/com/codejr/podiss/backend/NamingCompatibilityTests.java): oito testes de contrato e persistência, atualizados para os novos nomes físicos.
- [FreshSchemaIT.java](../../../../src/test/java/br/com/codejr/podiss/backend/FreshSchemaIT.java): herda os oito testes e adiciona verificações específicas em MariaDB real descartável.
- BackApplicationTests existente: inicialização em H2.

O teste MariaDB usa Testcontainers já presente no POM, imagem **mariadb:11.8.9**, com credenciais somente de teste e portas locais efêmeras. O responsável informou 11.8.9-MariaDB-log durante a etapa; o teste foi ajustado para essa versão, após uma rodada preliminar em 11.4.5. Isso reproduz a versão do servidor, não todas as configurações, plugins ou privilégios da Hostinger. Docker é utilizado no computador de desenvolvimento, nunca na hospedagem compartilhada.

### Cobertura

- Flyway descobre e aplica somente V1__fresh_schema.sql.
- Hibernate validate e inicialização Spring após migration.
- Segunda execução de migrate não aplica nada e preserva um registro inserido após a instalação.
- Banco não vazio sem histórico é recusado; marcador e dados existentes permanecem.
- PKs/mapeamentos, índices nomeados e unique de youtube_id; diferença de caixa respeitada.
- CRUD/JPQL/queries derivadas/projeção, contratos JSON e endpoints, permissões.
- Cadastro e login com papel ADMIN persistido e senha não armazenada em texto puro.
- Round-trip de imagem binária, conteúdo Unicode, tags e timestamp com microssegundos.

Não foi testado upgrade do schema antigo: esse caminho foi conscientemente substituído por instalação limpa. O teste de recusa comprova que a cadeia nova não é tratada como migração automática do legado.

### Comando de reprodução (PowerShell, raiz do repositório)

Docker local deve estar ativo. O parâmetro api.version foi necessário neste computador para compatibilizar a biblioteca Docker Java existente com Docker Engine 29.2.1, sem atualizar dependências.

```powershell
.\mvnw.cmd -B -ntp clean verify `
  "-Dapi.version=1.44" `
  "-Dspring.profiles.active=stage4-verification" `
  "-Dspring.flyway.enabled=false" `
  "-Dspring.datasource.url=jdbc:h2:mem:stage4-verification;MODE=MariaDB;DB_CLOSE_DELAY=-1" `
  "-Dspring.datasource.driver-class-name=org.h2.Driver" `
  "-Dspring.datasource.username=sa" `
  "-Dspring.datasource.password=" `
  "-Dspring.jpa.hibernate.ddl-auto=create-drop" `
  "-Dapp.cors.allowed-origins=http://localhost:5173"
```

Os argumentos H2 isolam os testes rápidos. DynamicPropertySource do IT sobrescreve essas propriedades com o MariaDB descartável e ddl-auto=validate, Flyway habilitado e localização nova. Nenhum profile de produção ou .env foi carregado.

Resultados finais e limitações estão registrados ao final deste documento.

## 6. Como irá para a Hostinger

**Não basta enviar um SQL e colocar o JAR em public_html.** Banco e execução Java são componentes distintos. O responsável confirmou o plano **Business Web Hosting**, MariaDB **11.8.9-MariaDB-log** e painel indicando backups diários. O conteúdo enviado não identifica um processo JVM.

A documentação oficial da Hostinger informa que Java não é suportado nos planos Web/Cloud e indica VPS como opção. Isso é um **bloqueio de deploy neste plano**, não de execução dos testes locais. Referência consultada em 20/09/2026: [linguagens suportadas pela Hostinger](https://www.hostinger.com/support/which-programming-languages-and-frameworks-are-supported-at-hostinger/).

Decisão pendente: identificar eventual runtime Java já existente fora desse plano, ou escolher ambiente compatível (VPS/serviço Java). Manter o banco na Hostinger dependerá de acesso remoto permitido, TLS e conectividade desse runtime. Não foi contratada infraestrutura, decidido provedor novo ou reescrito backend em outra linguagem. A exibição de backups diários não comprova restore testado nem retenção.

### Fluxo recomendado D1

1. Confirmar versão MariaDB, cota de segunda base, banco exclusivo do backend, acesso remoto/TLS e onde a JVM roda.
2. Guardar exportação de precaução via hPanel/phpMyAdmin, conforme etapa 03, ou registrar decisão explícita sobre recuperação; não excluir nada como parte desta etapa.
3. Criar a nova base/usuário pelo hPanel, sem root/SUPER; confirmar nomes completos com prefixos do provedor.
4. Aplicar somente a migration inicial nova pelo executor aprovado.
5. Publicar o JAR testado no runtime Java efetivamente disponível; configurar datasource novo, profile prod e secrets.
6. Validar primeiro ADMIN, login, formulários, posts/imagens, episódios e frontend.
7. Interromper instâncias/escritores antigos, fazer corte coordenado e observar.
8. Somente depois do aceite e autorização específica considerar excluir o banco antigo.

O build gera `target/podiss-backend-1.1.0.jar`. O número é o já existente no POM; identificar a entrega também por commit/hash, não apenas pelo nome 1.1.0.

### Execução Flyway local/CI — modelo, não executado na Hostinger

Usar CLI compatível com a versão do projeto (Flyway 10.20.1), driver/suporte MariaDB e versão do servidor verificados. Os testes deste relatório usam API Java do Flyway, **não validaram a distribuição CLI externa**.

Fornecer `FLYWAY_URL`, `FLYWAY_USER`, `FLYWAY_PASSWORD` por variáveis de ambiente/secrets do executor, com URL JDBC da **nova base vazia**, TLS conforme provedor. Não passar senha por argumento de linha de comando nem registrá-la em logs. `DB_*` configura a aplicação; `FLYWAY_*` configura a CLI.

Exemplo executado futuramente na raiz da cópia do repositório:

```powershell
# Somente depois de conferir o alvo e receber aprovação operacional.
$flywayArgs = @(
  "-locations=filesystem:src/main/resources/db/fresh-migration",
  "-baselineOnMigrate=false",
  "-cleanDisabled=true",
  "-failOnMissingLocations=true",
  "-validateMigrationNaming=true"
)
flyway @flywayArgs info
# Conferir: exclusivamente versão 1, fresh schema, banco novo correto.
flyway @flywayArgs validate
flyway @flywayArgs migrate
flyway @flywayArgs validate
flyway @flywayArgs info
```

Em pipeline, abortar imediatamente se qualquer comando retornar código diferente de zero; serializar execuções e guardar logs redigidos/hash da migration. Não montar cadeia legada nem incluir db/migration na location. Não usar baseline numa base vazia.

Se houver erro, parar: não executar clean/repair/baseline para “resolver” automaticamente. A disponibilidade do comando `flyway` e suas dependências precisa ser preparada no executor, não no servidor compartilhado.

### Se a porta remota estiver bloqueada

A nova migration é SQL puro, portanto pode ser aplicada manualmente no phpMyAdmin **em base vazia confirmada e após aprovação**. Isso cria schema, mas não equivale a execução Flyway e não cria um histórico legítimo de execução da V1.

Registrar arquivo/hash/operador/data, verificar schema completo e tratar falha parcial antes de repetir. Para adoção posterior do Flyway, será necessário acesso por algum executor autorizado, comparação do schema com a V1 e baseline explícito da versão já materializada. Não inserir histórico manualmente nem disparar migrate sobre esse banco sem reconciliação.

Se não houver nenhuma forma de executor se conectar, o uso manual contínuo precisa de decisão própria. Não afirmar que a porta bloqueada foi resolvida apenas pela existência do SQL.

### Configuração do backend

Manter `SPRING_PROFILES_ACTIVE=prod`, Flyway desativado no runtime, ddl-auto=validate e credenciais da base nova; JWT_SECRET válido, CORS_ALLOWED_ORIGINS do frontend e configurações externas necessárias. ADMIN_USERNAME/EMAIL/PASSWORD devem ser fornecidos com segurança para o bootstrap inicial, nunca como seed SQL com senha fixa. Confirmar política de tokens antigos antes de abrir.

Comando de início/restart, localização do JAR, proxy e TLS serão documentados após identificar o ambiente real de Java. Hospedagem compartilhada/phpMyAdmin, por si só, não comprova capacidade de executar o JAR. Nenhum procedimento de VPS/systemd/Docker no servidor foi presumido.

## 7. Riscos e recuperação

- Native UUID é compatível com a versão 11.8.9 informada; configuração/driver/privilégios reais ainda precisam de confirmação.
- Collations/privilegios/cotas do provedor precisam de ensaio equivalente.
- DDL pode ficar parcialmente aplicado. No alvo novo ainda descartável, recriação pode ser apropriada, mas exige identificar/autorização daquele alvo; não automatizada.
- Após receber dados úteis, usar novas migrations de correção e backup; não reutilizar a premissa de descarte.
- Não editar V1 após aplicada na cadeia nova.
- Antes do corte, falha no novo ambiente permite cancelar a publicação mantendo a origem.
- Depois do corte, voltar ao binário antigo exige também banco/configuração antigos; não existe rollback somente de JAR compatível com o schema novo.
- Voltar ao estado antigo pode significar voltar a uma aplicação infuncional; isso não é recuperação funcional garantida.
- Não foram diagnosticados logs reais, testada integração com frontend publicado, API externa YouTube, backup/restore Hostinger ou runtime Java de produção.

## 8. Pré-condições para produção e encerramento

- [x] Versão MariaDB informada pelo responsável: 11.8.9-MariaDB-log.
- [ ] Acesso remoto/TLS confirmado.
- [ ] Cota para base paralela e nome do alvo confirmados.
- [ ] Runtime Java e processo de publicação/restart esclarecidos.
- [ ] Testes repetidos no ambiente equivalente ao servidor real e na JVM de produção.
- [ ] CLI/CI ou procedimento manual aprovado e validado.
- [ ] Backup/retenção, administrador inicial e política de tokens definidos.
- [ ] Logs da falha atual analisados e fluxos funcionais validados.
- [ ] Corte e eventual exclusão aprovados separadamente.

O responsável se ofereceu para obter as informações da Hostinger. Informou versão MariaDB e plano Business Web Hosting; segunda base, acesso remoto e funcionamento da JVM continuam pendentes. Não foram solicitadas senhas/acesso à conta.

Esta entrega não aprova deploy. O avanço posterior depende da definição de um procedimento de deploy viável, conforme atualização do responsável registrada abaixo.

## 9. Resultado final da validação local

**BUILD SUCCESS**, `clean verify` final em 20/09/2026, com MariaDB **11.8.9**:

| Suíte | Testes | Falhas | Erros | Ignorados |
| --- | ---: | ---: | ---: | ---: |
| BackApplicationTests (H2) | 1 | 0 | 0 | 0 |
| NamingCompatibilityTests (H2) | 8 | 0 | 0 | 0 |
| FreshSchemaIT (MariaDB 11.8.9) | 13 | 0 | 0 | 0 |
| **Total** | **22** | **0** | **0** | **0** |

Compilação com release 21, execução em JVM **23.0.2**, não em Java 21. Ainda é necessário repetir na JVM escolhida para produção. A rodada preliminar em MariaDB 11.4.5 também passou, mas a evidência final é 11.8.9, informada pelo responsável.

Avisos/limitações:

- Flyway **10.20.1** informa que sua faixa de MariaDB declarada testada vai até 11.2. A migration e os testes passaram em 11.8.9, mas isso não elimina a necessidade de avaliar suporte/atualização em etapa própria. Nenhuma dependência foi atualizada.
- A primeira tentativa falhou na negociação da API Docker antes de qualquer migration; o argumento local `-Dapi.version=1.44` resolveu a execução com Docker Engine 29.2.1.
- Mockito/Byte Buddy emitem aviso de autoanexação no Java 23; H2 emite aviso de open-in-view. O teste MariaDB desativa open-in-view explicitamente.
- O erro de duplicata nos logs do teste é proposital e verificado, não falha de build.
- Não foi simulada falha em cada posição do DDL; recuperação de migration parcial é plano documentado, não garantia testada.
- Não foi exercitado bootstrap inicial ADMIN em MariaDB nesta suíte; cadastro/login/persistência de papel foram testados. Provisionamento inicial continua sendo smoke test obrigatório antes da abertura.
- CLI externa, TLS remoto, privilégios do hPanel, frontend publicado e backup/restore do provedor não foram testados.
- Não há contêiner Testcontainers em execução após a suíte. Docker Desktop foi iniciado localmente e as imagens de teste baixadas permanecem em cache; o serviço Docker não foi desligado.

### Integridade e documentação

Comparação SHA-256 confirmou que V1 histórica, V2 Java histórica, .run e POM permaneceram byte a byte iguais ao início da implementação.

SHA-256 da nova migration nesta entrega:

`D2C921532BE6C7033873FFBA9340D23E682FAC071F325A5289DDC2AB3A4752FE`

Os READMEs de resources principal/teste foram atualizados para não recomendar a cadeia antiga, baseline automático ou exemplo de teste inexistente. Os relatórios anteriores permanecem históricos.

O repositório já tinha alterações anteriores não commitadas; esta etapa não fez commit, push ou certificação da totalidade dessas mudanças para produção. As mudanças desta etapa se limitam à migration inicial nova, mapeamentos/services necessários, configuração do executor, testes e documentação.

**Etapa 04 concluída para revisão local. Deploy bloqueado pela definição do runtime Java.** O responsável autorizou avançar às próximas etapas quando houver um procedimento de deploy definido; essa condição ainda não foi atendida. Foi solicitada a escolha do ambiente Java. Nenhuma compra, alteração remota ou exclusão de banco será inferida dessa autorização.
