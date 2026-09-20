z# Etapa 03 — Revisão da estratégia: reconstrução do banco

Créditos/responsável: **oEnzoRibas**. Data da revisão: 20/09/2026.
Status: **proposta revisada, aguardando decisão do responsável. Nenhuma implementação ou exclusão autorizada por este relatório.**

Referências: [plano mestre](../master-plan.md), [etapa 03](../etapas/etapa03.md), [baseline da etapa 01](etapa01-auditoria-e-inventario.md) e [etapa 02](etapa02-padronizacao-interna.md).

## 1. Nova premissa e conclusão

O responsável esclareceu que **não há dados reutilizáveis no banco e a aplicação publicada está infuncional**. Esta revisão aceita essa informação como premissa de negócio, sem alegar inspeção do banco ou diagnóstico da indisponibilidade. Ela substitui a recomendação anterior de preservar obrigatoriamente o schema legado.

**Sim: com descarte integral dos dados e corte coordenado, criar o schema definitivo em inglês numa base vazia tende a ser significativamente mais simples do que migrar o legado.** Não é preciso converter registros, corrigir duplicatas antigas, manter IDs, fazer backfill, dual-write ou expand-and-contract.

A recomendação passa a ser **D1 — reconstrução limpa em outra base, validação e troca coordenada de aplicação/configuração**, seguida da exclusão do banco antigo após aceite. Se a conta não permitir outra base, **D2 — backup, exclusão e recriação no mesmo ambiente** é alternativa viável, com maior risco operacional e recuperação dependente de backup. A escolha entre D1 e D2 é do responsável.

Isso é diferente de simplesmente executar RENAME no banco antigo. A criação limpa deve já conter tabelas/colunas/constraints finais; renomear primeiro uma base que será apagada é trabalho sem benefício.

### 1.1 O que mudou na avaliação

| Premissa anterior | Informação nova | Consequência |
| --- | --- | --- |
| Dados reais precisavam ser preservados | Dados declarados sem reutilização | Não exigir conversão nem conciliação registro a registro |
| Compatibilidade do banco antigo era prioridade | Aplicação infuncional e novo início em avaliação | Corte coordenado é opção razoável; não é necessário rolling deploy |
| Tradução física era custo sem benefício suficiente | Base será criada do zero | Definir nomes finais evita carregar dívida nominal para a nova instalação |
| Evolução incremental do legado | Reconstrução de uma instalação | Migration inicial completa pode substituir o caminho de upgrade, em uma linhagem nova explicitamente separada |

**O que não mudou:** excluir banco é destrutivo; frontend e integrações podem continuar dependendo da API; apagar dados não corrige defeitos de aplicação, credenciais, rede, JWT, CORS ou execução da JVM. Não foi demonstrado que o banco seja a causa da aplicação estar infuncional.

Esta análise não autoriza apagar site, arquivos, domínio, outros bancos, usuários compartilhados ou backups. “Nada reutilizável” fundamenta a proposta de descarte, mas o pedido atual manda aguardar decisão, não executar o descarte.

### 1.2 Evidências mantidas

O inventário abaixo descreve a cópia de trabalho após a etapa 02: cinco entities, trinta atributos persistentes e cinco repositories. O schema real e o artefato publicado continuam não verificados. A referência histórica é o HEAD `c1edc6cb9151ad7794f2f131fced735899f1456c`, branch `refactor-fixall`; há mudanças anteriores ainda locais, V1 modificada e V2 Java não rastreada.

O mapa da seção 2 é preservado como **estado atual**, não como desenho recomendado do banco novo. Seções seguintes descrevem o destino proposto. Não foram alterados código, migrations, configurações ou .run. Nenhum SQL, teste, build, startup ou acesso ao banco foi executado nesta revisão.

## 2. Mapa Java/JPA → API → Flyway → MariaDB

### 2.1 Convenções e entidades

Arquivos Java sob `src/main/java/br/com/codejr/podiss/backend/`. Migrations em `src/main/resources/db/migration/V1__database.sql` e `src/main/java/db/migration/V2__functional_schema.java`.

| Classe/arquivo | Nome lógico JPA | @Table explícito | Repository | API associada |
| --- | --- | --- | --- | --- |
| contact/ContactMessage.java | Contato, explícito | contatos | ContactMessageRepository | POST e GET /contatos |
| suggestion/TopicSuggestion.java | Sugestao, explícito | sugestoes | TopicSuggestionRepository | POST e GET /sugestoes |
| episode/Episode.java | Video, explícito | episodes | EpisodeRepository | GET/POST /episodes; GET /episodes/{youtubeId}; DELETE /episodes/{id} |
| post/Post.java | Post, inferido | posts | PostRepository | /posts, /posts/{id}, /posts/{id}/image e GET /posts/image/{id} |
| user/User.java | User, inferido | users | UserRepository | POST /api/auth/login e /api/auth/register |

Não há relacionamentos JPA, tabelas de junção, FKs ou sequences explicitamente declarados nestes fontes/migrations. Isso não prova sua ausência no servidor. Tags são uma String, não uma entidade; imagens são BLOB na própria tabela; episódios não possuem FK para posts ou usuários.

Todos os IDs são `UUID`, com `@Id @GeneratedValue @JdbcTypeCode(SqlTypes.UUID)`. Não há gerador/sequence explicitamente nomeado. V1/V2 locais declaram tipo SQL UUID e não definem default SQL para gerar IDs. A geração pela aplicação não deve ser confundida com geração pelo banco para clientes SQL externos.

Nas tabelas abaixo:

- **I** = nome físico inferido, sem `@Column(name=...)`.
- **E** = nome físico explicitamente declarado.
- NN = não nulo; N = aceita nulo.
- Tipos/limites representam as annotations e as migrations locais, não um introspect do MariaDB.
- Campos camelCase dependem da estratégia física de nomes. Não foi encontrada estratégia customizada; a expectativa é snake_case. A confirmação final deve usar o schema real.

### 2.2 ContactMessage → contatos

| Atributo Java | Coluna | @Column e tipo esperado | API | Proveniência local |
| --- | --- | --- | --- | --- |
| id: UUID | id (I) | PK UUID; annotations de ID, sem @Column | resposta id | V2 cria tabela/PK |
| nome: String | nome (I) | NN, length=255 → VARCHAR(255) | entrada e saída nome | V2 |
| email: String | email (I) | NN, length=255 → VARCHAR(255) | entrada e saída email | V2 |
| assunto: String | assunto (I) | NN, length=255 → VARCHAR(255) | entrada e saída assunto | V2 |
| mensagem: String | mensagem (I) | NN, columnDefinition=TEXT | entrada e saída mensagem | V2 cria e modifica tipo |
| createdAt: Timestamp | created_at (I) | NN, updatable=false; SQL TIMESTAMP(6) | resposta createdAt | V2 adiciona com DEFAULT CURRENT_TIMESTAMP |

`CreateContactMessageRequest` exige nome/assunto até 255, email até 254 e mensagem até 10000 caracteres. Serviço preenche data e remove espaços das extremidades. GET é administrativo; POST devolve a entidade criada. Os campos portugueses são simultaneamente propriedades Java, JSON e nomes físicos.

### 2.3 TopicSuggestion → sugestoes

| Atributo Java | Coluna | @Column e tipo esperado | API | Proveniência local |
| --- | --- | --- | --- | --- |
| id: UUID | id (I) | PK UUID; sem @Column | resposta id | V2 cria tabela/PK |
| nome: String | nome (I) | NN, length=255 → VARCHAR(255) | entrada e saída nome | V2 |
| email: String | email (I) | NN, length=255 → VARCHAR(255) | entrada e saída email | V2 |
| tema: String | tema (I) | NN, length=2000 → VARCHAR(2000) | entrada e saída tema | V2 cria e modifica tipo |
| createdAt: Timestamp | created_at (I) | NN, updatable=false; SQL TIMESTAMP(6) | resposta createdAt | V2 adiciona com DEFAULT CURRENT_TIMESTAMP |

`CreateTopicSuggestionRequest` limita nome a 255, email a 254 e tema a 2000. Serviço preenche data e faz trim. GET administrativo, POST devolve entidade.

### 2.4 Episode → episodes

| Atributo Java | Coluna | @Column e tipo esperado | API | Proveniência local |
| --- | --- | --- | --- | --- |
| id: UUID | id (I) | PK UUID; sem @Column | resposta id e DELETE por UUID | V1 |
| title: String | title (I) | NN, length padrão 255 | saída title; obtido do YouTube | V1 |
| description: String | description (I) | NN, columnDefinition=TEXT | entrada opcional e saída description | V1; V2 reafirma TEXT NN |
| youtubeId: String | youtube_id (I) | NN, unique=true, length=11 | saída youtubeId e GET por ID YouTube | V2 adiciona, preenche, exige NN e cria unique |
| videoUrl: String | video_url (I) | NN, length padrão 255 | entrada e saída videoUrl | V1; V2 reescreve URLs |
| thumbnailUrl: String | thumbnail_url (I) | N, length=512 | saída thumbnailUrl | V2 adiciona |
| createdAt: Timestamp | created_at (I) | NN, updatable=false; SQL TIMESTAMP(6) | saída createdAt | V1; V2 preenche nulos e altera definição |

O DTO aceita videoUrl até 2048 caracteres; serviço normaliza para URL canônica curta antes de persistir. Não é, sozinho, evidência de overflow da coluna de 255. A V2 também normaliza URLs antigas, perdendo a representação original; precisa de aprovação como transformação de dados. GET usa ID YouTube, DELETE usa UUID: diferença contratual deliberadamente preservada.

### 2.5 Post → posts

| Atributo Java | Coluna | @Column e tipo esperado | API | Proveniência local |
| --- | --- | --- | --- | --- |
| id: UUID | id (I) | PK UUID; sem @Column | resposta id, paths UUID | V1 |
| title: String | title (I) | NN, length padrão 255 | entrada/saída title | V1 |
| description: String | description (I) | NN, columnDefinition=TEXT | entrada/saída description | V1; V2 reafirma definição |
| tags: String | tags (I) | NN, length=2048; inicializador Java "" | entrada lista tags; saída String tags | V1 permite nulo; V2 preenche "" e exige NN com default "" |
| createdAt: Timestamp | created_at (E) | name=created_at, NN, updatable=false; SQL TIMESTAMP(6) | resposta createdAt | V1; V2 preenche nulos e altera definição |
| image: byte[] | image (I) | @Lob, columnDefinition=LONGBLOB, N | multipart image; resposta binária separada | V1 |
| imageContentType: String | image_content_type (I) | N, length=100 | Content-Type da imagem; não campo direto de PostResponse | V2 adiciona e infere por assinatura |

`PostResponse` expõe id/title/description/tags/createdAt/hasImage e imageUrl calculado. **hasImage e imageUrl não são colunas.** Tags, apesar da entrada em lista, são persistidas separadas por vírgula. A projeção calcula hasImage pela presença de imageContentType, não lendo o BLOB. Precisam de validação os registros legados com imagem sem MIME ou MIME sem imagem.

### 2.6 User → users

| Atributo Java | Coluna | @Column e tipo esperado | API | Proveniência local |
| --- | --- | --- | --- | --- |
| id: UUID | id (I) | PK UUID; sem @Column | não exposto pelos endpoints atuais de autenticação | V1 |
| username: String | username (I) | NN, unique=true, length=255 | login/cadastro username | V1 |
| password: String | password (I) | NN, length padrão 255 | senha de entrada; hash persistido, não devolvido | V1 |
| email: String | email (I) | NN, unique=true, length=255 | cadastro email | V1 |
| role: User.Role | role (I) | @Enumerated(STRING), NN, length=16 | cadastro role e autorização interna | V2 adiciona com default USER |

Enum Java: USER e ADMIN. O SQL declara VARCHAR, não ENUM nem CHECK restringindo esses valores. Valores inesperados podem falhar na hidratação/autorização. Não traduzir enum persistido como se fosse rename JAVA_ONLY.

Não existe endpoint atual devolvendo diretamente User; o cadastro retorna mensagem e login retorna JWT. A entidade, entretanto, tem getter de password: uma futura exposição direta seria perigosa.

### 2.7 PKs, uniques, índices e consultas

| Tabela | PK | Unique conhecido | Índices secundários explicitamente nomeados na V2 |
| --- | --- | --- | --- |
| contatos | id | nenhum declarado | idx_contatos_created (created_at, id) |
| sugestoes | id | nenhum declarado | idx_sugestoes_created (created_at, id) |
| episodes | id | youtube_id; JPA unique=true | uk_episode_youtube_id (youtube_id); idx_episodes_created (created_at, id) |
| posts | id | nenhum além da PK | idx_posts_created (created_at, id) |
| users | id | username e email, sem nomes explícitos no SQL/JPA | nenhum outro declarado |

A grafia `uk_episode_youtube_id` usa singular enquanto tabela/índice temporal usam plural. Os nomes reais das uniques de users ou de constraints criadas pelo Hibernate só podem ser obtidos do servidor. Não inventar nomes para removê-las. Não há `@Index` nas entidades.

| Repository/consumidor | Dependências |
| --- | --- |
| ContactMessageRepository | CRUD/paginação JPA; entidade lógica Contato; tabela contatos |
| TopicSuggestionRepository | CRUD/paginação JPA; entidade lógica Sugestao; tabela sugestoes |
| EpisodeRepository | findByYoutubeId e existsByYoutubeId dependem do atributo youtubeId, não literalmente de youtube_id |
| PostRepository.findSummaries | JPQL com Post, construtor PostResponse e atributos id/title/description/tags/createdAt/imageContentType |
| UserRepository | findByUsername, existsByUsername, existsByEmail, existsByRole; nomes derivados de atributos Java |
| PaginationSupport | ordenação pelos atributos createdAt e id nas quatro coleções |
| V2 Java | SQL físico direto em users/posts/episodes/contatos/sugestoes, independente dos novos nomes das classes |

Não foram encontradas queries nativas nos repositories. SQL físico está na V2. JPQL usa nomes lógicos/atributos; SQL usa tabelas/colunas. Uma troca de nome Java pode exigir ajuste em JPQL, método derivado e sort mesmo quando o nome físico é preservado.

## 3. Comparação das alternativas após a nova informação

| Opção | Trabalho principal | Preserva dados antigos? | Risco e adequação |
| --- | --- | --- | --- |
| A — Java em inglês, físico legado | Mapeamentos explícitos; ainda resolver divergências do schema | Sim | Menor alteração de nomes, mas não resolve por si só o estado funcional; deixou de ser a recomendação principal |
| B — renomear tabelas/colunas existentes | Inventariar estado real, aplicar ALTER/RENAME, ajustar código | Sim | Mantém estrutura/dados descartáveis e exige conciliar migrations; pouco benefício neste cenário |
| C — expand-and-contract | Campos/tabelas paralelos, sincronização, backfill, várias releases | Sim | Complexidade desproporcional se não há dados nem operação saudável a preservar |
| **D1 — base nova paralela** | Criar schema final, testar, trocar binário e datasource juntos | Não importa os dados; conserva origem temporariamente | **Recomendada**, se houver cota: simplifica schema e preserva opção de retorno |
| **D2 — excluir/recriar antes do corte** | Preparar tudo, backup, parada, excluir alvo exato e recriar | Não; só backup opcional de recuperação | Viável se aceita perda total e indisponibilidade; maior risco por eliminar origem antes de validar |

D1 requer outra base e novas configurações, mas não precisa de cópia de dados nem sincronização. D2 evita manter duas bases, porém uma falha de criação, migration ou conexão amplia o período de indisponibilidade. Não há estimativa confiável de duração antes de ensaio.

O banco pode ser descartável; a conta de hospedagem e os demais recursos não são. A autorização de exclusão futura deverá identificar o **nome completo do banco alvo** e seu vínculo exclusivo com o backend.

## 4. Nomenclatura proposta para a instalação limpa

### 4.1 Mapa de mudanças e justificativa

Os demais campos, tipos Java, endpoints e propriedades da seção 2 permanecem como referência, salvo propostas explícitas abaixo. Nenhuma mudança foi aplicada.

| ID | Estado atual → destino físico novo | Java proposto | Dependências e motivo | Avaliação para base vazia |
| --- | --- | --- | --- | --- |
| R1 | contatos → contact_messages | ContactMessage; @Table atualizado | Repository/SQL e índice temporal; registro representa mensagem recebida | Justificado na criação inicial |
| R2 | sugestoes → topic_suggestions | TopicSuggestion; @Table atualizado | Repository/SQL e índice temporal; explicita sugestão de tema | Justificado na criação inicial |
| R3 | contatos.nome → contact_messages.sender_name | nome → senderName | Getters/setters/service/DTO/Jackson; identifica remetente | Justificado, manter JSON nome inicialmente |
| R4 | sugestoes.nome → topic_suggestions.sender_name | nome → senderName | Mesmas fronteiras de R3 | Justificado, manter JSON nome inicialmente |
| R5 | contatos.assunto → contact_messages.subject | assunto → subject | Service/DTO/Jackson; assunto da mensagem | Justificado |
| R6 | contatos.mensagem → contact_messages.message | mensagem → message | Service/DTO/Jackson; conteúdo textual | Justificado |
| R7 | sugestoes.tema → topic_suggestions.topic | tema → topic | Service/DTO/Jackson; tema sugerido | Justificado |
| R8 | idx_contatos_created → idx_contact_messages_created_at_id | Sem rename de propriedade | Criar índice (created_at,id) com nome final | Justificado ao criar; não renomear índice antigo antes de apagar |
| R9 | idx_sugestoes_created → idx_topic_suggestions_created_at_id | Sem rename de propriedade | Idem R8 | Justificado ao criar |
| R10 | uk_episode_youtube_id → uk_episodes_youtube_id | youtubeId preservado | Unique de episodes.youtube_id | Nome final consistente sem custo de conversão |
| R11 | idx_posts_created / idx_episodes_created → idx_posts_created_at_id / idx_episodes_created_at_id | createdAt preservado | Índices temporais existentes no desenho | Criar diretamente com nomes finais |
| R12 | episodes.video_url → youtube_url | Candidato videoUrl → youtubeUrl | Já está em inglês; alteraria mapeamento e poderia afetar API sem necessidade | **Não recomendado**, preservar video_url/videoUrl |

Nomes finais das tabelas: **users, posts, episodes, contact_messages, topic_suggestions**.

Para objetos novos, propor uniques explicitamente nomeadas `uk_users_username`, `uk_users_email` e `uk_episodes_youtube_id`; índices temporais conforme tabela. Isso não afirma conhecer os nomes das constraints atuais. PK continua id; não inventar FKs ou relacionamentos ausentes do domínio.

`email` já é claro no contexto de contact_messages/topic_suggestions: preservar, sem tradução redundante para sender_email. Preservar title, description, tags, image, image_content_type, thumbnail_url, youtube_id, created_at e valores USER/ADMIN. Não aproveitar a reconstrução para redesign de tags, armazenamento de imagem ou relacionamentos sem escopo próprio.

### 4.2 Impacto por fronteira

| Mudança | JPA e queries | API | Flyway e dados | Versão antiga |
| --- | --- | --- | --- | --- |
| R1–R2 | Atualizar @Table e consumidores SQL físicos | Rotas podem permanecer /contatos e /sugestoes | DATABASE_IMPACT; criar diretamente no schema novo; sem transferência de registros | Binário antigo não é compatível com essas tabelas novas |
| R3–R7 | @Column explícito novo; atualizar acessos Java, JPQL/sorts/derivados se houver | Preservar nome/assunto/mensagem/tema via DTO/mapeamento; sem isso há API_IMPACT | DATABASE_IMPACT + refatoração Java; nova definição, não ALTER no legado | Não apontar binário antigo para novo schema |
| R8–R11 | Sem mudança esperada de CRUD; revisar hints/scripts externos | Sem impacto HTTP esperado | Definições na nova migration inicial; não reutilizar histórico antigo | Retorno exige par antigo de aplicação e banco |
| Novos DB_URL/DB_USERNAME/DB_PASSWORD | Muda conexão efetiva | Sem mudança de contrato pretendida | CONFIGURATION_IMPACT; secrets fora de código/docs | Configuração antiga deve acompanhar artefato antigo |

A decisão de descartar o banco **não autoriza quebrar JSON ou endpoints**. Mesmo infuncional, o frontend existente pode conter formulários que dependem dos nomes portugueses. Recomenda-se manter o contrato público neste corte; tradução de API requer inventário dos clientes e decisão separada.

Classes/packages já padronizados na etapa 02 continuam aproveitáveis. Beans e nomes lógicos JPA legados não são objetos do banco e podem permanecer por enquanto. Sua retirada é JAVA_ONLY somente após revisar JPQL/reflexão/testes; não é condição para criar schema físico em inglês. Os testes de compatibilidade terão de distinguir contrato HTTP preservado de nomes físicos deliberadamente substituídos.

### 4.3 O que “renomear” não resolve

Além da nomenclatura, a instalação nova precisa representar corretamente as regras atuais: role, youtube_id, MIME, timestamps, nulabilidade, tamanhos e unicidade. Renomear as três tabelas da V1 ou apenas traduzir a V2 não garante schema completo.

Há decisões técnicas pendentes mesmo sem dados legados:

- Confirmar versão MariaDB e suporte ao tipo UUID usado pela aplicação; MariaDB suporta UUID na linha 10.7 em diante. Não tratar MySQL como equivalente. [MariaDB UUID](https://mariadb.com/docs/server/reference/data-types/string-data-types/uuid-data-type).
- Especificar engine/charset/collation em vez de herdar defaults desconhecidos. Proposta: InnoDB e utf8mb4 compatíveis com a versão confirmada, com tratamento explícito sensível à caixa para youtube_id.
- Definir semântica de unicidade de username/email e testar maiúsculas, acentos e espaços.
- Preservar UUIDs gerados pela aplicação, tipos/limites adequados, timestamps com UTC e criação de registros pelo serviço. Não há necessidade de converter IDs antigos.
- Criar o primeiro administrador por mecanismo seguro, sem senha fixa em migration, e validar autenticação antes da abertura.
- Investigar a falha funcional real antes da troca: logs sem secrets, inicialização, datasource, runtime, proxy, JWT/CORS e fluxo com frontend. A recriação é estratégia de instalação, não diagnóstico.

## 5. Flyway: instalação inicial nova, não upgrade do legado

### 5.1 Estado encontrado e o que deixa de ser necessário

Atualmente há V1 SQL, com users/posts/episodes, e V2 Java que complementa schema e transforma dados. V1 está modificada em relação ao HEAD; V2 não está rastreada. V2 usa checksum constante 2026091902 e declara execução não transacional.

Para um começo vazio, não há benefício em executar a lógica de conversão de URLs, correção de tags, preenchimento de datas antigas e inferência de MIME antigo. **Recomenda-se uma migration SQL inicial completa da nova instalação**, contendo diretamente as cinco tabelas finais, constraints e índices, sem importar registros antigos.

Não recomendar “renomear V2 para V1” ou editar silenciosamente a V1 possivelmente aplicada. A nova cadeia exige tratamento explícito de linhagem.

### 5.2 Política proposta para a nova linhagem

1. Preservar o estado legado em referência versionada/arquivo histórico rastreável antes de qualquer substituição futura, inclusive V2 local não commitada. Este relatório não criou commit, tag ou arquivo de migration.
2. Confirmar se existe outro ambiente que precise continuar usando a cadeia antiga. Se existir, mantê-la separada e imutável.
3. Após aprovação, definir localização/classpath exclusiva para a cadeia de **instalação nova**, por exemplo `db/migration/fresh`, com a migration inicial própria. Nome/localização são proposta, não arquivos criados.
4. A seleção de locations deve excluir a cadeia antiga no executor da nova instalação. Não confiar apenas em subpasta: a descoberta de um diretório pai pode alcançar subdiretórios. Testar SQL e classes Java presentes no classpath; V2 Java antiga não pode ser executada acidentalmente.
5. Usar nova V1 apenas dentro dessa linhagem isolada e num banco novo, sem reaproveitar o histórico anterior. Não ter duas versões 1 descobertas simultaneamente. Registrar release, cadeia e banco associados.
6. A partir da primeira execução aprovada da cadeia nova, migrations ficam imutáveis; mudanças seguintes recebem novas versões.
7. Se for necessário oferecer upgrade para bases antigas, planejar caminho próprio. A nova instalação limpa **não promete upgrade in-place**.

Em banco vazio, Flyway deve executar a migration inicial e registrar sua execução normal. **Não fazer baseline 1 para pular a criação**; baseline não cria as tabelas e exclui migrations até a versão indicada. [Flyway baseline](https://documentation.red-gate.com/flyway/reference/commands/baseline).

A conveniência de uma nova cadeia não justifica apagar registros do schema history de uma base em uso ou usar repair para mascarar divergências. O histórico antigo fica com o banco antigo; o novo nasce de execução real.

### 5.3 Configurações observadas e recomendação futura

| Configuração | Atual | Destino proposto, não aplicado |
| --- | --- | --- |
| ddl-auto | validate em base/dev/prod; create-drop no H2 de teste | Manter validate na aplicação; schema criado por migration, não Hibernate |
| flyway.enabled | true em base/dev/prod; false em testes | Executor externo único; desativar startup migrations na release operacional correspondente |
| baseline-on-migrate | false base/prod; true dev | false para instalação nova, inclusive dev utilizado com ela |
| baseline-version | 1 em dev; padrão 1 na base | Não usar baseline no banco novo vazio |
| clean-disabled | true | Manter true; exclusão autorizada no painel não deve virar clean rotineiro |
| locations | Sem override, classpath:db/migration esperado | Selecionar explicitamente só a nova linhagem no app/executor |
| histórico | flyway_schema_history esperado; servidor não inspecionado | Novo histórico criado pelo executor; não copiar o antigo |
| JDBC timezone | UTC | Manter/testar; confirmar sessão/servidor |
| .run | BackApplication, profile dev, EnvFile lendo .env | Conferir datasource/profile/locations; jamais carregar credenciais de produção por engano |

O `.run` continua intacto. A IDE atual ativa dev com baseline automático: banco novo não elimina risco de apontar para um alvo errado. DB_URL, DB_USERNAME e DB_PASSWORD precisariam ser atualizados de forma coordenada no runtime real, não apenas em um arquivo local.

### 5.4 Hostinger e executor

O ambiente informado permanece compartilhado, sem root/Docker. Criar banco/usuário deve usar hPanel; não pressupor permissão JDBC de CREATE DATABASE ou privilégios SUPER. A documentação da Hostinger descreve criação pela área de gestão de bancos, nomes com prefixo da conta e um usuário por banco. Não depender de separação entre usuário migrador e runtime sem confirmar suporte do plano. [Hostinger — criação de banco](https://www.hostinger.com/support/1583542-how-to-create-a-new-mysql-database-in-hostinger/).

- **3306 acessível:** Flyway local/CI, versão fixada, conexão segura autorizada e secrets protegidos; validar seleção de banco e lista de migrations antes de executar.
- **3306 bloqueada:** SQL inicial torna execução por phpMyAdmin mais simples que a V2 Java atual. Porém importar SQL manualmente **não equivale a executar Flyway**. Será necessário documentar bootstrap manual e posterior adoção do histórico: comparar schema com a migration exata e só então usar baseline explícito da versão já materializada, quando houver executor com acesso. Não falsificar histórico.
- Se acesso externo continuar impossível e não houver executor autorizado com conectividade, não prometer Flyway operacional. A exceção manual precisará de decisão própria, checksums, logs e rastreabilidade; a aplicação não poderá tentar recriar as tabelas no startup.

Nenhum comando de migrate, baseline, clean, DROP ou criação de banco foi executado ou preparado como script nesta revisão.

## 6. Sequência recomendada para D1 e alternativa D2

### D1 — criar primeiro, excluir depois

Plano condicional para implementação futura:

1. Aprovar descarte lógico dos dados e preservação inicial do contrato HTTP. Identificar banco exclusivo do backend, cotas e eventuais consumidores externos.
2. Guardar evidência/configuração antiga sem secrets expostos; definir retenção breve do banco antigo ou exportação de segurança. Não importar dados antigos no novo.
3. Preparar código e migration inicial coerentes; testar localmente em MariaDB equivalente. Confirmar a causa da falha funcional e o procedimento real para executar a JVM.
4. Criar nova base/usuário via hPanel, com nomes completos confirmados e credenciais próprias. Não excluir a base antiga.
5. Executar a cadeia nova exclusivamente no alvo novo, pela via aprovada. Validar schema e Hibernate validate.
6. Validar aplicação, primeiro ADMIN, login, contatos, sugestões, posts/imagens, episódios e integração com frontend em acesso controlado.
7. Suspender escritores antigos e trocar **binário + configuração do datasource** em uma janela coordenada. Reiniciar processo/pool conforme o runtime real; não supor recarga automática de env vars.
8. Fazer smoke tests e iniciar período de observação. Não deixar instâncias antigas gravando nem permitir que artefato antigo use schema novo.
9. Após aceite, confirmar retenção, referência exata e autorização de exclusão; então descomissionar somente o banco antigo e credenciais comprovadamente exclusivas. Nenhuma exclusão é autorizada neste momento.

Vantagem principal: defeitos são descobertos antes de destruir a origem. Conservar o banco antigo não implica reaproveitar seu conteúdo e não exige complexidade de sincronização.

### D2 — excluir e recriar no mesmo ambiente

Viável quando não há cota para D1 ou o responsável prefere esse caminho e aceita as consequências:

1. Preparar e ensaiar todos os artefatos antes da exclusão; confirmar banco alvo e que nenhum outro sistema o usa.
2. Exportar backup de precaução ou registrar **renúncia explícita à recuperação dos dados antigos**, separada da autorização atual de análise.
3. Interromper todos os escritores/processos dependentes. “Infuncional” não prova ausência de gravações.
4. Excluir/recriar apenas o banco autorizado pelo hPanel; confirmar usuário, permissões e nome/credenciais resultantes. Não supor que recriação preserve usuário ou senha.
5. Criar schema final, iniciar o binário correspondente, bootstrap administrativo e smoke tests.
6. Em falha, manter manutenção e corrigir; restaurar o par antigo só se necessário e possível. Não prometer recuperação funcional usando uma aplicação já infuncional.

Apagar antes de implementar não reduz o esforço de código/testes: apenas antecipa a perda da origem. Por isso D1 é preferível quando disponível, sem tornar D2 uma opção proibida.

## 7. Backup, descarte e recuperação proporcionais ao novo cenário

Não é mais necessário exigir um projeto de migração/conciliação de todos os registros antigos. Recomenda-se uma exportação única de precaução; o responsável pode optar por descarte irrecuperável, explicitamente. Dados novos criados após a abertura **não** herdam a condição de descartáveis.

### Exportação de precaução pelo hPanel/phpMyAdmin

- Selecionar o banco exato do backend no hPanel e abri-lo no phpMyAdmin.
- Exportar SQL com estrutura e dados, incluindo histórico Flyway se existir; inventariar views/triggers/rotinas se houver e registrar limites de permissão.
- Preservar BLOBs; parar escritores durante a exportação se não houver garantia de snapshot consistente.
- Baixar fora de public_html e do Git; registrar horário/tamanho/hash e manter acesso restrito/criptografado. O dump pode conter PII e hashes de senha.
- Se o plano contar com esse backup para voltar atrás, testar importação em base separada e medir limites de upload/timeout. Não prometer restore de backup não testado.
- Definir prazo e responsável por exclusão do backup; não conservar dados pessoais indefinidamente apenas por precaução.

Exportação/importação SQL são suportadas pelo phpMyAdmin; arquivos grandes podem enfrentar limites. [phpMyAdmin — import/export](https://docs.phpmyadmin.net/en/latest/import_export.html).

### Recuperação por momento da falha

| Momento | Estratégia |
| --- | --- |
| Antes do corte D1 | Abortar a publicação; banco antigo intacto; corrigir ambiente novo |
| Migration falha no banco novo ainda descartável | Inspecionar erro; pode ser mais simples recriar **esse alvo novo** com autorização específica e repetir cadeia validada; não é licença para apagar outras bases |
| Depois do corte, antes de receber dados úteis | Corrigir instalação ou voltar ao par antigo de binário/configuração/banco; retorno pode apenas restabelecer estado anterior infuncional |
| Depois de receber dados úteis | Preservar banco novo e gravações; preferir forward-fix ou binário comprovadamente compatível; nada de reset automático |
| D2 sem backup e banco antigo excluído | Recuperação dos dados antigos não garantida; só recriar instalação e corrigir falha |
| D2 com backup verificado | Restore possível conforme ensaio, mas precisa de binário/configuração compatíveis e aceitação de perda de gravações posteriores |

DDL MariaDB pode causar commits implícitos: falha de migration não garante schema vazio nem rollback integral. [MariaDB — commits implícitos](https://mariadb.com/docs/server/reference/sql-statements/transactions/sql-statements-that-cause-an-implicit-commit).

Voltar apenas o JAR antigo apontando para as novas tabelas em inglês não é rollback válido. Expand-and-contract e backfill permanecem alternativas apenas se a premissa de descarte mudar ou se coexistência sem parada se tornar requisito.

## 8. Riscos reclassificados

| Categoria | Ações no novo cenário | Observação |
| --- | --- | --- |
| SAFE | Análise, inventário e revisão deste relatório | Sem efeito no sistema |
| LOW RISK | Definir nomes finais e implementar/testar mapeamentos em ambiente isolado | API deve ser explicitamente preservada |
| MEDIUM RISK | Criar nova base separada, aplicar instalação inicial e validar | Sujeito a quota, privilégios, runtime e seleção correta do alvo |
| HIGH RISK | Troca operacional de binário/datasource; exclusão de banco; quebra de API; reset após dados novos | Exige aprovação, alvo identificado e plano de recuperação |
| NOT WORTH MIGRATING | Converter dados descartáveis; renomear tabelas antigas antes de apagá-las; dual-write sem requisito | Custo sem benefício nesta premissa |

### Riscos anteriores que deixam de bloquear a instalação limpa

Duplicatas/URLs antigas, tags legadas extensas, datas desconhecidas, MIME antigo, conversão dos UUIDs existentes e autorização dos usuários antigos não exigem saneamento se nenhum dado for importado. A V2 antiga não deve ser usada como conversor desnecessário.

### Riscos que continuam relevantes

- Versão/tipos MariaDB, collation de IDs YouTube, constraints e schema completo.
- Descoberta acidental das migrations antigas ou duas V1 na mesma execução.
- Credenciais/runtime incorretos, conexão remota bloqueada e privilégio inadequado.
- Bootstrap ADMIN, segurança e funcionamento real dos endpoints.
- Frontend enviando campos portugueses e binário incompatível com novo schema.
- Banco compartilhado com outro sistema ou escritores não identificados.
- Falta de backup dos **novos** dados após reabertura.
- Testes apenas H2: os nove testes aprovados na etapa 02 não certificam instalação MariaDB.

## 9. Validações necessárias

A matriz muda: testes de conversão dos registros antigos deixam de ser obrigatórios para D1/D2 sem importação. Permanecem:

1. Instalação completa em MariaDB vazio na versão/engine/charset previstos, com Java 21 e driver real.
2. Flyway descobrindo exclusivamente a nova cadeia; primeira execução cria tudo, segunda não tem pendências inesperadas e validate passa.
3. Falha de migration e procedimento de reconstrução do alvo descartável testados sem tocar base antiga.
4. Hibernate validate e CRUD de todas as entidades, mapeamentos explícitos, JPQL, derivados e ordenação.
5. Uniques, nulos, limites, acentos, Unicode, caixa de youtube_id, UUID e timestamps UTC.
6. Bootstrap de primeiro ADMIN sem credenciais fixas; login e permissões; tratamento dos tokens antigos antes da abertura, sem assumir que apagar usuários invalida todo JWT.
7. Fluxos de contatos/sugestões, posts/imagens e episódios; dados de teste criados do zero.
8. Contratos com frontend: rotas, JSON português preservado, multipart, autenticação, CORS e respostas.
9. Smoke tests após troca de datasource e prova de que instâncias antigas não escrevem mais.
10. Backup/recuperação dos novos dados e critérios de corte/retorno.
11. Evidência de correção da causa da aplicação estar infuncional; não declarar sucesso só porque as tabelas foram criadas.

Nada dessa lista foi executado na revisão documental.

## 10. Questões para decisão e gates de aprovação

### Decisões do responsável

1. **D1 (recomendada):** criar outra base, validar e só então excluir a antiga; ou **D2:** excluir/recriar com janela exclusiva?
2. Autorizar futura padronização física de R1–R11, preservando R12 e contratos HTTP por enquanto?
3. Confirmar descarte também de usuários, hashes, contatos, sugestões, posts/imagens, episódios e IDs antigos; definir como cadastrar o conteúdo inicial necessário ao lançamento.
4. Manter exportação temporária de precaução ou aceitar perda irrecuperável dos dados antigos? Qual retenção?
5. Confirmar prazo de manutenção e critério de “aplicação funcional” para aceite.

### Informações mínimas antes da implementação operacional

- [ ] Nome completo do banco exclusivo do backend e eventual uso por outros sistemas.
- [ ] Cota para segunda base e espaço; versão MariaDB, engine, charset/collation e privilégios.
- [ ] Local real de execução da JVM, comando/mecanismo de deploy e logs da falha atual.
- [ ] Acesso remoto/TLS para Flyway ou decisão sobre bootstrap manual via phpMyAdmin.
- [ ] Existência de outros ambientes consumidores das migrations legadas.
- [ ] Artefato/frontend alvo e mecanismo de bootstrap do administrador.
- [ ] Plano de interrupção de escritores, backup escolhido e responsável pelo aceite.

### Gates separados

- **Agora:** aprovar ou revisar a estratégia. Nenhuma implementação implícita.
- **Depois de autorização de implementação:** código/mapeamentos/migration inicial e testes isolados, com documentação e crédito a oEnzoRibas.
- **Antes de qualquer efeito na Hostinger:** confirmar alvo exato, execução planejada e autorização operacional.
- **Antes da exclusão:** confirmar banco a excluir, dependências, backup/renúncia e consequência irrecuperável. Aprovar o relatório não deve ser confundido com uma ordem imediata de apagar a base.

## 11. Registro da revisão e encerramento

A revisão anterior recomendava manter nomes físicos porque a premissa era preservar dados reais em produção. Com a nova informação do responsável, essa recomendação foi **substituída por instalação limpa**, preferencialmente em base nova paralela. O inventário técnico permanece válido como estado atual; não deve ser confundido com schema já implementado.

Este relatório foi reescrito no mesmo caminho, preservando o mapa completo Java/JPA/API/Flyway do estado observado. Não foram alterados relatórios das etapas anteriores, fontes, configurações, .run, dependências ou migrations. Não houve acesso à Hostinger, criação/exclusão de banco ou execução de SQL.

**Aguardar a decisão do responsável. Não iniciar a implementação, migrations ou exclusão de qualquer banco.**

