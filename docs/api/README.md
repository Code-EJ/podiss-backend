# API HTTP

Créditos: **oEnzoRibas**. Contrato gerado a partir dos controllers, DTOs e OpenApiConfig; decisão em [ADR 0006](../ADRS/0006-api-documentation.md).

No Docker local, após reconstruir: http://localhost:18080/swagger-ui/index.html e http://localhost:18080/v3/api-docs. Em produção a documentação fica desabilitada. Login retorna token; em Authorize use somente token de teste local. Senhas são writeOnly na especificação, não exemplos reais.

## Inventário de 18 operações

| Caminho | Método | Acesso | Sucesso |
| --- | --- | --- | --- |
| /api/auth/login | POST | Público | 200 token |
| /api/auth/register | POST | ADMIN | 201 string |
| /contatos | POST / GET | Público / ADMIN | 201 objeto / 200 array |
| /sugestoes | POST / GET | Público / ADMIN | 201 objeto / 200 array |
| /episodes | POST / GET | ADMIN / Público | 201 objeto / 200 array |
| /episodes/{id} | GET / DELETE | Público / ADMIN | 200 objeto / 204 |
| /posts | POST / GET | ADMIN / Público | 201 resumo / 200 array |
| /posts/{id} | GET / PUT / DELETE | Público / ADMIN / ADMIN | 200 / 200 / 204 |
| /posts/{id}/image | PUT / DELETE | ADMIN | 200 resumo / 204 |
| /posts/image/{id} | GET | Público | 200 binário |

Na especificação, /episodes/{id} reúne dois templates equivalentes. **GET recebe YouTube ID de 11 caracteres; DELETE recebe UUID interno**. No código GET continua nomeado youtubeId. Apenas o nome do placeholder documental foi unificado, pois OpenAPI não permite duplicar um caminho variando somente o nome do parâmetro. URLs efetivas não mudaram.

Listas usam page=0, size=100 (1–100), order=asc|desc. Retornam array, não envelope; headers X-Total-Count, X-Total-Pages, X-Page e X-Page-Size. Sort createdAt,id.

Criação de post e substituição de imagem são multipart; PUT de texto é JSON com semântica parcial (null preserva, tags=[] limpa). Respostas de post usam tags como string separada por vírgulas. Imagem aceita assinatura JPEG/PNG/GIF/WebP até 5MiB, request multipart até 6MiB; isso não garante decodificação completa.

DTOs: CreateContactMessageRequest (nome/email/assunto/mensagem), CreateTopicSuggestionRequest (nome/email/tema), CreateEpisodeRequest, CreatePostRequest, UpdatePostRequest, PostResponse, LoginRequest, RegisterRequest e JwtResponse. Bean Validation fornece restrições ao schema; ApiProblem descreve erros esperados. Entidades de contato/sugestão/episódio são respostas atuais — não trocar por outro DTO silenciosamente.

Erros documentados por operação: 400 validação; 401 credenciais; 403 ADMIN; 404 ausente; 409 conflito; 413 limite de upload; 429 quota de submissão com Retry-After; 502/504 YouTube. Um Bearer inválido enviado em rota pública também pode causar 401. Falhas de proxy/servidor não são certificadas por essa lista.

## Verificação

OpenApiTests confere paths, 18 métodos, summaries, operation IDs, segurança, respostas de sucesso, nomes legados, multipart, imagens e headers. Confere HTML do Swagger e configuração de URL; não automatiza navegador visual nem testa YouTube real. OpenApiDisabledTests verifica bloqueio mesmo para ADMIN. Testes não equivalem a validação externa integral da especificação; evolução deve adicionar casos para constraints/exemplos e erros específicos.
