# Acesso local à VPS e diagnóstico seguro

Créditos: **oEnzoRibas**. Confirmado pelo responsável: existe VPS do Podiss com Ubuntu 24.04 LTS. Não sabemos ainda como Java é executado, onde está o banco usado pelo processo e qual proxy termina TLS.

## 1. Onde configurar

Modelo versionado: deploy/vps.ssh.config.example.
Arquivo pessoal: **.local/vps.ssh.config**, ignorado pelo Git. Nesta sessão foi criado somente com placeholders.

Preencha:

- HostName: endereço da VPS mostrado no painel.
- User: usuário SSH que já existe na VPS (não pressupor deploy/root).
- Port: porta SSH real.
- IdentityFile: caminho de chave privada no seu computador, se já autorizada no servidor.

Nunca coloque senha SSH nesse arquivo, em .env.dev ou em documentação. Se só tem senha, o cliente SSH a solicitará interativamente. Não precisa enviá-la ao assistente. Não altere root login, firewall ou usuários como parte desta configuração.

Antes de conectar, confira a fingerprint da chave do servidor por canal confiável, como console da VPS no painel. Pelo console, comando de leitura:

```sh
ssh-keygen -lf /etc/ssh/ssh_host_ed25519_key.pub
```

Na primeira conexão, compare a fingerprint apresentada com a do console; só aceite se conferir:

```powershell
ssh -F .local/vps.ssh.config -o StrictHostKeyChecking=ask podiss-vps
```

Depois:

```powershell
ssh -F .local/vps.ssh.config podiss-vps
```

O template mantém StrictHostKeyChecking=yes. Não usar =no nem apagar known_hosts cegamente se a chave mudar.

## 2. Chave pessoal opcional

Se desejar autenticação por chave, gere uma chave nova fora do repositório, com passphrase, apenas se o arquivo ainda não existir:

```sh
ssh-keygen -t ed25519 -a 100 -f ~/.ssh/podiss_ed25519 -C podiss-development
```

A chave privada não deve sair da sua máquina. Instalar a chave pública em authorized_keys é uma alteração no servidor: fazer somente com autorização e mantendo acesso de recuperação. Não foi feito por esta entrega. Não sobrepor chave existente.

Restrinja as permissões de .local e dos arquivos SSH ao seu usuário (Linux/macOS: diretório 700, arquivo 600; Windows: ACL pelo painel de Segurança do arquivo). A chave fica em ~/.ssh, não dentro de .local.

## 3. Diagnóstico somente de leitura

Execute no SSH ou console, um comando por vez. “Permission denied” ou “command not found” são informações úteis; não instale/ative nada para corrigir agora.

```sh
cat /etc/os-release
java -version
ps -C java -o pid,comm,etime
systemctl list-units --type=service --state=running --no-pager
docker --version
docker compose version
docker ps --format '{{.Names}} | {{.Image}} | {{.Ports}}'
ss -lnt
```

Esses comandos não reiniciam processos, não alteram banco e evitam imprimir environment/argumentos completos de aplicações. Liste apenas nomes/status de serviços relevantes, versão Java, imagens e portas; oculte IPs privados e dados que não deseje compartilhar.

Não envie .env, chaves, certificados privados, docker inspect completo, systemctl show Environment, ps com argumentos ou logs sem revisão: podem conter secrets.

Para identificar o banco, phpMyAdmin é somente uma interface. Observe de qual serviço do painel você o abriu (gestão de banco do site ou painel da VPS) e a identificação de servidor. Isso pode ajudar, mas a comprovação definitiva é o datasource do **processo Java**, inspecionado localmente pelo responsável sem divulgar senha. Pode ser necessário perguntar a quem fez o deploy anterior.

## 4. Informação mínima para fechar o deploy

- Processo/serviço/contêiner que executa o backend e JDK utilizado.
- Local do JAR/imagem e mecanismo de restart.
- Banco usado: na VPS, em contêiner ou gerenciado hPanel; conectividade/TLS.
- Proxy ativo, hostname da API, certificado e portas públicas.
- Segunda base e política de backup/restore.
- Espaço/RAM disponíveis e usuário operacional.

Não executar upload, restart, install, alterações DNS/firewall ou migrations nessa fase de diagnóstico. Credenciais preenchidas localmente não equivalem a autorização de deploy.
