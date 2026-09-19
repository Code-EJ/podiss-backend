Agora documente o processo operacional de deploy do backend.

Utilize apenas informações verificáveis no projeto e informações confirmadas nas etapas anteriores.

Não invente detalhes da infraestrutura da Hostinger.

Quando uma informação não estiver disponível, marque claramente como:

`TODO / INFORMATION REQUIRED`

Crie documentação apropriada em `docs/deployment/`.

Quero um RUNBOOK operacional que outro desenvolvedor consiga seguir.

Documente:

* arquitetura de deployment conhecida;
* requisitos do ambiente;
* Java/JDK necessário;
* processo de build;
* artefato gerado;
* Spring profile utilizado;
* variáveis de ambiente;
* secrets;
* MariaDB;
* Flyway;
* TLS;
* portas;
* reverse proxy, se existir;
* inicialização;
* logs;
* health checks;
* smoke tests.

Separe claramente:

PRE-DEPLOY

* verificar versão;
* verificar migrations;
* verificar configuração;
* verificar secrets;
* executar testes;
* criar backup quando necessário;
* validar possibilidade de restore;
* verificar compatibilidade da aplicação nova com o schema existente.

DEPLOY

* ordem das operações;
* atualização do artefato;
* configuração;
* inicialização;
* execução/validação das migrations;
* acompanhamento dos logs.

POST-DEPLOY

* verificar inicialização;
* verificar Flyway;
* verificar conexão com MariaDB;
* executar smoke tests;
* verificar endpoints críticos;
* verificar autenticação;
* verificar logs;
* verificar métricas disponíveis.

ROLLBACK / RECOVERY

Documente separadamente:

* rollback da aplicação;
* problemas de migration;
* forward-fix;
* restore de backup;
* falha de inicialização;
* falha de conexão com MariaDB;
* configuration/secrets incorretos.

Crie também um checklist de deploy que possa ser reutilizado em releases futuras.

NUNCA coloque valores reais de:

* passwords;
* JWT secrets;
* API keys;
* certificates;
* database credentials.

Utilize placeholders.

Analise `.env.example` e determine se ele representa corretamente todas as variáveis necessárias.

Caso seja necessário melhorar `.env.example`, faça isso sem adicionar secrets reais.

Ao final, apresente lacunas existentes na documentação e informações da Hostinger que ainda precisam ser confirmadas manualmente.
