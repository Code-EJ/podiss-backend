# Etapa 02 — Padronização interna de nomenclatura

Créditos/responsável: **oEnzoRibas**. Data: 20/09/2026.
Plano: [etapa02](../etapas/etapa02.md). Baseline: [etapa01](etapa01-auditoria-e-inventario.md).
Status: **concluída para revisão**. Build limpo e nove testes aprovados; nenhuma migration executada.

## Classificação prévia

| Nome anterior | Nome proposto | Classificação | Garantia de compatibilidade |
| --- | --- | --- | --- |
| Contato | ContactMessage | JAVA_ONLY | Preservar propriedades, rotas, mapeamentos e referências internas |
| ContatoController | ContactMessageController | JAVA_ONLY | Preservar propriedades, rotas, mapeamentos e referências internas |
| ContatoService | ContactMessageService | JAVA_ONLY | Preservar propriedades, rotas, mapeamentos e referências internas |
| ContatoRepository | ContactMessageRepository | JAVA_ONLY | Preservar propriedades, rotas, mapeamentos e referências internas |
| ContatoRequest | CreateContactMessageRequest | JAVA_ONLY | Preservar propriedades, rotas, mapeamentos e referências internas |
| Sugestao | TopicSuggestion | JAVA_ONLY | Preservar propriedades, rotas, mapeamentos e referências internas |
| SugestaoController | TopicSuggestionController | JAVA_ONLY | Preservar propriedades, rotas, mapeamentos e referências internas |
| SugestaoService | TopicSuggestionService | JAVA_ONLY | Preservar propriedades, rotas, mapeamentos e referências internas |
| SugestaoRepository | TopicSuggestionRepository | JAVA_ONLY | Preservar propriedades, rotas, mapeamentos e referências internas |
| SugestaoRequest | CreateTopicSuggestionRequest | JAVA_ONLY | Preservar propriedades, rotas, mapeamentos e referências internas |
| Video | Episode | JAVA_ONLY | Preservar propriedades, rotas, mapeamentos e referências internas |
| VideoController | EpisodeController | JAVA_ONLY | Preservar propriedades, rotas, mapeamentos e referências internas |
| VideoService | EpisodeService | JAVA_ONLY | Preservar propriedades, rotas, mapeamentos e referências internas |
| VideoRepository | EpisodeRepository | JAVA_ONLY | Preservar propriedades, rotas, mapeamentos e referências internas |
| VideoUrlRequest | CreateEpisodeRequest | JAVA_ONLY | Preservar propriedades, rotas, mapeamentos e referências internas |
| PostRequest | CreatePostRequest | JAVA_ONLY | Preservar propriedades, rotas, mapeamentos e referências internas |
| JwtTokenUtil | JwtTokenService | JAVA_ONLY | Preservar propriedades, rotas, mapeamentos e referências internas |
| ApiErrors | ProblemResponseWriter | JAVA_ONLY | Preservar propriedades, rotas, mapeamentos e referências internas |
| Pages | PaginationSupport | JAVA_ONLY | Preservar propriedades, rotas, mapeamentos e referências internas |
| Package sugest | suggestion | JAVA_ONLY | Mesmo component scan; sem referência externa encontrada |
| Package video | episode | JAVA_ONLY | Mesmo component scan; sem referência externa encontrada |
| Classe principal BackApplication | Preservada | CONFIGURATION_IMPACT | .run referencia a classe |
| Campos nome, assunto, mensagem, tema | Preservados | API_IMPACT / DATABASE_IMPACT | JSON e nomes físicos não mudam |
| Endpoints e parâmetros | Preservados | API_IMPACT | Nenhuma mudança de contrato |
| Tabelas, colunas, índices e migrations | Preservados | DATABASE_IMPACT | Nenhuma edição |
| YAML, .env, .env.example, .run e POM | Preservados | CONFIGURATION_IMPACT | Nenhuma edição |

As classes JPA renomeadas mantêm explicitamente os nomes lógicos anteriores (Contato, Sugestao, Video) via Entity(name=...), além dos Table existentes. Beans renomeados mantêm seus identificadores anteriores explicitamente. As propriedades serializadas, nomes de campos e métodos derivados dos repositories permanecem.

Esta etapa parte da cópia de trabalho, incluindo mudanças anteriores ainda não commitadas. Não promove ou certifica essas mudanças para produção e não modifica a baseline histórica da etapa 01.

## 1. Nomes alterados e justificativas

A tabela de classificação acima foi registrada antes da implementação. Foram movidos/renomeados 21 arquivos Java, com atualização das referências consumidoras.

- **ContactMessage** e componentes relacionados: o registro representa uma mensagem enviada pelo formulário, não uma pessoa de uma agenda de contatos.
- **TopicSuggestion** e componentes relacionados: expressa a sugestão de tema e elimina a abreviação de package `sugest`.
- **Episode** e componentes relacionados: alinha o domínio Java à tabela `episodes` e ao recurso REST já existentes. O package `video` passou a `episode`; `YouTubeClient` e `YouTubeUrl` foram movidos junto.
- **CreateContactMessageRequest**, **CreateTopicSuggestionRequest**, **CreateEpisodeRequest** e **CreatePostRequest**: diferenciam comandos de criação das entidades e dos DTOs de atualização.
- **JwtTokenService**: o componente possui chave/configuração injetada e responsabilidade de emissão/leitura de tokens; não é apenas utilitário estático.
- **ProblemResponseWriter**: descreve a escrita de respostas HTTP de erro e diferencia esse papel do `ApiExceptionHandler`.
- **PaginationSupport**: explicita que a classe auxilia a construção de paginação e respostas.
- A variável interna `video` em `EpisodeService` passou a `episode`.

Não foram alteradas regras de negócio, algoritmos, limites de validação, códigos HTTP ou mensagens. Comentários de crédito existentes foram preservados; os testes de regressão possuem `@author oEnzoRibas`. Esta etapa não antecipou a documentação Javadoc extensiva.

## 2. Mecanismos preservados para compatibilidade

### JPA

| Classe nova | Nome lógico JPA preservado | Tabela preservada |
| --- | --- | --- |
| ContactMessage | Contato | contatos |
| TopicSuggestion | Sugestao | sugestoes |
| Episode | Video | episodes |

Foi introduzido apenas o nome lógico explícito em `@Entity(name = "...")` nas três classes renomeadas. Antes, esse nome era inferido da classe antiga. A explicitação mantém válido o JPQL antigo; não altera tabelas ou colunas.

Todos os atributos, tipos, nomes físicos explícitos, enum values, nulabilidade, tamanhos e annotations de validação permanecem. Não houve renomeação de fields JPA nem necessidade de migration nesta etapa.

### Beans Spring

Os nomes anteriormente inferidos foram explicitados:

- `contatoController`, `contatoService`, `contatoRepository`.
- `sugestaoController`, `sugestaoService`, `sugestaoRepository`.
- `videoController`, `videoService`, `videoRepository`.
- `jwtTokenUtil`.

Isso preserva referências por nome, qualifiers e possíveis expressões. Injeções por tipo foram atualizadas para as classes novas. Os testes verificam os dez identificadores.

### Jackson, Spring MVC e queries

- Campos JSON, componentes de records e nomes dos getters/setters foram preservados.
- Endpoints, paths de imagens, nomes dos parâmetros HTTP e multipart foram preservados.
- `@PreAuthorize` e expressões SpEL permanecem iguais.
- Métodos derivados, como `findByYoutubeId` e `existsByYoutubeId`, continuam com os mesmos nomes.
- A projeção JPQL `PostResponse` não precisou mudar, pois sua classe não foi renomeada.
- Nomes de ordenação `createdAt` e `id`, cabeçalhos `X-Total-*` e propriedades de configuração permanecem.
- A busca por referências antigas não encontrou imports/packages antigos ativos. Nomes legados permanecem intencionalmente em metadados JPA, beans, testes de compatibilidade e documentação histórica.
- Não foram encontradas chamadas `Class.forName` dependentes dos nomes renomeados no código examinado. Configurações externas não disponíveis continuam fora dessa comprovação.

## 3. Nomes deliberadamente preservados

- `BackApplication`, package raiz e módulo Maven: referenciados pelo `.run`.
- `nome`, `assunto`, `mensagem`, `tema`: preservam serialização e persistência.
- `youtubeId`, `videoUrl`, `thumbnailUrl`, `createdAt`: preservam contratos, colunas inferidas e queries derivadas.
- `Contato`, `Sugestao`, `Video` como nomes lógicos JPA.
- Identificadores legados de beans listados acima.
- `User.Role.USER` e `User.Role.ADMIN`.
- Todos os endpoints, nomes de tabelas/colunas, índices e nomes de migrations.
- `.run/BackApplication.run.xml` e suas opções EnvFile/profile dev.

Os identificadores legados mantidos por compatibilidade não representam uma tradução incompleta acidental; são fronteiras deliberadas da etapa.

## 4. Itens API_IMPACT adiados

- Traduzir propriedades públicas de contato/sugestão.
- Renomear `/contatos` ou `/sugestoes`.
- Alterar `videoUrl`, `youtubeId` ou a identificação dos episódios.
- Alterar formato de tags, paginação, respostas ou códigos HTTP.
- Introduzir mudanças decorrentes de OpenAPI: não implementado nesta etapa.

## 5. Itens DATABASE_IMPACT adiados

- Traduzir nomes físicos de tabelas e colunas.
- Modificar constraints, índices, tipos, nulabilidade ou dados.
- Rever/validar a V1 local alterada e a V2 anterior ainda não rastreada.
- Adoção de Flyway, baseline e histórico real de produção.
- Migração de UUIDs e de papéis administrativos.

A V1 e a V2 já presentes no início da etapa permaneceram byte a byte iguais. Preservá-las não significa aprová-las para produção.

## 6. Itens CONFIGURATION_IMPACT adiados

- Renomear classe principal/módulo e atualizar `.run`.
- Versionar template da IDE ou alterar precedência EnvFile/runconfig.
- Mudar profiles, variáveis de ambiente, propriedades Spring ou YAML.
- Rever baseline automático do profile dev.
- Alterar POM, dependências ou plugins.
- Definir CLI/CI externo do Flyway ou alternativa phpMyAdmin.
- Documentar/implementar o deploy definitivo da Hostinger nas etapas previstas.

## 7. Build, testes e verificações

### Ambiente

- Java de execução: 23.0.2.
- Compilação: `release 21`, conforme o projeto.
- Maven via wrapper do repositório.
- Profile temporário de execução de testes: `stage2-verification`.
- Bancos efêmeros H2 em modo MariaDB; nenhum datasource de produção.
- Flyway explicitamente desativado.
- `.env` não carregado, `.run` não executado.
- Hibernate criou/descartou apenas schemas de testes em memória, conforme `create-drop`.

### Comando de validação final (PowerShell)

```powershell
.\mvnw.cmd -B -ntp clean test `
  "-Dspring.profiles.active=stage2-verification" `
  "-Dspring.flyway.enabled=false" `
  "-Dspring.datasource.url=jdbc:h2:mem:stage2-verification;MODE=MariaDB;DB_CLOSE_DELAY=-1" `
  "-Dspring.datasource.driver-class-name=org.h2.Driver" `
  "-Dspring.datasource.username=sa" `
  "-Dspring.datasource.password=" `
  "-Dspring.jpa.hibernate.ddl-auto=create-drop" `
  "-Dapp.cors.allowed-origins=http://localhost:5173"
```

As propriedades foram fornecidas somente à execução de testes; os arquivos de configuração não foram editados. O CORS explícito supre a propriedade ausente no YAML de testes.

O build limpo removeu os artefatos gerados em target e recompilou 46 fontes de produção e duas classes de teste. Essa limpeza evita que classes antigas continuem no classpath após os movimentos de arquivos.

### Resultados

| Teste | Quantidade | Falhas | Erros | Ignorados |
| --- | ---: | ---: | ---: | ---: |
| BackApplicationTests | 1 | 0 | 0 | 0 |
| NamingCompatibilityTests | 8 | 0 | 0 | 0 |
| **Total** | **9** | **0** | **0** | **0** |

**BUILD SUCCESS** no build limpo. O teste existente também passou numa execução preliminar antes das renomeações; a execução limpa é a evidência final.

Os oito testes adicionados verificam:

1. Nomes JSON das três entidades renomeadas.
2. Desserialização dos DTOs com os nomes de propriedades antigos.
3. Nomes lógicos JPA e tabelas preservados.
4. Resolução dos dez identificadores de beans.
5. Persistência, JPQL legado, consultas derivadas e projeção de posts.
6. Endpoints públicos de contato/sugestão e listagem de episódios.
7. Binding multipart e resposta JSON de criação de post.
8. Restrições de administrador após renomear controllers.

Os testes não chamam a API externa do YouTube e não executam migrations.

### Integridade de arquivos fora do escopo

Comparação SHA-256 antes/depois em 12 arquivos, sem diferenças:

- `.gitignore`
- `src/main/resources/README.MD`
- `.env.example`
- `pom.xml`
- `src/main/resources/application-prod.yaml`
- `src/main/resources/db/migration/V1__database.sql`
- `src/main/resources/application-dev.yaml`
- `.run/BackApplication.run.xml`
- `src/main/java/db/migration/V2__functional_schema.java`
- `src/main/resources/server.p12`
- `src/test/resources/application.yaml`
- `src/main/resources/application.yaml`

O relatório da etapa 01 permanece como registro histórico dos nomes anteriores.

## 8. Riscos e dúvidas restantes

- O projeto continua contendo alterações funcionais anteriores ainda não commitadas. Esta etapa não valida essas alterações para produção.
- O schema real e o artefato publicado permanecem desconhecidos.
- H2 e os testes de compatibilidade não comprovam upgrade MariaDB ou segurança da V2.
- Não houve execução em JVM 21; houve compilação com release 21 e execução em Java 23.
- O contexto de teste emite aviso de open-in-view habilitado, diferente do YAML principal; a configuração de teste foi preservada e essa divergência continua pendente.
- Mockito/Byte Buddy emitem avisos de autoanexação de agente no Java 23; não impedem a execução atual.
- Integrações externas que referenciem nomes Java completos, fora dos arquivos disponíveis, precisam ser inventariadas antes de uma release.
- O resultado não é autorização de deploy, commit ou migration.

## Encerramento

Etapa 02 concluída e interrompida no limite previsto. Foram realizadas apenas mudanças internas de nomes, mecanismos explícitos para conservar os identificadores anteriores, testes de regressão e documentação. Não foi iniciada a etapa 03 nem feita qualquer alteração no banco da Hostinger.
