# Desenvolvimento local reproduzível

Créditos: **oEnzoRibas**. Escopo: somente computador local. Não copiar compose.dev.yaml, volume ou credenciais locais para produção.

## Componentes

- Java 21 no Dockerfile; Maven 3.9.9 no builder.
- MariaDB 11.8.9 no serviço db, sem porta publicada.
- Flyway 10.20.1 em processo separado, exclusivamente db/fresh-migration.
- Aplicação sem root, filesystem somente leitura, tmp temporário, sem capabilities.
- HTTP somente em 127.0.0.1:18080. O healthcheck consulta GET /episodes?size=1: verifica HTTP e acesso ao banco, não é um Actuator completo.
- Volume podiss-dev_database persiste os dados locais.
- A aplicação espera banco saudável e migration concluída. [Ordem de startup no Compose](https://docs.docker.com/compose/how-tos/startup-order/).

Tags de imagens fixam versões principais/exatas conforme disponíveis, mas não garantem imutabilidade de digest. Para release, registrar digest e promover o mesmo artefato testado; não reconstruir silenciosamente outro artefato no servidor.

## 1. Preparar

Instale Git, Docker Desktop com engine Linux e Docker Compose v2 recente (suporte a --wait). Para executar Maven fora do contêiner, instale JDK 21. Não use banco remoto de produção.

Abra o Docker Desktop e espere iniciar. Entre na pasta do projeto usando **um** dos terminais:

PowerShell:

```powershell
cd "F:\Users\Enzo HD\Github\Repos\CODEJR\podiss\podiss-backend"
```

Git Bash (prompt MINGW64):

```bash
cd "/f/Users/Enzo HD/Github/Repos/CODEJR/podiss/podiss-backend"
```

Se clonou em outro local, ajuste somente esse caminho. Todos os próximos comandos partem da raiz do projeto.

Na raiz do projeto, copie .env.dev.example para .env.dev se ainda não existir. Nesta sessão já foi criada uma .env.dev com valores aleatórios exclusivos de desenvolvimento; **não sobrescreva se o ambiente já foi inicializado**.

Preencha DEV_DB_PASSWORD, DEV_DB_ROOT_PASSWORD, DEV_JWT_SECRET e DEV_ADMIN_PASSWORD. JWT exige Base64 de pelo menos 64 bytes aleatórios. Senha do administrador: 12 caracteres no mínimo e até 72 bytes UTF-8. Não use exemplos em produção.

Para gerar cada valor no PowerShell, execute manualmente (gera no console; não cole o resultado em chat/logs):

```powershell
$bytes = New-Object byte[] 64
$rng = [System.Security.Cryptography.RandomNumberGenerator]::Create()
$rng.GetBytes($bytes)
[Convert]::ToBase64String($bytes)
$rng.Dispose()
```

Use 24 bytes para senhas e 64 para JWT. Gere valores diferentes para cada campo. .env.dev é ignorado pelo Git e excluído da imagem. Ele não é a .env usada pela IDE e não configura o servidor.

## 2. Subir e desenvolver

PowerShell:

```powershell
.\scripts\dev.ps1 Up
.\scripts\dev.ps1 Status
.\scripts\dev.ps1 Validate
```

Git Bash, a partir da raiz (não execute .ps1 diretamente):

```sh
docker compose --env-file .env.dev -f compose.dev.yaml up -d --build --wait --wait-timeout 180
docker compose --env-file .env.dev -f compose.dev.yaml ps -a
docker compose --env-file .env.dev -f compose.dev.yaml run --rm migrate validate
```

Abra http://localhost:18080/episodes. Lista vazia é normal na instalação inicial. Login de desenvolvimento: usuário dev_admin; senha é DEV_ADMIN_PASSWORD da sua .env.dev. Não há credenciais de produção nessa base.

Resultado esperado: app e db como healthy; migrate como Exited (0), pois o executor termina após aplicar/validar as migrations. Swagger: http://localhost:18080/swagger-ui/index.html. Se mudou DEV_HTTP_PORT, ajuste a porta em todas as URLs deste tutorial.

Se aparecer syntax error near unexpected token ']' em [CmdletBinding()], você tentou executar PowerShell no Bash. Use os comandos Docker acima ou, no Git Bash:

```bash
powershell.exe -NoProfile -File ./scripts/dev.ps1 Up
```

Executar o script sem Up apenas mostra o status; não inicia o ambiente.

Após editar Java, execute Up novamente para recompilar imagem e recriar a aplicação. Não há hot reload/debug remoto automático. Flyway valida o histórico e só aplica versões pendentes; não edite migration já aplicada.

Veja logs com `scripts/dev.ps1 Logs` e histórico com `scripts/dev.ps1 Info`. Não publique saída de `docker compose config` ou `docker inspect` completo: pode revelar variáveis/segredos. Prefira config --quiet.

## 3. Testar a API e conferir o banco

### 3.1 Criar um contato pela API

Escolha somente o exemplo do seu terminal. Cada execução cria um registro de teste **no banco local**, sem acessar a Hostinger.

PowerShell:

```powershell
$dados = @{
    nome = "Teste local"
    email = "teste@example.com"
    assunto = "Verificacao do banco"
    mensagem = "Mensagem criada pela API local."
} | ConvertTo-Json

$contato = Invoke-RestMethod -Method Post -Uri "http://localhost:18080/contatos" -ContentType "application/json" -Body $dados
$contato
```

Git Bash:

```bash
curl -i -X POST http://localhost:18080/contatos \
  -H 'Content-Type: application/json' \
  --data '{"nome":"Teste local","email":"teste@example.com","assunto":"Verificacao do banco","mensagem":"Mensagem criada pela API local."}'
```

Esperado: HTTP 201 e JSON com id, nome, email, assunto, mensagem e createdAt. O PowerShell mostra o objeto retornado. Anote o id para comparar com o SELECT abaixo. Se receber 429 após repetir testes, respeite o Retry-After antes de tentar novamente.

Alternativa visual: Swagger → POST /contatos → Try it out → preencher o mesmo JSON → Execute. Essa rota não exige login; GET /contatos exige ADMIN.

### 3.2 Entrar no MariaDB local

O comando é o mesmo no PowerShell e Git Bash:

```sh
docker compose --env-file .env.dev -f compose.dev.yaml exec db mariadb -u podiss_dev -p podiss_dev
```

Digite a senha de DEV_DB_PASSWORD da sua .env.dev quando solicitada; os caracteres não aparecem. Não use DEV_ADMIN_PASSWORD nem senha da Hostinger. Não coloque a senha na linha do comando. Se o Git Bash reclamar de TTY, prefira executar esse comando no PowerShell.

No prompt MariaDB, execute:

```sql
SELECT DATABASE();
SHOW TABLES;

SELECT id, sender_name, email, subject, message, created_at
FROM contact_messages
WHERE email = 'teste@example.com'
ORDER BY created_at DESC;

SELECT version, description, success
FROM flyway_schema_history;

exit;
```

Esperado: DATABASE() = podiss_dev, contato com o mesmo id retornado pela API e migration V1 com success = 1. O JSON usa nomes legados em português; o banco usa nomes físicos em inglês. Essa conferência demonstra API → aplicação → banco. O histórico Flyway demonstra a migration, não a gravação do contato.

### 3.3 Confirmar persistência

Pare e inicie conforme as seções abaixo, depois repita o SELECT. O contato deve continuar no banco porque o volume é preservado. Não use down -v nem apague volumes no Docker Desktop.

Para operações administrativas no Swagger, faça POST /api/auth/login com username dev_admin e a senha DEV_ADMIN_PASSWORD da .env.dev; copie o token retornado para Authorize (somente o token, sem escrever Bearer). Não compartilhe senha/token em prints ou logs. O bootstrap não troca a senha de uma conta já existente.

## 4. Testes antes de promover mudanças

O build Docker executa os testes unitários/H2 em Java 21 e gera o JAR (17 testes na baseline da etapa 10). A suíte completa daquela baseline tem 30 testes, incluindo 13 de integração. H2 não substitui MariaDB.

Para a suíte completa, use JDK 21 e Docker local ativo e o comando clean verify documentado no [relatório da etapa 04](../ADRS/0002/relatorios/etapa04-instalacao-limpa.md). Os IT criam bases descartáveis separadas e não alteram o volume de desenvolvimento.

O workflow .github/workflows/verify.yml executa Maven verify com Java 21 e Testcontainers em runner Ubuntu, depois constrói a imagem. Ele não recebe secrets, não publica imagem e não faz deploy. Foi preparado, mas só pode ser confirmado em GitHub após push/execução autorizados. Não há push automático nesta entrega.

Antes de release: testes rápidos, integração MariaDB, Flyway validate, smoke HTTP/login, revisão de diff/migrations, backup quando aplicável e aprovação do alvo. “Passou local” não comprova proxy/TLS/firewall/backup da VPS.

## 5. Parar sem perder dados

PowerShell:

```powershell
.\scripts\dev.ps1 Down
```

Git Bash:

```bash
docker compose --env-file .env.dev -f compose.dev.yaml down
```

Down remove somente contêineres/rede do projeto Compose local e preserva o volume. Não foi incluída ação de reset ou remoção de volume. Não usar down -v como rotina; destruiria os dados locais.

Se mudar senha do banco na .env.dev após a primeira inicialização, o MariaDB existente não muda a senha automaticamente. Rever credenciais ou fazer mudança administrativa deliberada; não apagar volume para contornar sem avaliar conteúdo. O bootstrap também não redefine senha de administrador existente.

## 6. Diagnosticar problemas

PowerShell:

```powershell
.\scripts\dev.ps1 Status
.\scripts\dev.ps1 Logs
.\scripts\dev.ps1 Info
.\scripts\dev.ps1 Validate
```

Git Bash:

```bash
docker compose --env-file .env.dev -f compose.dev.yaml ps -a
docker compose --env-file .env.dev -f compose.dev.yaml logs --tail 100 app migrate
docker compose --env-file .env.dev -f compose.dev.yaml run --rm migrate info
docker compose --env-file .env.dev -f compose.dev.yaml run --rm migrate validate
```

Falha de conexão com o Docker: verificar Docker Desktop/engine Linux. Falta de DEV_*: preencher .env.dev sem sobrescrever credenciais de um volume existente. App unhealthy: conferir logs; não apagar banco nem executar clean/repair para tentar resolver. Remova secrets/dados pessoais antes de compartilhar logs.

## 7. IDE

.run preservado continua usando .env e profile dev, não .env.dev. O Compose não publica o banco; portanto não apontar a IDE para db:3306 do host. Use a aplicação no contêiner neste fluxo. Um modo de debug/porta local de banco pode ser adicionado depois com isolamento explícito.

## Segurança e limites

- Credenciais de desenvolvimento são privadas locais, mas ficam no ambiente dos contêineres; quem controla Docker pode acessá-las.
- Não compartilhar .env.dev, .local, chaves ou volume.
- Não há HTTPS local; produção exige TLS e política de proxy explícitos.
- Não habilitar MySQL/SSH públicos para facilitar desenvolvimento.
- Flyway atual emite aviso de suporte declarado ao MariaDB 11.8.9; testes específicos passam, mas revisão de dependências permanece necessária.
