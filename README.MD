# PodIss Backend

Backend Spring Boot para o PodIss. Créditos: **oEnzoRibas**.

Java 21, Spring Boot 3.4.1, MariaDB 11.8.9 nos testes/ambiente local e Flyway 10.20.1. Instalação limpa em inglês; contratos HTTP legados preservados.

## Comece aqui

1. [Tutorial passo a passo: iniciar no Git Bash/PowerShell, testar a API e conferir o banco](docs/development/local-environment.md).
2. [Entenda .env, .env.dev e configuração SSH](docs/configuration/environment.md).
3. [Configure seu acesso local à VPS](docs/deployment/vps-access.md).
4. [Runbook de produção — Hostinger VPS](docs/deployment/hostinger/vps/README.md).
5. [Runbook preliminar/histórico de deploy](docs/deployment/runbook.md).
6. [API e Swagger local](docs/api/README.md).
7. [Fechamento das etapas 01–10](docs/ADRS/0002/relatorios/etapa10-auditoria-final.md).

**Docker local:** use .env.dev, não .env. Nesta máquina ele já foi gerado com credenciais exclusivas locais. Em clone novo, copie .env.dev.example e preencha seguindo o tutorial.

### Iniciar pelo PowerShell

Abra o Docker Desktop, espere o engine Linux iniciar e execute na raiz do projeto:

```powershell
.\scripts\dev.ps1 Up
.\scripts\dev.ps1 Status
.\scripts\dev.ps1 Validate
```

### Iniciar pelo Git Bash (MINGW64)

Não execute ./scripts/dev.ps1 diretamente no Bash: .ps1 é PowerShell e causa erro em [CmdletBinding()]. Use:

```bash
docker compose --env-file .env.dev -f compose.dev.yaml up -d --build --wait --wait-timeout 180
docker compose --env-file .env.dev -f compose.dev.yaml ps -a
docker compose --env-file .env.dev -f compose.dev.yaml run --rm migrate validate
```

Alternativa no Git Bash: powershell.exe -NoProfile -File ./scripts/dev.ps1 Up.

Espere app/db como healthy e migrate como Exited (0). Abra o [Swagger local](http://localhost:18080/swagger-ui/index.html). Para criar um contato pela API e verificar o registro com SELECT no MariaDB, siga a seção **3. Testar a API e conferir o banco** do [tutorial](docs/development/local-environment.md).

Aplicação em http://localhost:18080. Banco não exposto ao host. `Down` para parar preservando volume. Não copie o Compose de desenvolvimento para a VPS.

**IDE sem Docker:** .run usa EnvFile/.env e profile dev; precisa de MariaDB local previamente migrado. Spring não carrega .env sozinho. O fluxo Compose é o ponto de entrada recomendado para uma instalação isolada.

## Qual arquivo vai para o Git?

| Real, privado — não versionar | Modelo versionado |
| --- | --- |
| .env — IDE | .env.example |
| .env.dev — Docker local | .env.dev.example |
| .local/vps.ssh.config — SSH | deploy/vps.ssh.config.example |

Tutoriais, Compose, Dockerfile, scripts, testes e modelos ficam no repositório. Nunca versionar senhas, JWT real, chave privada ou volume do banco. Os modelos usam placeholders/vazios, não credenciais reais.

## Testes e build

Swagger local: http://localhost:18080/swagger-ui/index.html após rebuild. Desabilitado por padrão e em prod. Javadoc com Java 21: .\mvnw.cmd javadoc:javadoc. A geração em target pode emitir avisos de membros sem comentários.

Antes de produção, consulte os [riscos de segurança](docs/ADRS/0002/relatorios/etapa07-seguranca.md). Testes aprovados não liberam automaticamente deploy nem resolvem advisories.

`Dockerfile` compila e executa testes H2 em Java 21. A suíte completa Maven verify usa Testcontainers/MariaDB, exige Docker e não toca a Hostinger. Use o [comando isolado da etapa 04](docs/ADRS/0002/relatorios/etapa04-instalacao-limpa.md) com JDK 21.

Workflow de verificação: .github/workflows/verify.yml. Não há deploy/push de imagem automático.

## Banco e decisões

- Cadeia ativa: src/main/resources/db/fresh-migration.
- Banco vazio dedicado; não aplicar como upgrade de schema legado.
- Flyway externo e Hibernate validate; startup da aplicação não executa migrations.
- [ADR de evolução do banco](docs/ADRS/0003-database-schema-evolution.md).
- [ADR de desenvolvimento local](docs/ADRS/0004-local-development.md).
- [ADR de deploy na VPS — Proposed](docs/ADRS/0005-vps-deployment.md).
- [Plano mestre e etapas](docs/ADRS/0002/master-plan.md).
- [Relatório da etapa 04](docs/ADRS/0002/relatorios/etapa04-instalacao-limpa.md).

Produção atual: Hostinger VPS com Nginx no host, frontend estático em /var/www/podiss, backend Spring Boot em Docker publicado somente em 127.0.0.1:18080, MariaDB em Docker sem porta pública e Flyway executado separadamente. O procedimento completo de reconstrução, operação, atualização, backup e troubleshooting está no [runbook de produção da Hostinger VPS](docs/deployment/hostinger/vps/README.md).

## Contributing

This repository is maintained by Code Soluções em Tecnologia Júnior.

Access to the source code does not grant permission to copy, modify, distribute, sublicense, publish, or otherwise use the software outside the scope expressly authorized by Code Soluções em Tecnologia Júnior or the applicable project agreement.

Contributions must follow the project's development standards and may require prior authorization from the project maintainers.

## License

This project is proprietary software.

Unauthorized use, reproduction, distribution, or modification is prohibited.

See the `LICENSE` file for the applicable terms.

## Maintainers

### **Code Soluções em Tecnologia Júnior**

* Enzo Rocha Leite Diniz Ribas — Developer, Tech Lead, and Chief Information and Technology Officer.
