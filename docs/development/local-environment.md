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

Equivalente no terminal, a partir da raiz:

```sh
docker compose --env-file .env.dev -f compose.dev.yaml up -d --build --wait --wait-timeout 180
docker compose --env-file .env.dev -f compose.dev.yaml ps -a
docker compose --env-file .env.dev -f compose.dev.yaml run --rm migrate validate
```

Abra http://localhost:18080/episodes. Lista vazia é normal na instalação inicial. Login de desenvolvimento: usuário dev_admin; senha é DEV_ADMIN_PASSWORD da sua .env.dev. Não há credenciais de produção nessa base.

Após editar Java, execute Up novamente para recompilar imagem e recriar a aplicação. Não há hot reload/debug remoto automático. Flyway valida o histórico e só aplica versões pendentes; não edite migration já aplicada.

Veja logs com `scripts/dev.ps1 Logs` e histórico com `scripts/dev.ps1 Info`. Não publique saída de `docker compose config` ou `docker inspect` completo: pode revelar variáveis/segredos. Prefira config --quiet.

## 3. Testes antes de promover mudanças

O build Docker executa os nove testes H2 em Java 21 e gera o JAR. H2 não substitui MariaDB.

Para a suíte completa, use JDK 21 e Docker local ativo e o comando clean verify documentado no [relatório da etapa 04](../ADRS/0002/relatorios/etapa04-instalacao-limpa.md). Os IT criam bases descartáveis separadas e não alteram o volume de desenvolvimento.

O workflow .github/workflows/verify.yml executa Maven verify com Java 21 e Testcontainers em runner Ubuntu, depois constrói a imagem. Ele não recebe secrets, não publica imagem e não faz deploy. Foi preparado, mas só pode ser confirmado em GitHub após push/execução autorizados. Não há push automático nesta entrega.

Antes de release: testes rápidos, integração MariaDB, Flyway validate, smoke HTTP/login, revisão de diff/migrations, backup quando aplicável e aprovação do alvo. “Passou local” não comprova proxy/TLS/firewall/backup da VPS.

## 4. Parar sem perder dados

```powershell
.\scripts\dev.ps1 Down
```

Down remove somente contêineres/rede do projeto Compose local e preserva o volume. Não foi incluída ação de reset ou remoção de volume. Não usar down -v como rotina; destruiria os dados locais.

Se mudar senha do banco na .env.dev após a primeira inicialização, o MariaDB existente não muda a senha automaticamente. Rever credenciais ou fazer mudança administrativa deliberada; não apagar volume para contornar sem avaliar conteúdo. O bootstrap também não redefine senha de administrador existente.

## 5. IDE

.run preservado continua usando .env e profile dev, não .env.dev. O Compose não publica o banco; portanto não apontar a IDE para db:3306 do host. Use a aplicação no contêiner neste fluxo. Um modo de debug/porta local de banco pode ser adicionado depois com isolamento explícito.

## Segurança e limites

- Credenciais de desenvolvimento são privadas locais, mas ficam no ambiente dos contêineres; quem controla Docker pode acessá-las.
- Não compartilhar .env.dev, .local, chaves ou volume.
- Não há HTTPS local; produção exige TLS e política de proxy explícitos.
- Não habilitar MySQL/SSH públicos para facilitar desenvolvimento.
- Flyway atual emite aviso de suporte declarado ao MariaDB 11.8.9; testes específicos passam, mas revisão de dependências permanece necessária.
