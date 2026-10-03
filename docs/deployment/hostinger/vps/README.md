# Deploy do PodISS na Hostinger VPS

Créditos: **oEnzoRibas**.

> Runbook operacional e de recuperação do ambiente de produção do PodISS na Hostinger.
> Este documento descreve o ambiente efetivamente montado em outubro de 2026 e o procedimento para reproduzi-lo em uma VPS limpa.
> **Nunca coloque senhas, chaves privadas, JWT, dumps, tokens ou conteúdo real de `.env.prod` neste arquivo ou em commits.**

## 1. Estado atual da produção

Arquitetura publicada:

```text
Internet
  |
  +-- https://podiss.com.br
  |       |
  |       +-- Nginx no host
  |               |
  |               +-- /var/www/podiss
  |                       |
  |                       +-- React + TypeScript + Vite
  |
  +-- https://www.podiss.com.br
  |       |
  |       +-- mesmo frontend
  |
  +-- https://api.podiss.com.br
          |
          +-- Nginx no host
                  |
                  +-- 127.0.0.1:18080
                          |
                          +-- podiss-backend:prod
                                  |
                                  +-- MariaDB 11.8.9
```

Componentes:

| Componente | Implementação |
| --- | --- |
| VPS | Hostinger VPS, Ubuntu LTS |
| IPv4 público atual | `145.223.92.36` |
| Domínio web | `podiss.com.br` |
| Alias web | `www.podiss.com.br` |
| API | `api.podiss.com.br` |
| Proxy / arquivos estáticos | Nginx no host |
| TLS | Let's Encrypt via Certbot |
| Backend | Spring Boot 3.4.1, Java 21 |
| Banco | MariaDB 11.8.9 em Docker |
| Migrations | Flyway 10.20.1 em container separado |
| Frontend | React + TypeScript + Vite |
| Build frontend | Node 22 em container Docker |
| Repositório backend | `Code-EJ/podiss-backend` |
| Repositório frontend | `Code-EJ/podiss-frontend` |
| Diretório de produção | `/opt/apps/app` |
| Frontend publicado | `/var/www/podiss` |
| Backend no host | somente `127.0.0.1:18080` |
| Banco no host | nenhuma porta publicada |

O nome DNS `api.podiss.com.br` aponta para a VPS. Ele não deve ser confundido com o hostname interno retornado pelo shell; confirme o hostname real com `hostnamectl` quando necessário.

Antes de reproduzir o ambiente, sempre confirme a distribuição real:

```bash
cat /etc/os-release
uname -a
```

Não assuma a versão do sistema apenas a partir de documentação antiga.

---

## 2. Regra de segurança: o que pode e o que não pode ir para o Git

### Pode ser versionado

- IPv4 público da VPS.
- Domínios.
- Nome do usuário operacional, como `deploy`.
- Portas públicas.
- Comandos de instalação.
- Templates com placeholders.
- Configuração Nginx sem secrets.
- Compose sem senhas hardcoded.
- Procedimentos de deploy, backup e rollback.

### Nunca versionar

- Senha de `root`.
- Senha do usuário `deploy`.
- Chave SSH privada.
- Conteúdo real de `.env.prod`.
- `DB_PASSWORD`.
- `DB_ROOT_PASSWORD`.
- `JWT_SECRET`.
- `ADMIN_PASSWORD`.
- Tokens JWT.
- API keys.
- Certificados/chaves privadas.
- Dumps reais de banco.
- Backups de volumes.

Segredos de produção devem existir em um gerenciador de senhas corporativo e, quando necessários em runtime, somente no servidor.

Permissões mínimas do arquivo de produção:

```bash
chmod 600 /opt/apps/app/.env.prod
```

### Sobre anotações locais com credenciais

Uma anotação do tipo:

```text
IPv4: <ip>
Usuário: root
senha vps: <senha>
user: deploy
pass: <senha>
ADMIN_USERNAME=<usuario>
ADMIN_EMAIL=<email>
ADMIN_PASSWORD=<senha>
```

**não deve ser enviada para o Git**.

Destino recomendado:

| Informação | Destino |
| --- | --- |
| IPv4 / domínios | este runbook |
| usuário `deploy` | este runbook |
| comandos operacionais | este runbook |
| senha root | password manager |
| senha deploy | password manager |
| chave privada SSH | somente `~/.ssh` na máquina autorizada |
| senha ADMIN | password manager |
| DB/JWT/API keys | `.env.prod` na VPS + password manager |

Se uma senha real já foi reutilizada, compartilhada ou colocada em texto aberto, troque-a. Em especial, não use senhas triviais como `deploypass` em produção.

---

## 3. Portas e firewall

Somente três portas precisam ser acessíveis publicamente:

| Porta | Serviço |
| ---: | --- |
| 22/TCP | SSH |
| 80/TCP | HTTP / emissão e redirecionamento TLS |
| 443/TCP | HTTPS |

**Não expor publicamente:**

- `8080` / `18080` do Spring.
- `3306` do MariaDB.

No firewall da Hostinger, manter regras de ACCEPT para TCP 22, 80 e 443 e bloquear o restante da entrada.

Na própria VPS:

```bash
sudo apt update
sudo apt install -y ufw fail2ban nginx git curl wget unzip

sudo ufw default deny incoming
sudo ufw default allow outgoing
sudo ufw allow OpenSSH
sudo ufw allow 'Nginx Full'
sudo ufw enable

sudo ufw status verbose
```

Estado esperado:

```text
22/tcp       ALLOW IN
80/tcp       ALLOW IN
443/tcp      ALLOW IN
```

Ativar serviços:

```bash
sudo systemctl enable --now nginx
sudo systemctl enable --now fail2ban
```

---

## 4. Usuário operacional

Use `root` apenas para bootstrap/recuperação. Operação diária deve usar o usuário `deploy`.

Na primeira configuração, como root:

```bash
adduser deploy
usermod -aG sudo deploy
usermod -aG docker deploy
id deploy
```

O resultado deve incluir os grupos `sudo` e `docker`.

Criar o diretório de aplicações:

```bash
mkdir -p /opt/apps
chown deploy:deploy /opt/apps
```

Depois:

```bash
su - deploy
mkdir -p /opt/apps/app
cd /opt/apps/app
```

### SSH por chave

A chave usada para entrar na VPS é diferente das chaves usadas pela VPS para acessar o GitHub.

Na máquina do operador, prefira Ed25519:

```bash
ssh-keygen -t ed25519 -a 100 -f ~/.ssh/podiss_vps_ed25519 -C podiss-vps
```

Apenas a chave pública vai para:

```text
/home/deploy/.ssh/authorized_keys
```

Na VPS:

```bash
mkdir -p /home/deploy/.ssh
chmod 700 /home/deploy/.ssh
chmod 600 /home/deploy/.ssh/authorized_keys
chown -R deploy:deploy /home/deploy/.ssh
```

Teste uma **nova sessão** antes de endurecer o SSH:

```bash
ssh deploy@145.223.92.36
```

Somente depois de confirmar login por chave, é recomendado desativar login remoto de root e autenticação SSH por senha:

```bash
sudo tee /etc/ssh/sshd_config.d/99-hardening.conf >/dev/null <<'EOF'
PermitRootLogin no
PasswordAuthentication no
PubkeyAuthentication yes
EOF

sudo sshd -t
sudo systemctl reload ssh
```

Não feche a sessão atual antes de confirmar uma segunda sessão funcional com `deploy`.

A senha local de `deploy` pode continuar existindo para `sudo`, desde que seja forte, única e armazenada no password manager.

---

## 5. Instalação do Docker

Instalar Docker Engine pelo repositório oficial.

```bash
sudo apt update
sudo apt install -y ca-certificates curl

sudo install -m 0755 -d /etc/apt/keyrings

sudo curl -fsSL https://download.docker.com/linux/ubuntu/gpg \
  -o /etc/apt/keyrings/docker.asc

sudo chmod a+r /etc/apt/keyrings/docker.asc
```

Adicionar o repositório usando o codename real da instalação:

```bash
. /etc/os-release

sudo tee /etc/apt/sources.list.d/docker.sources >/dev/null <<EOF
Types: deb
URIs: https://download.docker.com/linux/ubuntu
Suites: ${VERSION_CODENAME}
Components: stable
Architectures: $(dpkg --print-architecture)
Signed-By: /etc/apt/keyrings/docker.asc
EOF
```

Instalar:

```bash
sudo apt update

sudo apt install -y \
  docker-ce \
  docker-ce-cli \
  containerd.io \
  docker-buildx-plugin \
  docker-compose-plugin

sudo systemctl enable --now docker
sudo docker run --rm hello-world
```

Verificar:

```bash
docker --version
docker compose version
```

Se o usuário `deploy` acabou de ser adicionado ao grupo `docker`, saia e entre novamente no SSH antes de testar Docker sem `sudo`.

---

## 6. Acesso aos repositórios

Layout final:

```text
/opt/apps/app/
├── .env.prod
├── compose.prod.yaml
├── podiss-backend/
└── podiss-frontend/
```

Os repositórios são:

```text
Code-EJ/podiss-backend
Code-EJ/podiss-frontend
```

Se forem públicos, HTTPS é suficiente.

Se forem privados, use uma Deploy Key diferente para cada repositório. Uma Deploy Key do GitHub é vinculada ao repositório e não deve ser reutilizada em vários repositórios.

Exemplo:

```bash
ssh-keygen -t ed25519 -f ~/.ssh/frontend_deploy -C "frontend@podiss-vps"
ssh-keygen -t ed25519 -f ~/.ssh/backend_deploy -C "backend@podiss-vps"
```

Cadastrar somente os arquivos `.pub` em GitHub → Repository → Settings → Deploy keys. Não habilitar write access para a VPS.

`~/.ssh/config`:

```text
Host github-frontend
    HostName github.com
    User git
    IdentityFile ~/.ssh/frontend_deploy
    IdentitiesOnly yes

Host github-backend
    HostName github.com
    User git
    IdentityFile ~/.ssh/backend_deploy
    IdentitiesOnly yes
```

Permissões:

```bash
chmod 700 ~/.ssh
chmod 600 ~/.ssh/config
chmod 600 ~/.ssh/frontend_deploy
chmod 600 ~/.ssh/backend_deploy
```

Teste:

```bash
ssh -T git@github-frontend
ssh -T git@github-backend
```

Clone:

```bash
cd /opt/apps/app

git clone git@github-frontend:Code-EJ/podiss-frontend.git
git clone git@github-backend:Code-EJ/podiss-backend.git
```

Nunca copie a chave privada para o repositório.

---

## 7. Segredos de produção

Criar os segredos fora dos repositórios:

```bash
cd /opt/apps/app
umask 077

cat > .env.prod <<EOF
DB_PASSWORD=$(openssl rand -hex 32)
DB_ROOT_PASSWORD=$(openssl rand -hex 32)
JWT_SECRET=$(openssl rand -base64 64 | tr -d '\n')

CORS_ALLOWED_ORIGINS=https://podiss.com.br,https://www.podiss.com.br

ADMIN_USERNAME=
ADMIN_EMAIL=
ADMIN_PASSWORD=

YOUTUBE_API_KEY=
EOF

chmod 600 .env.prod
```

Não usar `cat .env.prod` em logs, tickets, commits ou mensagens.

As credenciais ADMIN são usadas somente para o bootstrap inicial. Ver seção 10.

---

## 8. Compose de produção

Criar:

```bash
nano /opt/apps/app/compose.prod.yaml
```

Conteúdo:

```yaml
name: podiss-prod

services:
  db:
    image: mariadb:11.8.9
    restart: unless-stopped
    environment:
      MARIADB_DATABASE: podiss
      MARIADB_USER: podiss
      MARIADB_PASSWORD: ${DB_PASSWORD:?DB_PASSWORD is required}
      MARIADB_ROOT_PASSWORD: ${DB_ROOT_PASSWORD:?DB_ROOT_PASSWORD is required}
    volumes:
      - database:/var/lib/mysql
    healthcheck:
      test: ["CMD", "healthcheck.sh", "--connect", "--innodb_initialized"]
      interval: 5s
      timeout: 5s
      retries: 20
      start_period: 30s

  migrate:
    image: redgate/flyway:10.20.1
    environment:
      FLYWAY_URL: jdbc:mariadb://db:3306/podiss
      FLYWAY_USER: podiss
      FLYWAY_PASSWORD: ${DB_PASSWORD:?DB_PASSWORD is required}
      FLYWAY_LOCATIONS: filesystem:/flyway/sql
      FLYWAY_BASELINE_ON_MIGRATE: "false"
      FLYWAY_CLEAN_DISABLED: "true"
      FLYWAY_CONNECT_RETRIES: "10"
      FLYWAY_FAIL_ON_MISSING_LOCATIONS: "true"
      FLYWAY_VALIDATE_MIGRATION_NAMING: "true"
    volumes:
      - ./podiss-backend/src/main/resources/db/fresh-migration:/flyway/sql:ro
    depends_on:
      db:
        condition: service_healthy
    command: migrate
    restart: "no"

  app:
    image: podiss-backend:prod
    build:
      context: ./podiss-backend
    restart: unless-stopped
    environment:
      SPRING_PROFILES_ACTIVE: prod
      SPRING_FLYWAY_ENABLED: "false"
      SPRING_JPA_HIBERNATE_DDL_AUTO: validate

      DB_URL: jdbc:mariadb://db:3306/podiss
      DB_USERNAME: podiss
      DB_PASSWORD: ${DB_PASSWORD:?DB_PASSWORD is required}

      JWT_SECRET: ${JWT_SECRET:?JWT_SECRET is required}
      CORS_ALLOWED_ORIGINS: ${CORS_ALLOWED_ORIGINS:?CORS_ALLOWED_ORIGINS is required}

      ADMIN_USERNAME: ${ADMIN_USERNAME:-}
      ADMIN_EMAIL: ${ADMIN_EMAIL:-}
      ADMIN_PASSWORD: ${ADMIN_PASSWORD:-}

      YOUTUBE_API_KEY: ${YOUTUBE_API_KEY:-}

      HIKARI_MAX_POOL_SIZE: "5"
      HIKARI_MIN_IDLE: "1"

      SSL_ENABLED: "false"

    ports:
      - "127.0.0.1:18080:8080"

    depends_on:
      db:
        condition: service_healthy
      migrate:
        condition: service_completed_successfully

    read_only: true

    tmpfs:
      - /tmp:rw,noexec,nosuid,size=128m

    cap_drop:
      - ALL

    security_opt:
      - no-new-privileges:true

    stop_grace_period: 30s

volumes:
  database:
```

Características de segurança importantes:

- banco sem `ports:`;
- backend publicado somente em `127.0.0.1:18080`;
- Spring com filesystem read-only;
- capabilities removidas;
- `no-new-privileges`;
- Flyway separado da aplicação;
- Hibernate em `validate`;
- migrations automáticas do Spring desabilitadas.

Validar sem imprimir a configuração expandida:

```bash
cd /opt/apps/app

docker compose \
  --env-file .env.prod \
  -f compose.prod.yaml \
  config --quiet
```

---

## 9. Primeiro startup do backend e banco

### Importante sobre o banco

A cadeia `src/main/resources/db/fresh-migration` foi criada para uma base nova e dedicada.

Não use esta instalação limpa como tentativa de upgrade de um banco legado sem uma estratégia de migração explicitamente revisada.

Subir a stack:

```bash
cd /opt/apps/app

docker compose \
  --env-file .env.prod \
  -f compose.prod.yaml \
  up -d --build
```

Verificar:

```bash
docker compose \
  --env-file .env.prod \
  -f compose.prod.yaml \
  ps -a
```

Estado esperado:

```text
podiss-prod-db-1        Up (...) (healthy)
podiss-prod-migrate-1   Exited (0)
podiss-prod-app-1       Up (...) (healthy)
```

`migrate = Exited (0)` é o comportamento esperado: o container aplica/valida migrations e encerra.

Smoke direto no backend:

```bash
curl -i 'http://127.0.0.1:18080/episodes?size=1'
```

Esperado: resposta HTTP bem-sucedida.

Logs:

```bash
docker logs --tail=100 podiss-prod-app-1
docker logs --tail=100 podiss-prod-migrate-1
```

Não publicar logs sem revisar a presença de dados sensíveis.

---

## 10. Bootstrap do primeiro administrador

O administrador inicial é opcional. O backend só cria o primeiro ADMIN quando as três variáveis estão presentes:

```text
ADMIN_USERNAME
ADMIN_EMAIL
ADMIN_PASSWORD
```

Regras atuais:

- username: somente letras, números, ponto, underscore e hífen;
- username: máximo 100 caracteres;
- email: formato válido;
- senha: 12 a 72 caracteres;
- senha: no máximo 72 bytes UTF-8;
- as três variáveis devem ser preenchidas juntas;
- se já existe um ADMIN, o bootstrap não altera nada;
- o bootstrap não eleva ou sobrescreve uma conta existente silenciosamente.

Editar temporariamente:

```bash
cd /opt/apps/app
nano .env.prod
```

Exemplo **somente com placeholders**:

```dotenv
ADMIN_USERNAME=<usuario_admin>
ADMIN_EMAIL=<email_admin>
ADMIN_PASSWORD=<senha_admin_forte>
```

Recriar somente a aplicação:

```bash
docker compose \
  --env-file .env.prod \
  -f compose.prod.yaml \
  up -d --no-deps --force-recreate app
```

Verificar:

```bash
docker ps
docker logs --tail=100 podiss-prod-app-1
```

Consultar usuários sem imprimir hashes:

```bash
docker compose \
  --env-file .env.prod \
  -f compose.prod.yaml \
  exec db sh -lc \
  'mariadb -u"$MARIADB_USER" -p"$MARIADB_PASSWORD" "$MARIADB_DATABASE" -e "SELECT username,email,role FROM users;"'
```

Após confirmar o login, remover os valores plaintext de bootstrap:

```dotenv
ADMIN_USERNAME=
ADMIN_EMAIL=
ADMIN_PASSWORD=
```

Recriar novamente:

```bash
docker compose \
  --env-file .env.prod \
  -f compose.prod.yaml \
  up -d --no-deps --force-recreate app
```

A conta permanece no MariaDB e a senha persiste somente como hash BCrypt.

Login:

```text
https://podiss.com.br/admin/login
```

### Troubleshooting: container reiniciando após preencher ADMIN_*

Sintoma:

```text
Application run failed
IllegalStateException: Configure ADMIN_USERNAME, ADMIN_EMAIL e ADMIN_PASSWORD (12 a 72 caracteres).
```

Verificar:

```bash
docker inspect \
  -f 'Status={{.State.Status}} ExitCode={{.State.ExitCode}} RestartCount={{.RestartCount}}' \
  podiss-prod-app-1

docker logs --tail=150 podiss-prod-app-1
```

Corrigir as três variáveis e recriar `app`.

---

## 11. Build do frontend

O frontend exige a URL absoluta da API no momento do build.

Criar:

```bash
cd /opt/apps/app/podiss-frontend

cat > .env.production <<'EOF'
VITE_API_URL=https://api.podiss.com.br
EOF
```

`.env.production` contém apenas configuração pública de build; nenhuma credencial de banco deve existir no frontend.

Build sem instalar Node diretamente no host:

```bash
docker run --rm \
  --user "$(id -u):$(id -g)" \
  -e HOME=/tmp \
  -v "$PWD:/app" \
  -w /app \
  node:22-alpine \
  sh -lc 'npm ci && npm run build'
```

O uso de `--user` evita gerar `dist`/`node_modules` com owner root.

Verificar:

```bash
ls -la dist
stat dist/index.html
```

Esperado:

```text
dist/
├── index.html
└── assets/
```

---

## 12. Publicação do frontend no Nginx

Criar o document root:

```bash
sudo mkdir -p /var/www/podiss
```

Publicar o build:

```bash
sudo find /var/www/podiss -mindepth 1 -delete
sudo cp -a dist/. /var/www/podiss/
```

Confirmar que o build e o publicado são iguais:

```bash
sha256sum dist/index.html
sha256sum /var/www/podiss/index.html
```

Os hashes devem coincidir.

---

## 13. Configuração do Nginx

Criar:

```bash
sudo nano /etc/nginx/sites-available/podiss
```

Configuração:

```nginx
server {
    listen 80;
    listen [::]:80;

    server_name podiss.com.br www.podiss.com.br;

    root /var/www/podiss;
    index index.html;

    location / {
        try_files $uri $uri/ /index.html;
    }
}

server {
    listen 80;
    listen [::]:80;

    server_name api.podiss.com.br;

    client_max_body_size 20m;

    location / {
        proxy_pass http://127.0.0.1:18080;

        proxy_http_version 1.1;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
}
```

Habilitar:

```bash
sudo ln -sfn /etc/nginx/sites-available/podiss /etc/nginx/sites-enabled/podiss
sudo rm -f /etc/nginx/sites-enabled/default

sudo nginx -t
sudo systemctl reload nginx
```

Teste antes do DNS:

```bash
curl -I -H 'Host: podiss.com.br' http://127.0.0.1

curl -i \
  -H 'Host: api.podiss.com.br' \
  'http://127.0.0.1/episodes?size=1'
```

---

## 14. DNS na Hostinger

Registros desejados:

```text
A    @      145.223.92.36
A    www    145.223.92.36
A    api    145.223.92.36
```

Durante a migração foram removidos os apontamentos antigos de hosting/CDN para `@` e `www`.

Não remover registros de e-mail como:

- MX;
- SPF;
- DKIM;
- DMARC;
- autodiscover/autoconfig;

a menos que exista uma mudança planejada do serviço de e-mail.

Verificar de uma máquina externa:

```bash
nslookup podiss.com.br
nslookup www.podiss.com.br
nslookup api.podiss.com.br
```

Os três devem resolver para `145.223.92.36`.

---

## 15. HTTPS com Let's Encrypt

Instalar:

```bash
sudo apt update
sudo apt install -y certbot python3-certbot-nginx
```

Emitir:

```bash
sudo certbot --nginx \
  -d podiss.com.br \
  -d www.podiss.com.br \
  -d api.podiss.com.br
```

Manter redirecionamento HTTP -> HTTPS.

Validar:

```bash
curl -I https://podiss.com.br
curl -I https://www.podiss.com.br
curl -i 'https://api.podiss.com.br/episodes?size=1'
```

Testar renovação:

```bash
sudo certbot renew --dry-run
```

---

## 16. Validação pós-deploy

Verificar containers:

```bash
cd /opt/apps/app

docker compose \
  --env-file .env.prod \
  -f compose.prod.yaml \
  ps -a
```

Verificar Nginx:

```bash
sudo nginx -t
sudo systemctl is-active nginx
```

Smoke externo:

```bash
curl -I https://podiss.com.br
curl -i 'https://api.podiss.com.br/episodes?size=1'
```

Validar no navegador:

- página inicial;
- refresh em rota profunda da SPA;
- posts;
- episódios;
- login ADMIN;
- criação/edição administrativa quando autorizada;
- envio de contato/sugestão;
- ausência de CORS/mixed content/404 de assets.

---

## 17. Diagnóstico de frontend antigo/cache

Confirmar commit local:

```bash
cd /opt/apps/app/podiss-frontend

git fetch origin
git rev-parse HEAD
git rev-parse origin/main
git log -1 --oneline
```

Confirmar artefato:

```bash
sha256sum dist/index.html
sha256sum /var/www/podiss/index.html
curl -sS https://podiss.com.br/ | sha256sum
```

Para ignorar DNS e forçar a VPS:

```bash
curl -sS \
  --resolve podiss.com.br:443:145.223.92.36 \
  https://podiss.com.br/ | sha256sum
```

Interpretação:

```text
dist == /var/www == domínio
=> servidor está entregando o build atual.

dist == /var/www != domínio
=> investigar DNS/cache/proxy externo.

dist != /var/www
=> build atual ainda não foi publicado.
```

No navegador, se necessário, usar hard refresh ou desabilitar cache no DevTools.

---

## 18. Deploy rotineiro — frontend

Fluxo:

```text
alteração local
-> PR/revisão
-> merge em main
-> git pull na VPS
-> npm build
-> publicar dist
```

Na VPS:

```bash
cd /opt/apps/app/podiss-frontend

git status --short
git pull --ff-only origin main
git log -1 --oneline

docker run --rm \
  --user "$(id -u):$(id -g)" \
  -e HOME=/tmp \
  -v "$PWD:/app" \
  -w /app \
  node:22-alpine \
  sh -lc 'npm ci && npm run build'

sudo find /var/www/podiss -mindepth 1 -delete
sudo cp -a dist/. /var/www/podiss/

curl -I https://podiss.com.br
```

Não é necessário reiniciar Nginx para substituir somente arquivos estáticos.

Não faça modificações manuais de código diretamente na VPS.

---

## 19. Deploy rotineiro — backend sem migration nova

```bash
cd /opt/apps/app/podiss-backend

git status --short
git pull --ff-only origin main
git log -1 --oneline

cd /opt/apps/app

docker compose \
  --env-file .env.prod \
  -f compose.prod.yaml \
  up -d --build --no-deps app
```

Validar:

```bash
docker ps
docker logs --tail=100 podiss-prod-app-1
curl -i 'https://api.podiss.com.br/episodes?size=1'
```

---

## 20. Deploy do backend com migration nova

Nunca altere o conteúdo de uma migration já aplicada em produção. Adicione uma nova versão.

Antes de aplicar, faça backup e revise o SQL.

Atualizar código:

```bash
cd /opt/apps/app/podiss-backend
git pull --ff-only origin main
```

Aplicar Flyway:

```bash
cd /opt/apps/app

docker compose \
  --env-file .env.prod \
  -f compose.prod.yaml \
  run --rm migrate
```

Somente após migration bem-sucedida:

```bash
docker compose \
  --env-file .env.prod \
  -f compose.prod.yaml \
  up -d --build --no-deps app
```

Validar logs e smoke.

Não executar automaticamente `flyway clean`, `repair` ou `baseline` para contornar falha.

---

## 21. Backup do MariaDB

O volume Docker é persistente, mas **volume não é backup**.

Criar diretório restrito:

```bash
sudo mkdir -p /opt/backups/podiss
sudo chown deploy:deploy /opt/backups/podiss
chmod 700 /opt/backups/podiss
```

Gerar dump sem colocar a senha na linha de comando:

```bash
cd /opt/apps/app

docker compose \
  --env-file .env.prod \
  -f compose.prod.yaml \
  exec -T db sh -lc \
  'mariadb-dump -u"$MARIADB_USER" -p"$MARIADB_PASSWORD" --single-transaction --routines --triggers "$MARIADB_DATABASE"' \
  | gzip > "/opt/backups/podiss/podiss-$(date +%Y%m%d-%H%M%S).sql.gz"

chmod 600 /opt/backups/podiss/*.sql.gz
```

Conferir:

```bash
ls -lh /opt/backups/podiss
sha256sum /opt/backups/podiss/*.sql.gz
```

Um backup só deve ser considerado recuperável depois de um restore de teste em banco isolado.

Não commitar dumps.

Também avaliar snapshots/backups da própria VPS Hostinger, mas eles não substituem o dump lógico do banco.

---

## 22. Reboot / recuperação da VPS

A stack foi configurada com `restart: unless-stopped` para `db` e `app`.

Após manutenção/reboot:

```bash
sudo reboot
```

Depois de reconectar:

```bash
sudo systemctl is-active docker
sudo systemctl is-active nginx

docker ps -a

curl -I https://podiss.com.br
curl -i 'https://api.podiss.com.br/episodes?size=1'
```

O container `migrate` permanecer como `Exited (0)`. Isso é esperado.

---

## 23. Rollback

### Frontend

Identifique o commit/tag anterior conhecido como bom:

```bash
cd /opt/apps/app/podiss-frontend
git log --oneline --decorate -10
```

Faça checkout controlado do release anterior, gere novo `dist` e republique.

Depois de recuperar, retorne o checkout para `main` antes do próximo deploy.

### Backend

Rollback de binário só é seguro se o schema atual continuar compatível com a versão anterior.

Procedimento geral:

1. identificar o último commit/tag bom;
2. confirmar compatibilidade com as migrations já aplicadas;
3. fazer backup;
4. reconstruir a imagem da versão anterior;
5. recriar somente `app`;
6. executar smoke tests.

Não reverta banco automaticamente apenas porque o binário falhou.

Migrations de produção devem ser tratadas como append-only.

---

## 24. Comandos rápidos de diagnóstico

Containers:

```bash
docker ps -a
```

Logs backend:

```bash
docker logs --tail=150 podiss-prod-app-1
```

Logs Flyway:

```bash
docker logs --tail=150 podiss-prod-migrate-1
```

Portas:

```bash
sudo ss -lntp
```

Firewall:

```bash
sudo ufw status verbose
```

Nginx:

```bash
sudo nginx -t
sudo systemctl status nginx --no-pager
```

Certificados:

```bash
sudo certbot certificates
sudo certbot renew --dry-run
```

Disco:

```bash
df -h
docker system df
```

Não use `docker system prune --volumes` em produção sem compreender exatamente quais volumes serão removidos.

---

## 25. Fluxo de responsabilidades

### GitHub

Fonte de verdade do código:

```text
desenvolvimento
-> commit
-> PR
-> revisão
-> merge em main
```

### VPS

Somente execução/deploy:

```text
git pull
-> build
-> migration, quando aplicável
-> publicação/recriação
-> smoke
```

Evite commits feitos diretamente na VPS.

### Banco

```text
Flyway
-> altera schema

Spring/Hibernate
-> valida schema

MariaDB volume
-> persiste dados
```

---

## 26. Pendências recomendadas

Melhorias posteriores, sem alterar o funcionamento atual:

- automatizar backup do MariaDB com retenção e restore testado;
- monitorar espaço, RAM, disponibilidade HTTP e validade TLS;
- configurar GitHub Actions com environment de produção e aprovação manual;
- construir artefatos/imagens no CI e promover o mesmo digest para produção;
- substituir tags flutuantes por versões/digests imutáveis onde fizer sentido;
- criar estratégia de releases atômicos para o frontend;
- documentar RPO/RTO e processo de desastre;
- revisar periodicamente usuários SSH e Deploy Keys;
- manter fail2ban, sistema, Docker e Nginx atualizados;
- revisar os documentos preliminares antigos de deployment agora que a topologia real foi confirmada.

---

## 27. Checklist de reconstrução completa

- [ ] VPS criada e sistema identificado.
- [ ] DNS/IPv4 conhecidos.
- [ ] firewall Hostinger com 22/80/443.
- [ ] UFW configurado.
- [ ] usuário `deploy` criado.
- [ ] SSH por chave validado.
- [ ] Docker + Compose instalados.
- [ ] Nginx instalado.
- [ ] repositórios clonados.
- [ ] `.env.prod` criado com modo 600.
- [ ] `compose.prod.yaml` criado.
- [ ] MariaDB healthy.
- [ ] Flyway `Exited (0)`.
- [ ] Spring healthy em `127.0.0.1:18080`.
- [ ] frontend compilado com `VITE_API_URL=https://api.podiss.com.br`.
- [ ] `dist` publicado em `/var/www/podiss`.
- [ ] Nginx validado com `nginx -t`.
- [ ] A records `@`, `www` e `api` apontando para a VPS.
- [ ] Certbot concluído.
- [ ] `certbot renew --dry-run` aprovado.
- [ ] login ADMIN testado.
- [ ] variáveis ADMIN plaintext removidas após bootstrap.
- [ ] backup lógico criado e restore de teste planejado.
- [ ] smoke de frontend e API aprovado.

---

## Referências internas

- [Configuração de ambiente](../../../configuration/environment.md)
- [Acesso à VPS](../../vps-access.md)
- [Runbook preliminar](../../runbook.md)
- [ADR-0005 — deployment na VPS](../../../ADRS/0005-vps-deployment.md)
- [ADR-0003 — evolução do banco](../../../ADRS/0003-database-schema-evolution.md)

Este documento descreve a topologia operacional atual. Mudanças em DNS, portas, estratégia de banco, proxy, CI/CD ou runtime devem atualizar este runbook e, quando arquiteturalmente relevantes, uma ADR.
