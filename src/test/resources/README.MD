# Recursos de teste

Créditos: oEnzoRibas.

O YAML de teste utiliza H2 em memória, Flyway desativado e Hibernate create-drop para os testes rápidos. Não comprova compatibilidade com MariaDB e não deve ser usado em produção.

A classe real `FreshSchemaIT` utiliza Testcontainers com MariaDB 11.8.9, habilita a nova cadeia `classpath:db/fresh-migration` e executa Hibernate validate. O datasource vem exclusivamente do contêiner descartável; não aceita credenciais da Hostinger.

- `test`: testes rápidos de contexto e contrato.
- `verify`: também executa FreshSchemaIT pelo Failsafe; Docker local precisa estar disponível.
- A integração não é ignorada silenciosamente quando Docker falha.
- O modo MariaDB do H2 não substitui o teste da migration SQL real.
- A configuração de integração prevalece sobre as propriedades H2 por DynamicPropertySource.

O comando completo, a opção de compatibilidade com Docker Engine 29 e as evidências estão no [relatório da etapa 04](../../../docs/ADRS/0002/relatorios/etapa04-instalacao-limpa.md). Use esse comando para fornecer explicitamente a configuração CORS requerida pelo contexto e isolar os testes da configuração externa.

Não há teste de upgrade do legado: a decisão aprovada é instalação limpa. Há teste de recusa de banco não vazio, preservando os dados existentes.
