# Qual arquivo de configuração usar?

Créditos: **oEnzoRibas**.

| Arquivo | Uso | Quem carrega | Vai para Git? |
| --- | --- | --- | --- |
| .env | Spring executado diretamente na IDE | EnvFile configurado em .run; não é leitura automática do Spring | Não |
| .env.example | Modelo para a .env | Desenvolvedor copia/preenche | Sim |
| .env.dev | Docker Compose local | scripts/dev.ps1 ou --env-file .env.dev | Não |
| .env.dev.example | Modelo do Docker local | Desenvolvedor copia/preenche | Sim |
| .local/vps.ssh.config | Host/usuário/porta/caminho de chave SSH | ssh -F .local/vps.ssh.config | Não |
| deploy/vps.ssh.config.example | Modelo SSH sem endereço/usuário real | Desenvolvedor copia/preenche | Sim |
| application*.yaml | Defaults e leitura de variáveis da aplicação | Spring Boot | Sim, sem secrets |
| Configuração de produção | Secrets e datasource do runtime real | Mecanismo da VPS ainda a confirmar | Nunca no Git |

**Não precisa usar os dois .env ao mesmo tempo.** Docker local usa .env.dev. IDE usa .env. Eles não devem conter as mesmas credenciais de produção. A .env existente não foi sobrescrita.

O nome .env.dev não tem significado especial para Spring. compose.dev.yaml injeta explicitamente SPRING_PROFILES_ACTIVE=dev e converte DEV_DB_PASSWORD → DB_PASSWORD, DEV_JWT_SECRET → JWT_SECRET etc. DB_URL dentro do Docker é fixo para o serviço db e banco podiss_dev; variáveis DB_URL da sua .env normal não são encaminhadas ao contêiner.

O script sempre passa --env-file .env.dev e não imprime a configuração expandida. Variáveis do próprio terminal podem prevalecer sobre o arquivo no Compose: não exportar valores DEV_* de outro ambiente.

Não use .env.dev na VPS. Não guarde senha SSH em .env; ela é solicitada interativamente, ou use uma chave protegida fora do repositório.

## Variáveis consumidas pelo Spring

Os exemplos reais de formato estão em .env.example, sempre sem valores secretos.

| Variável | Finalidade / padrão | Obrigatoriedade e sensibilidade |
| --- | --- | --- |
| DB_URL | URL JDBC MariaDB; sem default | Obrigatória; endereço do banco; TLS conforme topologia |
| DB_USERNAME | Usuário do banco; sem default | Obrigatória; informação restrita |
| DB_PASSWORD | Senha; sem default | Obrigatória e secreta |
| JWT_SECRET | Base64 de >=64 bytes aleatórios; sem default | Obrigatória e secreta; curta/inválida impede startup |
| CORS_ALLOWED_ORIGINS | Lista por vírgula; http://localhost:5173 | Configurar frontend real em prod; não é secret |
| ADMIN_USERNAME | Bootstrap; vazio | Opcional em conjunto com email/senha |
| ADMIN_EMAIL | Bootstrap; vazio | Dado pessoal; trio completo e válido |
| ADMIN_PASSWORD | Bootstrap; vazio | Secret; trio completo; 12..72 caracteres e <=72 bytes UTF-8 |
| YOUTUBE_API_KEY | API YouTube; vazio usa oEmbed | Secret opcional; comportamento limitado sem chave |
| HIKARI_MAX_LIFETIME | 1800000 ms | Opcional; duração de conexão |
| HIKARI_MAX_POOL_SIZE | 10 | Opcional; limite de conexões |
| HIKARI_MIN_IDLE | 5 | Opcional; conexões ociosas mínimas |
| SSL_ENABLED | false no profile prod | Opcional; TLS direto no Spring |
| SSL_KEY_STORE | vazio no profile prod | Necessário se SSL habilitado; caminho de arquivo externo |
| SSL_KEY_STORE_PASSWORD | vazio no profile prod | Secret necessário conforme keystore |
| SSL_KEY_ALIAS | server no profile prod | Alias correspondente ao certificado |
| SPRING_PROFILES_ACTIVE | sem profile forçado na base | Variável padrão Spring; dev local ou prod no servidor |

DB/JWT/CORS/bootstrap/YouTube/pool são lidos na configuração base, herdada por dev/prod. SSL_* está em application-prod.yaml. SPRING_FLYWAY_ENABLED=false e SPRING_JPA_HIBERNATE_DDL_AUTO=validate podem reforçar a política no runtime, mas não substituem revisão da configuração efetiva.

O bootstrap valida o trio antes de verificar administrador existente: para desativá-lo depois, remova **as três** variáveis. Não basta apagar só a senha. Nunca usar usuário/senha de exemplo em produção.

## Variáveis exclusivas do Compose local

DEV_DB_PASSWORD, DEV_DB_ROOT_PASSWORD, DEV_JWT_SECRET, DEV_ADMIN_PASSWORD são obrigatórias e secretas. DEV_HTTP_PORT tem default 18080; DEV_CORS_ALLOWED_ORIGINS tem default http://localhost:5173. Banco/usuário podiss_dev e ADMIN dev_admin são nomes locais deliberados.

FLYWAY_URL/USER/PASSWORD/LOCATIONS e proteções da CLI são montados pelo Compose a partir do ambiente local; não precisam estar na .env.dev. A CLI externa usa variáveis FLYWAY_*, não DB_* automaticamente.

O arquivo .local/vps.ssh.config não é environment de aplicação. Configure nele somente a conexão SSH, seguindo [acesso à VPS](../deployment/vps-access.md).

## Precedência e proteção

A .run existente continua ativando dev e lendo .env. Isso não garante isolamento se o arquivo apontar para produção: confira sempre o alvo. JVM/Spring sem EnvFile não carregam .env por convenção.

Git ignora .env, .env.*, .local, com exceções somente para os dois modelos. .dockerignore usa allowlist de fontes/POM e exclui keystores/chaves. Antes do commit, revisar arquivos staged e rodar git check-ignore nos arquivos reais. Não compartilhar compose config, inspect completo ou prints com secrets.
