Refatorei recentemente o `pom.xml`, os arquivos `application*.yaml` e os arquivos relacionados às variáveis de ambiente (`.env` / `.env.example`).

Quero realizar uma revisão estrutural completa do backend antes de continuar desenvolvendo novas funcionalidades.

IMPORTANTE: este sistema já está rodando em PRODUÇÃO, hospedado na Hostinger, utilizando MariaDB com dados reais. Portanto, qualquer alteração que possa afetar banco de dados, schema, migrations, variáveis de ambiente, autenticação, deploy ou compatibilidade com dados existentes deve ser tratada como potencialmente crítica.

O objetivo não é simplesmente "limpar" ou "refatorar" o projeto. Quero estabelecer uma baseline profissional e segura do sistema existente antes de continuar sua evolução.

A ordem de trabalho deve ser:

1. compreender e inventariar o estado atual;
2. analisar e padronizar nomenclaturas;
3. avaliar impactos no banco de dados e produção;
4. definir uma estratégia segura de migração e deploy;
5. registrar decisões arquiteturais importantes em ADRs;
6. documentar o código existente;
7. documentar a API com OpenAPI/Swagger;
8. somente depois discutir refatorações funcionais ou arquiteturais.

Não pule etapas.

## 1. Auditoria inicial do projeto

Comece fazendo uma análise completa do estado atual do projeto.

Analise:

* `pom.xml` e todas as dependências;
* `application.yaml`;
* `application-dev.yaml`;
* `application-prod.yaml`;
* `.env`, `.env.example` e demais configurações de ambiente;
* configuração do Spring Boot;
* configuração do MariaDB;
* Flyway e migrations existentes;
* estrutura de packages;
* controllers;
* services;
* repositories;
* entities;
* DTOs;
* configurations;
* security;
* exceptions;
* utilities;
* testes;
* demais componentes relevantes.

Analise também as dependências Maven.

Verifique:

* dependências redundantes;
* dependências não utilizadas;
* dependências vulneráveis ou desatualizadas;
* scopes incorretos;
* dependências que deveriam estar apenas em desenvolvimento/testes;
* conflitos de versões;
* dependências transitivas relevantes;
* configurações potencialmente inseguras.

Não remova ou atualize automaticamente uma dependência apenas porque existe uma versão mais recente. Primeiro determine impacto, compatibilidade e necessidade.

## 2. Padronização do projeto em inglês

ANTES de iniciar a documentação extensiva, quero analisar a nomenclatura atual do projeto.

O objetivo é padronizar os identificadores técnicos em inglês.

Analise nomes de:

* packages;
* classes;
* interfaces;
* enums;
* métodos;
* variáveis;
* constantes;
* DTOs;
* records;
* exceptions;
* services;
* repositories;
* controllers;
* endpoints;
* propriedades de configuração;
* variáveis de ambiente;
* entities;
* atributos JPA;
* tabelas;
* colunas;
* constraints;
* índices;
* sequences;
* migrations.

Identifique nomes em português, misturas entre português e inglês, abreviações inconsistentes e nomenclaturas que não representem corretamente o domínio.

Para cada caso relevante, proponha uma nomenclatura em inglês consistente.

A documentação NÃO precisa obrigatoriamente estar em inglês nesta etapa. O foco dessa padronização são os identificadores técnicos do sistema.

Não faça traduções literais quando elas produzirem nomes ruins. Utilize terminologia comum em engenharia de software e termos adequados ao domínio da aplicação.

## 3. Diferencie refatoração Java de alteração do banco

Não trate renomear um atributo Java e renomear uma coluna do banco como se fossem a mesma operação.

Para cada mudança de nomenclatura envolvendo JPA, determine explicitamente se ela afeta:

* somente o código Java;
* serialização JSON;
* contrato da API;
* nome físico de tabela;
* nome físico de coluna;
* foreign keys;
* constraints;
* índices;
* queries;
* repositories;
* migrations Flyway;
* dados existentes;
* integrações externas.

Quando for possível melhorar o nome Java sem alterar imediatamente o schema existente, considere explicitamente o uso de mapeamentos como `@Table` e `@Column` para preservar compatibilidade.

Não renomeie automaticamente tabelas ou colunas de produção apenas para atingir consistência estética.

## 4. Banco de dados em produção

O MariaDB da Hostinger já contém o schema utilizado pela aplicação em produção.

Portanto, antes de alterar qualquer elemento persistido, faça um inventário do schema esperado pela aplicação e das migrations Flyway existentes.

Analise cuidadosamente:

* tabelas existentes;
* colunas;
* tipos;
* nullability;
* primary keys;
* foreign keys;
* unique constraints;
* índices;
* defaults;
* relacionamentos;
* nomes utilizados pelas entities;
* migrations já aplicadas;
* ordem das migrations;
* configuração de `ddl-auto`;
* configuração de `baseline`;
* Flyway schema history.

NUNCA altere uma migration Flyway que já tenha sido aplicada em produção.

Mudanças no schema devem ser introduzidas por novas migrations versionadas.

Antes de propor uma migration, determine:

* impacto nos dados existentes;
* compatibilidade com a versão atualmente implantada;
* possibilidade de rollback;
* necessidade de backup;
* necessidade de janela de manutenção;
* possibilidade de executar a migration de forma backward-compatible;
* impacto de locks e operações DDL no MariaDB;
* impacto caso o deploy falhe depois da migration.

Quando houver risco, prefira estratégias de migração compatíveis com versões anteriores, incluindo quando apropriado abordagens do tipo expand-and-contract.

## 5. Segurança da produção

A prioridade absoluta é NÃO quebrar a aplicação ou corromper/perder dados existentes.

Antes de qualquer alteração relacionada a banco ou deploy:

* identificar riscos;
* definir pré-condições;
* definir procedimento de backup;
* definir procedimento de validação;
* definir estratégia de rollback;
* determinar como verificar a migration antes de produção;
* determinar como testar a aplicação contra um schema equivalente ao de produção.

Nunca assuma que uma migration é segura apenas porque funciona em um banco local vazio.

Considere explicitamente o cenário de upgrade de uma versão REAL anterior da aplicação para a nova versão.

## 6. ADRs

Quero começar a registrar decisões arquiteturais importantes utilizando Architecture Decision Records.

Analise quais decisões atuais merecem ADR e proponha uma estrutura como:

`docs/adr/`

Os ADRs devem registrar contexto, problema, decisão, alternativas consideradas, consequências e implicações operacionais quando aplicável.

Crie um ADR específico para a estratégia de banco de dados, migrations e deploy em produção.

Esse ADR deve documentar, no mínimo:

* MariaDB como banco de produção;
* Hostinger como ambiente atual de produção;
* Flyway como mecanismo de versionamento do schema;
* política para criação de migrations;
* política de nunca modificar migrations já aplicadas;
* estratégia de nomenclatura do banco;
* estratégia para renomear tabelas e colunas;
* compatibilidade entre releases;
* backup antes de migrations críticas;
* validação pré-deploy;
* deploy;
* smoke tests pós-deploy;
* rollback da aplicação;
* rollback/forward-fix do banco;
* recuperação em caso de migration parcialmente problemática;
* procedimento para alterações incompatíveis;
* estratégia de expand-and-contract quando necessária.

Não registre apenas "o que fazemos". Explique também POR QUE a decisão foi tomada e quais problemas ela pretende evitar.

## 7. Documentação do processo de deploy

Além do ADR, documente o processo operacional de deploy.

Quero que seja possível para outro desenvolvedor entender como colocar uma nova versão em produção com segurança.

Documente:

* pré-requisitos;
* build;
* profiles Spring;
* variáveis de ambiente;
* secrets necessários;
* configuração de produção;
* conexão com MariaDB;
* Flyway;
* backup;
* validações pré-deploy;
* ordem das operações;
* inicialização da aplicação;
* migrations;
* health checks;
* smoke tests;
* logs que devem ser observados;
* validação pós-deploy;
* rollback;
* troubleshooting.

Nunca coloque valores reais de secrets na documentação.

Use placeholders e explique como os secrets devem ser fornecidos.

## 8. Documentação das configurações

Analise `application*.yaml`, `.env.example` e configurações relacionadas.

Crie uma documentação centralizada das variáveis de ambiente contendo, para cada variável:

* nome;
* finalidade;
* obrigatória ou opcional;
* valor padrão;
* profiles nos quais é utilizada;
* se contém informação sensível;
* formato esperado;
* exemplo seguro;
* consequências caso esteja ausente ou inválida.

Avalie também se os nomes atuais das variáveis de ambiente estão consistentes.

## 9. Javadoc

Somente depois da análise e da padronização de nomenclatura, comece a documentação detalhada do código Java.

Quero Javadoc de nível profissional, comparável a projetos open source maduros.

Documente classes e métodos relevantes explicando:

* responsabilidade;
* comportamento;
* invariantes;
* parâmetros;
* retorno;
* exceções;
* side effects;
* pré-condições;
* decisões não óbvias;
* comportamento relacionado à segurança quando relevante.

Nas classes mantenha:

`@author oEnzoRibas`

Evite documentação artificial.

Não adicione comentários que simplesmente traduzam o código para linguagem natural.

Getters, setters, constructors triviais e métodos autoexplicativos não precisam receber documentação apenas para aumentar cobertura.

## 10. OpenAPI / Swagger

Documente adequadamente a API REST utilizando OpenAPI/Swagger.

Analise todos os controllers e endpoints existentes.

Documente, quando aplicável:

* objetivo;
* path;
* método HTTP;
* parâmetros;
* request body;
* responses;
* códigos HTTP;
* autenticação;
* autorização;
* erros;
* schemas;
* DTOs;
* validações.

A documentação OpenAPI deve descrever o contrato HTTP.

O Javadoc deve explicar o código e suas responsabilidades.

Evite duplicação desnecessária entre ambos.

IMPORTANTE: não altere contratos públicos da API apenas para melhorar nomenclatura sem antes identificar o impacto sobre clientes existentes.

## 11. Estrutura da documentação

Analise qual estrutura faz mais sentido para o projeto.

Considere algo próximo de:

`README.md`
`docs/architecture/`
`docs/adr/`
`docs/api/`
`docs/configuration/`
`docs/deployment/`
`docs/security/`

Não crie arquivos apenas para preencher essa estrutura. Crie somente documentos que tenham responsabilidade clara e conteúdo útil.

## 12. Forma de execução

Não faça centenas de alterações de uma vez.

Trabalhe incrementalmente.

Para cada etapa:

1. analise;
2. apresente os problemas encontrados;
3. classifique o impacto;
4. proponha as mudanças;
5. identifique riscos;
6. somente então implemente o que for seguro dentro do escopo atual;
7. valide o resultado.

Quando uma mudança tiver potencial de afetar produção, pare antes de executá-la e apresente:

* estado atual;
* mudança proposta;
* motivo;
* impacto;
* risco;
* migration necessária;
* estratégia de backup;
* estratégia de rollback;
* validações necessárias.

Não faça alterações destrutivas automaticamente.

## Resultado esperado

Ao final dessa fase quero ter:

* nomenclatura técnica consistente e predominantemente em inglês;
* entendimento claro do schema atual;
* estratégia definida para nomenclatura do banco;
* migrations Flyway tratadas de forma segura;
* dependências Maven revisadas;
* configurações Spring revisadas;
* variáveis de ambiente organizadas;
* decisões arquiteturais registradas em ADR;
* processo de deploy da Hostinger documentado;
* estratégia explícita de proteção do MariaDB de produção;
* Javadocs profissionais;
* API documentada com OpenAPI/Swagger;
* documentação de configuração e operação;
* lista separada de melhorias arquiteturais futuras.

O princípio central é:

**Primeiro entender. Depois padronizar. Depois proteger a migração. Depois documentar. Só então refatorar comportamento.**

Preserve compatibilidade com produção sempre que possível e nunca sacrifique segurança dos dados apenas para obter consistência estética no código ou no schema.
