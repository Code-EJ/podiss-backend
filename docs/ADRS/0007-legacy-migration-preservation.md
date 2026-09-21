# ADR 0007 — preservar a V1 histórica

Status: Accepted. Data: 2026-09-21. Créditos: oEnzoRibas.

## Decisão

Restaurar src/main/resources/db/migration/V1__database.sql exatamente ao conteúdo de HEAD, sem publicar a edição local. O responsável autorizou decidir o destino dessa pendência. Nenhum SQL será executado.

A edição pendente retirava IF NOT EXISTS e DEFAULT RANDOM_UUID(), mudava ordem de tabelas, tags para VARCHAR(2048) e timestamps para TIMESTAMP(6) NOT NULL. Seu comentário orientava baseline em 1. Essa proposta foi superada pela cadeia independente db/fresh-migration, já testada com cinco tabelas e Hibernate validate.

Não sabemos se a V1 histórica foi aplicada em algum ambiente. Alterá-la pode invalidar checksums; publicá-la também manteria instruções de baseline incompatíveis com a decisão atual. Preservar o original não torna seu SQL recomendado para MariaDB: ele é somente histórico, fora das locations ativas.

## Registro recuperável da edição abandonada

O conteúdo abaixo foi preservado antes da restauração, como documentação, não como migration executável:

```sql
-- Créditos: oEnzoRibas
-- Fresh installs only. Existing Hibernate-managed schemas are explicitly baselined at 1.
-- Native UUID preserves the mapping used by Hibernate on MariaDB 10.7+.
CREATE TABLE users (
    id UUID PRIMARY KEY,
    username VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE
);
CREATE TABLE posts (
    id UUID PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description TEXT NOT NULL,
    tags VARCHAR(2048),
    image LONGBLOB,
    created_at TIMESTAMP(6) NOT NULL
);
CREATE TABLE episodes (
    id UUID PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description TEXT NOT NULL,
    video_url VARCHAR(255) NOT NULL,
    created_at TIMESTAMP(6) NOT NULL
);
```

## Consequências e verificação

Nenhuma mudança no schema ativo, banco local ou Hostinger. A nova V1 mantém seu hash da etapa 10. Verificar diff vazio para a V1 histórica, locations no YAML e status Git. A pendência registrada na etapa 10 fica resolvida por esta ADR.
