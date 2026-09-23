Agora que nomenclatura, configurações e decisões arquiteturais foram estabilizadas, documente o código Java existente utilizando Javadoc profissional.

O objetivo é documentação de qualidade comparável a projetos open source maduros.

NÃO altere comportamento.

NÃO faça refatorações arquiteturais nesta etapa.

NÃO altere contratos da API.

NÃO altere schema.

Adicione Javadoc onde houver valor real.

Documente prioritariamente:

* interfaces públicas;
* services;
* domain services;
* controllers;
* repositories quando houver comportamento relevante;
* entities quando houver invariantes ou significado de domínio;
* DTOs relevantes;
* configuration classes;
* security components;
* exceptions relevantes;
* utilities;
* componentes de integração;
* classes relacionadas a autenticação/autorização.

Nas classes utilize:

`@author oEnzoRibas`

Documente, quando aplicável:

* responsabilidade;
* contexto;
* comportamento;
* invariantes;
* parâmetros;
* retornos;
* exceções;
* side effects;
* thread-safety;
* pré-condições;
* pós-condições;
* regras de domínio;
* aspectos de segurança;
* interações importantes com outros componentes.

Utilize adequadamente:

* `@param`;
* `@return`;
* `@throws`;
* `@see`;
* `{@link ...}`;
* `{@code ...}`.

Não escreva comentários como:

"Gets the username."

quando o método `getUsername()` já comunica isso perfeitamente.

Não documente getters/setters triviais apenas para aumentar cobertura.

Não replique implementação linha por linha em linguagem natural.

Explique principalmente o PORQUÊ, os contratos e comportamentos não óbvios.

Preserve documentação existente que seja correta e melhore-a quando necessário.

Verifique se os Javadocs continuam válidos após a padronização de nomenclatura realizada anteriormente.

Depois:

* compile o projeto;
* verifique warnings relevantes;
* execute testes.

Ao final apresente:

1. classes documentadas;
2. principais contratos documentados;
3. áreas deliberadamente não documentadas por serem triviais;
4. documentação ainda ausente;
5. resultado de build/testes.
