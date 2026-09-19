Utilize exclusivamente as mudanças de banco APROVADAS após a Etapa 3.

Não introduza novas alterações de schema além das aprovadas.

Implemente as migrations Flyway necessárias como NOVAS migrations versionadas.

NUNCA modifique uma migration que já tenha sido aplicada em produção.

Antes de implementar cada migration, confirme no código:

* estado anterior esperado;
* estado posterior esperado;
* entities envolvidas;
* constraints;
* foreign keys;
* índices;
* queries dependentes;
* compatibilidade da aplicação.

As migrations devem preservar os dados existentes.

Quando possível, prefira migrations backward-compatible.

Quando uma mudança exigir expand-and-contract, implemente apenas a fase correspondente ao deploy atual e documente explicitamente as fases futuras.

Não faça DROP prematuro de:

* tabelas;
* colunas;
* índices;
* constraints;
* dados.

Não execute nenhuma migration contra produção.

Implemente e valide utilizando ambiente local/de teste.

IMPORTANTE:

Não teste somente criando um banco vazio.

Quando possível, teste também o caminho real de upgrade:

SCHEMA DA VERSÃO ANTERIOR
→ migrations novas
→ SCHEMA NOVO

Valide:

* Flyway;
* Hibernate `validate`;
* inicialização da aplicação;
* integridade dos dados;
* constraints;
* relacionamentos;
* repositories;
* queries;
* testes de integração.

Crie testes de migration quando a infraestrutura atual permitir.

Ao final apresente:

1. migrations criadas;
2. SQL executado por cada migration;
3. motivo;
4. compatibilidade;
5. riscos;
6. validações realizadas;
7. resultado dos testes;
8. procedimento necessário antes de produção;
9. rollback/forward-fix esperado.

NÃO execute deploy.

NÃO conecte ao banco de produção.

Pare após deixar as migrations prontas e validadas.
