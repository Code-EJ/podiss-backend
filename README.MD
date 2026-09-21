# PodIss Backend

Backend Spring Boot para o PodIss. Créditos: **oEnzoRibas**.

Java 21, Spring Boot 3.4.1, MariaDB 11.8.9 nos testes/ambiente local e Flyway 10.20.1. Instalação limpa em inglês; contratos HTTP legados preservados.

## Comece aqui

1. [Tutorial passo a passo: desenvolvimento com Docker](docs/development/local-environment.md).
2. [Entenda .env, .env.dev e configuração SSH](docs/configuration/environment.md).
3. [Configure seu acesso local à VPS e faça diagnóstico somente leitura](docs/deployment/vps-access.md).
4. [Runbook de deploy — pendências explícitas antes de produção](docs/deployment/runbook.md).

**Docker local:** use .env.dev, não .env. Nesta máquina ele já foi gerado com credenciais exclusivas locais. Em clone novo, copie .env.dev.example e preencha seguindo o tutorial.

```powershell
.\scripts\dev.ps1 Up
.\scripts\dev.ps1 Status
.\scripts\dev.ps1 Validate
```

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

VPS Ubuntu 24.04 confirmada pelo responsável. O backend/processo/proxy e a localização do banco ainda precisam ser inventariados antes do deploy. Estar na Hostinger não significa que frontend, banco e Java estejam no mesmo serviço.

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
