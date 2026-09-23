# Etapa 01 — Auditoria e inventário do backend

- Data da auditoria e registro: 20/09/2026.
- Projeto: `podiss-backend`.
- Responsável/créditos do projeto: **oEnzoRibas**.
- Branch: `refactor-fixall`.
- Commit de referência: `c1edc6cb9151ad7794f2f131fced735899f1456c`.
- Plano: [Plano mestre](../master-plan.md).
- Escopo: [Etapa 01](../etapas/etapa01.md).
- Status: **auditoria concluída para revisão; implementação e próximas etapas não autorizadas por este relatório**.

Este documento registra a auditoria apresentada na conversa, complementada pela inspeção de `.run`. Seu salvamento foi autorizado posteriormente pelo responsável. Nesta etapa não foram modificados código, dependências, configurações ou migrations; não foram executados aplicação, testes, migrations ou consultas ao banco de produção.

Valores reais de credenciais não são reproduzidos. As referências a comportamento são conclusões da leitura do código, não resultados de testes desta etapa.

## Identificação da baseline e limites

Há três estados que não devem ser confundidos:

| Estado | Evidência |
| --- | --- |
| Código versionado em HEAD | POM e configurações recentes, mas implementação Java anterior |
| Cópia de trabalho auditada | 26 arquivos modificados, 2 excluídos e 16 não rastreados, antes de salvar este relatório |
| Aplicação em produção | Commit, artefato, configurações efetivas e schema ainda não verificados |

Parte das mudanças locais foi iniciada pelo assistente durante a solicitação anterior de correções, antes da mudança de escopo para auditoria. Essas alterações permaneceram no diretório e não foram revertidas nem completadas nesta etapa.

A versão declarada no POM é `1.1.0`. Isso, isoladamente, não identifica o artefato publicado. O HEAD não reproduz toda a implementação local; a implementação local não comprova o estado de produção.

O ambiente informado pelo responsável é hospedagem compartilhada Hostinger/hPanel para `podiss.com.br`, com banco MariaDB real, administração pelo phpMyAdmin, sem root ou Docker no servidor. A execução futura de migrations deverá considerar CLI/CI externa ou uma alternativa SQL, conforme a disponibilidade de acesso remoto. Nada disso foi executado nesta etapa.

A inspeção incluiu arquivos do repositório, arquivos locais de configuração, diferenças em relação ao HEAD e o BOM Maven disponível no cache. Não foi executado dependency tree, scanner SCA, build ou resolução de dependências. Não foi inspecionado o JAR publicado nem o schema remoto.

## 1. Visão geral da arquitetura atual

Monólito Spring Boot, Java 21, empacotamento JAR e organização por funcionalidade.

| Package | Responsabilidade |
| --- | --- |
| `br.com.codejr.podiss.backend` | Inicialização por BackApplication |
| `config` | Segurança, BCrypt e cliente HTTP do YouTube |
| `security` | JWT, respostas de autenticação e limitação de envios |
| `user` | Login, cadastro, usuários e bootstrap administrativo |
| `post` | Publicações, tags e imagens |
| `video` | Episódios e integração com YouTube |
| `contact` | Mensagens de contato |
| `sugest` | Sugestões de temas |
| `common` | Erros HTTP e paginação |
| `db.migration` | Migration Java V2, fora do package principal |

Na cópia de trabalho foram encontrados 46 arquivos Java em src/main/java, cinco entidades, cinco repositories e cinco controllers.

Fluxo predominante: requisição HTTP → filtros de segurança → controller → service → repository → banco.

Características:

- Spring MVC e Jackson para HTTP/JSON.
- Spring Data JPA/Hibernate para persistência e MariaDB como datasource de execução.
- JWT no cabeçalho Authorization e BCrypt para senhas.
- Imagens armazenadas no banco.
- Sem relacionamentos JPA entre as cinco entidades.
- Sem mensageria, processamento assíncrono ou cache distribuído identificados.
- Sem implementação OpenAPI ou Actuator identificada.
- Transações explícitas em cadastro de usuários, criação de contatos/sugestões e operações de posts.
- A consulta externa ao YouTube ocorre antes da gravação do episódio, sem uma transação de serviço mantida durante a chamada HTTP.

### Diferenças entre HEAD e cópia de trabalho

| Funcionalidade | HEAD | Cópia de trabalho |
| --- | --- | --- |
| Cadastro | Público | Restrito a ADMIN |
| Autorização | Usuário autenticado | Papéis USER e ADMIN |
| Contatos e sugestões | Envio exige autenticação | Envio público |
| YouTube | Processo Python | HTTP em Java |
| Listagens | Sem paginação | Paginação limitada a 100 registros |
| Respostas de posts | Entidade diretamente | PostResponse sem BLOB |
| Flyway | V1 SQL antiga | V1 alterada e V2 Java não rastreada |

Essa divergência impede tratar automaticamente o diretório como uma release pronta.

## 2. Inventário das configurações

Arquivos examinados: `pom.xml`, `src/main/resources/application*.yaml`, `src/test/resources/application.yaml`, `.env.example`, `.gitignore`, wrapper Maven, READMEs e `.run/BackApplication.run.xml`. O `.env` foi inspecionado quanto a nomes e presença de valores, sem reproduzir credenciais.

### 2.1. Profiles e comportamento

| Configuração | Base | dev | prod | Recursos de testes |
| --- | --- | --- | --- | --- |
| Banco | MariaDB por ambiente | Herda | Herda | H2 em memória |
| ddl-auto | validate | Herda | Herda | create-drop |
| Flyway | Ativo | Ativo | Herda ativo | Desativado |
| Baseline automático | Desativado | Ativado, versão 1 | Herda desativado | Não aplicável com Flyway desligado |
| clean-disabled | true | Herda | Herda | — |
| SQL no log | Não definido na base | false | false | false |
| Porta | Padrão do framework | 8080 | 8080 | Ambiente de teste |
| SSL da aplicação | Não configurado | Não configurado | Opcional; desativado por padrão | Não configurado |

Outras configurações:

- `open-in-view: false`.
- JDBC configurado para UTC.
- Multipart: 5 MB por arquivo e 6 MB por requisição.
- JWT: issuer `podiss-backend`, validade de cinco horas.
- YouTube: conexão de três segundos e leitura de oito segundos.
- Envios públicos: dez por minuto por endereço remoto, conforme implementação local.
- Nenhum profile ativo é fixado nos YAMLs de execução.

### 2.2. Variáveis de ambiente

| Variável | Finalidade | Obrigatoriedade/padrão | Observação |
| --- | --- | --- | --- |
| DB_URL | Conexão JDBC | Obrigatória | Driver MariaDB; URL e opções reais não verificadas |
| DB_USERNAME | Usuário do banco | Obrigatória | Credencial operacional |
| DB_PASSWORD | Senha do banco | Obrigatória | Secret |
| HIKARI_MAX_LIFETIME | Vida útil das conexões | 1800000 ms | Validar limites do provedor |
| HIKARI_MAX_POOL_SIZE | Máximo de conexões | 10 | Ausente no .env.example |
| HIKARI_MIN_IDLE | Conexões ociosas mínimas | 5 | Ausente no .env.example |
| JWT_SECRET | Assinatura JWT | Obrigatória | Código local exige Base64 com pelo menos 64 bytes |
| CORS_ALLOWED_ORIGINS | Origens permitidas | http://localhost:5173 | Produção também herda esse fallback |
| ADMIN_USERNAME | Bootstrap administrativo | Vazio | Usado em conjunto com email e senha |
| ADMIN_EMAIL | Bootstrap administrativo | Vazio | Pode conflitar com conta existente |
| ADMIN_PASSWORD | Bootstrap administrativo | Vazio | Secret; pode causar escrita no startup |
| YOUTUBE_API_KEY | YouTube Data API | Vazio | Sem chave, usa oEmbed e descrição vazia |
| SSL_ENABLED | TLS na aplicação | false em prod | Depende da terminação HTTPS real |
| SSL_KEY_STORE | Caminho do keystore | Vazio | Exemplo aponta para recurso versionado |
| SSL_KEY_STORE_PASSWORD | Senha do keystore | Vazio | Necessária conforme configuração SSL |
| SSL_KEY_ALIAS | Alias do keystore | server | Deve existir no keystore |

As variáveis de banco, JWT, CORS, bootstrap e YouTube são consumidas pela configuração base; as de SSL são mapeadas no profile prod. As opções Hikari têm formato numérico; origens são separadas por vírgula; o JWT exige Base64; o endereço JDBC deve usar formato aceito pelo driver.

Não foi encontrado carregamento automático de `.env` implementado pela aplicação. Há carregamento pela configuração local do IntelliJ, descrita abaixo. Execução via JAR/CLI/pipeline precisa fornecer o ambiente por outro mecanismo.

Inconsistências:

- O exemplo de JWT_SECRET não satisfaz o mínimo de 64 bytes do código local.
- O README principal exemplifica DB_HOST, DB_PORT e DB_NAME, enquanto o YAML usa DB_URL.
- A documentação de recursos afirma show-sql=true em dev; o YAML contém false.
- A documentação descreve bootstrap quando a tabela está vazia; o código verifica ausência de usuário ADMIN.
- Propriedades de produção não estão “travadas”: podem ser sobrescritas externamente.

### 2.3. Inventário de .run — complemento solicitado

Arquivo local encontrado: `.run/BackApplication.run.xml`.

| Item | Valor observado |
| --- | --- |
| Nome da configuração | BackApplication |
| Tipo IntelliJ | SpringBootApplicationConfigurationType |
| Classe principal | br.com.codejr.podiss.backend.BackApplication |
| Módulo | podiss-backend |
| Profile ativo | dev |
| Atualização ao perder foco | UpdateClassesAndResources |
| Antes de iniciar | Make habilitado |
| Extensão | net.ashald.envfile |
| EnvFile habilitado | true |
| Substituição de variáveis | true |
| Suporte a macros de caminho do plugin | false |
| Ignorar arquivos ausentes | false |
| Integrações experimentais | false |
| Primeira entrada | PARSER=env, PATH=.env, habilitada, não executável |
| Segunda entrada | PARSER=runconfig, habilitada, não executável |
| Variáveis explícitas em nós env | Nenhuma encontrada |
| Versionamento | Arquivo não rastreado; ignorado explicitamente pelo .gitignore, linha 40 |

Conclusões:

1. Essa configuração explica como o desenvolvimento local pode receber o conteúdo de `.env` sem uma biblioteca dotenv no backend.
2. Seu comportamento depende do IntelliJ e da extensão EnvFile. Não é uma configuração de deploy da Hostinger, do JAR ou do Maven.
3. Um clone do repositório não recebe esse arquivo ignorado. A experiência de execução local não está integralmente reproduzida pelo Git.
4. A classe principal e o módulo são dependências de nomenclatura. Renomeá-los exige revisar o XML: **CONFIGURATION_IMPACT**, além da alteração Java.
5. O profile dev habilita baseline automático. Executar essa configuração com DB_URL apontando para produção representa risco de alteração do histórico/schema, apesar de ddl-auto=validate.
6. A combinação de entradas env e runconfig exige confirmar a precedência efetiva do plugin quando uma mesma variável estiver definida nas duas fontes.
7. O caminho relativo `.env` depende de sua resolução no ambiente da IDE; o comportamento com arquivo ausente deve ser documentado, pois a opção de ignorar ausência está desativada.
8. O arquivo não foi executado nem alterado. Nenhuma credencial foi copiada para este relatório.

## 3. Análise das dependências Maven

POM: versão do projeto 1.1.0, parent Spring Boot 3.4.1 e Java 21. Versões gerenciadas verificadas no BOM disponível no cache Maven; não são confirmação do artefato de produção.

| Dependência | Versão/gerenciamento | Avaliação |
| --- | --- | --- |
| spring-boot-starter-web | Boot 3.4.1 | Utilizada; MVC, Jackson e Tomcat |
| spring-boot-starter-validation | Boot | Utilizada pelos DTOs locais |
| spring-boot-starter-data-jpa | Boot | Utilizada; Hibernate 6.6.4.Final |
| spring-boot-starter-security | Boot | Utilizada; Security 6.4.2 |
| jjwt-api | 0.11.5 explícita | Utilizada |
| jjwt-impl | 0.11.5, runtime | Scope coerente |
| jjwt-jackson | 0.11.5, runtime | Integração de serialização; não é redundância indevida |
| mariadb-java-client | 3.4.1 pelo BOM | Sem scope explícito; candidato a runtime após avaliar ferramentas |
| flyway-core | 10.20.1 | Startup e migration Java local |
| flyway-mysql | 10.20.1 | Suporte ao banco; não redundante com core |
| Lombok | 1.18.36 | Utilizado; annotation processor alinhado |
| DevTools | runtime, optional | Desenvolvimento; confirmar exclusão no artefato publicado |
| spring-boot-starter-test | test | Utilizado pelo único teste |
| spring-security-test | test | Nenhum uso encontrado nos testes existentes |
| H2 | 2.3.232, test | Utilizado pela configuração de testes |
| Testcontainers JUnit | 1.20.4, test | Nenhum teste consumidor encontrado |
| Testcontainers MariaDB | 1.20.4, test | Nenhum teste consumidor encontrado |

Outras versões gerenciadas relevantes: Spring Framework 6.2.1, Tomcat 10.1.34, Jackson BOM 2.18.2, Logback 1.5.12 e SnakeYAML 2.3.

Plugins e ferramentas:

- Compiler gerenciado 3.13.0; Surefire gerenciado 3.5.2.
- Failsafe configurado para integration-test/verify, sem testes correspondentes identificados.
- Spring Boot Maven Plugin presente, com exclusão explícita de Lombok.
- Wrapper fixa Maven 3.9.7.
- Sem plugin Flyway explicitamente configurado, scanner de vulnerabilidades, SBOM ou Maven Enforcer.

Não foi comprovado conflito direto de versões. Inventário transitivo completo, scopes efetivos e análise do JAR real continuam pendentes. Dependências sem uso observado não devem ser removidas automaticamente.

### Avisos de segurança a investigar

| Componente esperado | Aviso | Aplicabilidade observada |
| --- | --- | --- |
| Spring Security 6.4.2 | CVE-2025-22228 | BCrypt em faixa afetada; cadastro limita bytes, login limita caracteres; investigar credenciais legadas |
| Spring Framework 6.2.1 | CVE-2025-22233 | Versão afetada; não encontrado uso de disallowedFields, condição relevante |
| Tomcat 10.1.34 | CVE-2025-24813 | Versão afetada; não encontrada escrita habilitada no Default Servlet, condição do cenário descrito |
| Tomcat 10.1.34 | CVE-2025-48988 / CVE-2025-48976 | Endpoints multipart presentes; investigar consumo de memória por partes/cabeçalhos |

Fontes oficiais consultadas:

- [Spring Security — CVE-2025-22228](https://spring.io/security/cve-2025-22228/).
- [Spring Framework — CVE-2025-22233](https://spring.io/security/cve-2025-22233/).
- [Apache Tomcat — avisos de segurança](https://tomcat.apache.org/security-10.html).

Versão afetada não comprova exploração. A lista não substitui um scanner atualizado nem é exaustiva. Nenhuma dependência foi atualizada.

## 4. Inventário de nomenclaturas

Candidatos para discussão, não decisões implementadas. Uma ocorrência pode ter múltiplas classificações.

| Nome atual | Candidato | Classificação/cuidado |
| --- | --- | --- |
| BackApplication | PodissApplication | JAVA_ONLY; CONFIGURATION_IMPACT confirmado no .run |
| contact.Contato | contact.ContactMessage | JAVA_ONLY preservando Table e contratos |
| ContatoController/Service/Repository/Request | ContactMessage… | JAVA_ONLY com referências internas atualizadas |
| sugest | suggestion | JAVA_ONLY; abreviação pouco clara |
| Sugestao e associadas | TopicSuggestion e associadas | JAVA_ONLY com mapeamentos preservados |
| nome | name ou senderName | API_IMPACT e DATABASE_IMPACT na renomeação direta |
| assunto | subject | API_IMPACT e DATABASE_IMPACT |
| mensagem | message | API_IMPACT e DATABASE_IMPACT |
| tema | topic | API_IMPACT e DATABASE_IMPACT |
| /contatos | /contact-messages | API_IMPACT |
| /sugestoes | /topic-suggestions | API_IMPACT |
| Tabela contatos | contact_messages | DATABASE_IMPACT |
| Tabela sugestoes | topic_suggestions | DATABASE_IMPACT |
| video.Video e associadas | episode.Episode e associadas | JAVA_ONLY preservando tabela episodes e JSON |
| VideoUrlRequest | CreateEpisodeRequest | JAVA_ONLY |
| PostRequest | CreatePostRequest | JAVA_ONLY |
| JwtTokenUtil | JwtTokenService | JAVA_ONLY; componente com estado/configuração |
| Pages | PaginationSupport | JAVA_ONLY |
| ApiErrors | ProblemResponseWriter | JAVA_ONLY; diferencia do handler |
| youtubeId / YouTube | Convenção única | API_IMPACT e possível DATABASE_IMPACT se atributo mudar |
| id nas rotas de episodes | Explicitar UUID versus ID externo | API_IMPACT se rota mudar |
| createContato, saveSugestao no HEAD | Métodos em inglês | JAVA_ONLY |
| scrap/youtube-scrapp.py no HEAD | Nome de scraping consistente | CONFIGURATION_IMPACT; processo externo usa caminho |
| docs/ADRS, README.MD, yaml citado como yml | Convenção documental | CONFIGURATION_IMPACT se ferramentas consumirem caminhos |
| Nome/módulo/classe da configuração .run | Alinhar à nomenclatura aprovada | CONFIGURATION_IMPACT |
| DB_*, JWT_*, HIKARI_* | Já em inglês | Sem necessidade funcional identificada de renomear |

Renomear atributo Java pode ser JAVA_ONLY se coluna e nome JSON forem preservados explicitamente. Sem isso, Lombok/Jackson e a estratégia física do Hibernate podem alterar contrato e schema.

Há acoplamento textual interno: a projeção JPQL referencia o nome completo de PostResponse; paginação usa as propriedades "createdAt" e "id". Renomeações exigem atualizar essas referências.

## 5. Mapa preliminar Java ↔ API ↔ banco

O mapa representa o schema esperado pela cópia de trabalho, não o banco real da Hostinger.

### 5.1. Entidades e mapeamentos

Todas as entidades locais usam UUID id, Id, GeneratedValue e JdbcTypeCode(SqlTypes.UUID).

| Entidade / tabela explícita | Java e JSON | Colunas esperadas |
| --- | --- | --- |
| User → users | id, username, password, email, role | Mesmos nomes; username/email únicos; strings 255, papel 16 |
| Post → posts | id, title, description, tags, createdAt | id, title, description TEXT, tags VARCHAR(2048), created_at |
| Post → posts | image e imageContentType internos | image LONGBLOB, image_content_type VARCHAR(100), opcionais |
| Video → episodes | id, title, description, youtubeId, videoUrl, thumbnailUrl, createdAt | youtube_id VARCHAR(11) único, video_url, thumbnail_url VARCHAR(512), created_at; descrição TEXT |
| Contato → contatos | nome, email, assunto, mensagem, createdAt | Strings 255, mensagem TEXT, created_at |
| Sugestao → sugestoes | nome, email, tema, createdAt | Nome/email 255, tema 2000, created_at |

- Tabelas explicitadas por Table.
- Maioria das colunas sem Column(name=...).
- Conversões createdAt/created_at, youtubeId/youtube_id e equivalentes dependem da estratégia física, salvo mapeamento explícito como Post.createdAt.
- Campos obrigatórios têm nullable=false; imagens, MIME e miniatura são opcionais.
- Sem FKs, relacionamentos ou sequences explicitamente declarados.
- Papel persistido como texto; tags como string separada por vírgulas.
- PostResponse acrescenta hasImage e imageUrl, que não são colunas.
- Login retorna token; não retorna diretamente a entidade User.

### 5.2. Endpoints locais

| Método e rota | Entrada | Saída / acesso |
| --- | --- | --- |
| POST /api/auth/login | LoginRequest | Token, público |
| POST /api/auth/register | RegisterRequest | Mensagem, ADMIN |
| GET /posts | page, size, order | Lista PostResponse, público |
| GET /posts/{id} | UUID | PostResponse, público |
| POST /posts | Multipart PostRequest | PostResponse, ADMIN |
| PUT /posts/{id} | JSON UpdatePostRequest | PostResponse, ADMIN |
| DELETE /posts/{id} | UUID | Sem corpo, ADMIN |
| GET /posts/image/{id} | UUID | Binário, público |
| PUT /posts/{id}/image | Multipart | PostResponse, ADMIN |
| DELETE /posts/{id}/image | UUID | Sem corpo, ADMIN |
| GET /episodes | Paginação | Lista Video, público |
| GET /episodes/{youtubeId} | ID externo de 11 caracteres | Video, público |
| POST /episodes | URL e descrição opcional | Video, ADMIN |
| DELETE /episodes/{id} | UUID interno | Sem corpo, ADMIN |
| POST /contatos | ContatoRequest | Entidade criada, público |
| GET /contatos | Paginação | Lista, ADMIN |
| POST /sugestoes | SugestaoRequest | Entidade criada, público |
| GET /sugestoes | Paginação | Lista, ADMIN |

Listas são arrays; metadados ficam em X-Total-Count, X-Total-Pages, X-Page e X-Page-Size. Padrão: 100 registros, ordem crescente por createdAt e id.

DTOs locais: LoginRequest, RegisterRequest, JwtResponse, PostRequest, UpdatePostRequest, PostResponse, VideoUrlRequest, ContatoRequest e SugestaoRequest. Há records internos de imagem e metadados YouTube.

Repositories são JpaRepository<Entidade, UUID>. Há consultas derivadas para usuários e YouTube ID e projeção JPQL para listar posts sem selecionar BLOB.

Criações locais usam 201, exclusões usam 204 e demais retornos normais predominantemente 200. Erros de domínio/validação/autorização utilizam ProblemDetail em caminhos tratados. Não foi produzido ou validado um contrato OpenAPI nesta etapa.

## 6. Análise do Flyway

| Migration | Situação |
| --- | --- |
| src/main/resources/db/migration/V1__database.sql | Rastreada, alterada localmente |
| src/main/java/db/migration/V2__functional_schema.java | Não rastreada; trabalho local anterior à auditoria |

Convenções: V<versão>__<descrição>.sql e classe Java V2__functional_schema estendendo BaseJavaMigration. Descoberta prevista em classpath:db/migration. Sem repeatable/undo identificadas.

### V1 do HEAD

Cria episodes, posts e users com CREATE TABLE IF NOT EXISTS e UUID DEFAULT RANDOM_UUID(). Não cria contatos/sugestoes e não representa todos os atributos locais. Compatibilidade dessa expressão com MariaDB não está demonstrada.

### V1 local

Altera definições da V1, incluindo tipos/defaults, e usa UUID nativo. Cria três tabelas e delega o restante à V2.

Não se sabe se a V1 foi aplicada em produção. Portanto, a legitimidade de modificá-la depende da inspeção do histórico real; não se deve presumir que nunca foi usada.

### V2 local

Valida URLs e duplicidades, exige UUID nativo em users/posts/episodes, cria tabelas ausentes, adiciona role com padrão USER, campos de imagem/YouTube, altera tipos/nulabilidade, normaliza URLs, preenche dados e cria índices.

Riscos:

1. canExecuteInTransaction=false; falha pode deixar mudanças parciais.
2. Não é idempotente como um todo; ADD COLUMN e CREATE INDEX podem falhar na repetição.
3. Checksum Java fixo: edição do conteúdo sem atualizar o número pode não ser percebida pela validação.
4. Tags passam a VARCHAR(2048); conteúdo legado maior em TEXT exige análise.
5. Description NOT NULL pode falhar com nulos legados.
6. Datas preenchidas não representam necessariamente datas históricas reais.
7. Contas existentes passam a USER e podem perder acesso administrativo.
8. IDs de contatos/sugestoes existentes não são validados pelo preflight de tipos.
9. Não há ensaio contra cópia real de produção comprovado.

A configuração liga Flyway no startup, enquanto a operação desejada é externa via CLI/CI ou SQL pelo phpMyAdmin. Esses mecanismos não estão reconciliados.

Migration Java não pode ser executada diretamente no phpMyAdmin. Na CLI exige classe compilada e classpath correto. O repositório não oferece um procedimento operacional completo para isso.

Nada foi executado contra o banco nesta etapa.

## 7. Problemas de segurança encontrados

### 7.1. HEAD

Permanecem no código registrado os problemas da auditoria anterior: cadastro público associado a operações de qualquer autenticado, chave JWT embutida, parsing de token sem tratamento adequado em todos os caminhos, entry point sem resposta implementada e ausência de separação entre usuário comum/administrador.

Mudanças locais não comprovam correção na versão publicada.

### 7.2. Cópia de trabalho

| Achado | Consequência |
| --- | --- |
| Login sem limitação de tentativas na aplicação | Tentativas automatizadas e consumo de BCrypt |
| Limitação por getRemoteAddr() | Proxy pode agrupar usuários no mesmo limite |
| Limitador em memória | Reinicia com processo; não coordena instâncias |
| JWT sem revogação individual | Token utilizável até expirar, salvo outras mudanças de estado |
| Login limita caracteres; cadastro limita bytes | Inconsistência BCrypt com multibyte |
| Imagem validada por assinatura inicial | Não comprova integridade integral |
| server.p12 versionado | Identificar finalidade e presença de material privado |
| SSL_ENABLED=false por padrão | Transporte depende da infraestrutura externa |
| JWT inválido processado em rotas públicas | Token expirado pode impedir consulta pública pelo cliente |

CSRF desativado é coerente com a intenção stateless por Bearer. Se autenticação migrar para cookies enviados automaticamente, será necessário reavaliar.

O keystore não foi aberto nem teve conteúdo criptográfico inspecionado. Secrets de .env/.run não foram reproduzidos. A configuração .run não apresentou nós env com valores explícitos; referencia o arquivo .env.

## 8. Problemas de DevOps/configuração encontrados

1. Local da JVM não esclarecido. A informação de hospedagem compartilhada precisa ser conciliada com a execução do Spring Boot. A documentação oficial da Hostinger indica VPS para Java; isso não invalida que site e banco estejam no hPanel. [Referência](https://www.hostinger.com/support/which-programming-languages-and-frameworks-are-supported-at-hostinger/).
2. Clone de HEAD não reproduz vários componentes locais.
3. Sem pipeline CI/CD ou procedimento operacional completo de deploy identificado.
4. Sem separação implementada entre usuário de migrations e de execução; Flyway utiliza o datasource da aplicação.
5. Dev permite baseline automático e não há proteção que o impeça de apontar para produção.
6. .run ativa dev e carrega .env. Logo, a escolha de DB_URL na IDE pode provocar adoção/migração de um banco real.
7. .run é ignorado e depende do plugin EnvFile; execução local não é integralmente reproduzível por clone.
8. Sem configuração versionada para executar externamente a V2 Java ou alternativa SQL equivalente.
9. Sem health check específico e observabilidade operacional definida.
10. Pool Hikari não validado contra a quota da hospedagem compartilhada.
11. Documentação divergente:
    - ADR 0001 descreve ddl-auto=update em dev.
    - README de testes menciona ENGINE=InnoDB e collation não encontrados na V1.
    - Teste Testcontainers ilustrado não existe.
    - Exemplo usa ServiceConnection sem dependência explícita spring-boot-testcontainers.
    - Exemplo não demonstra reativação do Flyway desativado nos recursos de testes.
    - README de recursos diz show-sql=true em dev, enquanto YAML usa false.

## 9. Riscos para produção

| Prioridade | Risco | Evidência necessária |
| --- | --- | --- |
| Crítica | Publicar estado diferente do auditado | Commit/artefato real |
| Crítica | Alterar migration aplicada | Histórico e checksums de produção |
| Crítica | Executar DDL no startup | Responsável e mecanismo de migrations |
| Crítica | Perder acesso administrativo após V2 | Mapeamento dos administradores atuais |
| Alta | UUID incompatível | Versão MariaDB e tipos das cinco PKs |
| Alta | Schema parcialmente migrado | Backup, ensaio e recuperação DDL |
| Alta | Truncamento/falha em dados legados | Tags, nulos, URLs duplicadas/inválidas |
| Alta | Clone versionado falhar em banco vazio | V1 do HEAD e alterações não registradas |
| Alta | Encerrar sessões existentes | Rotação de chave e issuer JWT |
| Alta | Omitir conteúdo no frontend | Consumo da paginação e quantidade de registros |
| Alta | HTTPS/JDBC sem proteção confirmada | Terminação TLS e conexão remota |
| Alta | IDE atingir produção com profile dev | Destino DB_URL e política de execução .run |
| Média | Bloquear formulários coletivamente | IP real atrás de proxy |

O estado local não está certificado como pronto para deploy.

## 10. Dívida técnica observada

- Único teste: BackApplicationTests.contextLoads().
- Sem testes funcionais, de autorização, validação, concorrência, contratos ou upgrade real.
- H2 com create-drop e Flyway desligado não valida migrations.
- YAML de testes não define app.cors.allowed-origins, exigida pelo código; resolução efetiva precisa ser validada antes de declarar testes autossuficientes.
- Testcontainers e Spring Security Test sem testes consumidores.
- Entidades expostas em episódios, contatos e sugestões.
- PUT de post tem comportamento parcial, preservando campos ausentes.
- Tags entram em lista e persistem/saem como string.
- Paginação altera quantidade retornada apesar de preservar array.
- Sem auditoria de alterações administrativas ou datas de atualização.
- Sem gestão completa de papéis, desativação ou recuperação de acesso.
- Javadoc escasso; comentário de autoria de ApiErrors antes do package, não associado à classe.
- Sem OpenAPI/contratos versionados.
- Documentação mistura implementação, exemplos e propostas.
- Configuração local da IDE depende de arquivo ignorado e plugin, ainda sem documentação operacional adequada.

Não foram executados testes nesta etapa. Resultados anteriores não validam o estado atual.

## 11. Mudanças que parecem seguras

Candidatas futuras, preservando contratos:

- Corrigir documentação contraditória com fatos verificados.
- Padronizar variáveis locais e métodos privados.
- Renomear auxiliares/services atualizando referências internas.
- Padronizar classes em inglês mantendo Table, JSON e endpoints.
- Documentar variáveis e a configuração .run/EnvFile sem secrets.
- Elaborar testes com isolamento explícito.
- Remover imports/comentários obsoletos após validação.

JAVA_ONLY ainda requer considerar JPQL, beans, reflexão e configurações externas. Renomear a classe principal especificamente exige atualizar .run e é também CONFIGURATION_IMPACT.

Nenhuma recomendação foi implementada. A única alteração desta entrega é o registro documental autorizado.

## 12. Mudanças que exigem planejamento

- Renomeação de tabelas, colunas, índices e constraints.
- Definição do destino da V1 local e V2 proposta.
- Adoção de Flyway no banco existente.
- Migração de IDs ou exigência de UUID nativo.
- Migração de papéis administrativos.
- Rotação JWT e compatibilidade de sessões.
- Renomeação de JSON/endpoints.
- Paginação e adaptação dos consumidores.
- Atualização de Spring Boot e dependências.
- Integração YouTube e disponibilidade da descrição.
- Migrations externas via CLI/CI ou procedimento phpMyAdmin.
- Backup, restauração, janela de manutenção e rollback.
- Política de profiles, destino do banco e precedência de ambiente no .run.
- Eventual template versionado de configuração da IDE, sem secrets, após decisão explícita.

Esses itens pertencem às próximas etapas. Este relatório não autoriza sua execução.

## 13. Questões antes da próxima etapa

1. Qual commit/JAR está em produção?
2. Como tratar as mudanças locais iniciadas anteriormente: proposta separada ou avaliação incremental?
3. Onde roda a JVM, qual URL o frontend usa e como o backend é iniciado/reiniciado?
4. Qual a versão exata do MariaDB e o schema real, incluindo tipos, collation, índices, defaults e nulabilidade?
5. Existe flyway_schema_history? Quais versões, estados e checksums?
6. Quais contas são administradores legítimos?
7. Banco aceita acesso remoto, allowlist e TLS? Quais limites de DDL e conexão?
8. Existe backup com restauração testada? Qual janela e tolerância a perda?
9. Frontend suporta paginação/novos códigos HTTP? Quantos registros existem?
10. Keystore versionado é de desenvolvimento ou relacionado à produção?
11. Descrição completa do YouTube é obrigatória?
12. A configuração .run é exclusivamente local? O .env usado nela aponta para banco de desenvolvimento isolado?
13. Dev deve continuar permitindo baseline automático? Quem pode autorizar a adoção de uma base existente?
14. Deve existir um template versionado de execução da IDE, ou apenas instruções para recriar a configuração?

## Encerramento

Etapa 01 concluída para revisão. A baseline do código está registrada; equivalência com produção depende das evidências de implantação/schema.

O salvamento deste relatório foi autorizado após a auditoria. Código, .run, .env, dependências, configurações e migrations foram preservados. Nenhuma etapa seguinte foi iniciada.

