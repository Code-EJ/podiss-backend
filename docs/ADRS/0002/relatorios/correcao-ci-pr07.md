# Correções do CI — PR #7

Créditos: oEnzoRibas. Referência: https://github.com/Code-EJ/podiss-backend/pull/7.

## Causas observadas

1. Execução 35809653897: o wrapper foi chamado como `bash mvnw`. A resolução `${0%/*}` gerou `mvnw/.mvn/wrapper/maven-wrapper.properties`, falhando antes do Maven. O commit 13f6f87 usa `bash ./mvnw`.
2. Execução 35810083632: após resolver o wrapper, a compilação falhou em `JwtTokenService.parserBuilder()`. O merge da main trouxe JJWT 0.12.6, incompatível com essa chamada antiga.

## Correção de JWT

Adotar SecretKey, `Jwts.parser().verifyWith(...)`, `parseSignedClaims().getPayload()` e o builder atual com `Jwts.SIG.HS512`. Manter assinatura HS512, issuer, subject, expiração e validação de segredo; não reduzir a versão da dependência nem relaxar autorização.

Teste adicional verifica algoritmo HS512 e rejeição de tokens sem expiração, sem subject e expirados. Os testes existentes continuam verificando issuer e chave incorretos.

Referência da API: https://github.com/jwtk/jjwt/blob/0.12.6/README.adoc.

## Validação e escopo

Executar a mesma sequência Maven do CI com Java 21, H2 isolado e MariaDB descartável via Testcontainers, seguida do workflow remoto completo (testes e build Docker). A aprovação deve ser conferida na execução do commit correspondente, não inferida de uma compilação isolada.

Nenhum teste foi desabilitado. Não houve alteração em banco produtivo, deploy, secrets, configuração de TLS ou migrations.

Validação local concluída em Java 21.0.9: `clean verify` com BUILD SUCCESS, 18 testes unitários/contratos e 13 de integração MariaDB, sem falhas, erros ou skips. O resultado remoto deve ser acompanhado nos checks do PR.
