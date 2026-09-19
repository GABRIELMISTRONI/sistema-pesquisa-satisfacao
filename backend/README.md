# Backend — API do sistema de chamados e pesquisa de satisfação

API REST em Java 21 + Spring Boot 3.5, com autenticação JWT.

## Banco de dados

Só um banco: **Postgres no Supabase**, schema `pesquisa_satisfacao` (esse Supabase é
compartilhado com outro sistema do grupo — um bot de Teams que usa o schema `public` —
por isso nossas tabelas ficam isoladas nesse schema próprio). Não tem mais banco local
(H2) neste projeto; sem as variáveis de ambiente abaixo o backend não sobe.

## Como rodar

1. Execute uma vez `src/main/resources/db/schema-supabase.sql` no SQL Editor do Supabase
   (dashboard do projeto → SQL Editor → New query), se as tabelas ainda não existirem.
2. Defina as variáveis de ambiente e suba:

```bash
set DB_URL=jdbc:postgresql://db.<project-ref>.supabase.co:5432/postgres
set DB_USUARIO=postgres
set DB_SENHA=<senha do banco>
cd backend
mvn spring-boot:run
```

A API sobe em `http://localhost:8080`.

> No Windows, se o `mvn` estiver usando outro JDK, aponte o JAVA_HOME para o Java 21:
> `set JAVA_HOME=C:\Program Files\Eclipse Adoptium\jdk-21.0.11.10-hotspot`

Outras variáveis opcionais:
- `JWT_SEGREDO` — chave de assinatura do token (tem um valor padrão de desenvolvimento).
- `FRONTEND_URL` — usada para montar o link da pesquisa e o redirect pós-clique no
  cartão do Teams (padrão `http://localhost:5173`, a porta do Vite).
- `BACKEND_URL` — usada para montar os links dos botões do cartão do Teams (padrão
  `http://localhost:8080`).
- `TEAMS_WEBHOOK_URL` — URL do webhook do Teams (Conector "Incoming Webhook" ou app
  "Workflows"). Sem ela, o envio ao Teams é só pulado (log de aviso), o resto do
  sistema funciona normalmente.

## Usuários criados automaticamente (`CargaInicial`, só roda se o banco estiver vazio)

| E-mail | Senha | Perfil |
|---|---|---|
| `admin@empresa.com` | `admin123` | ADMIN |
| `atendente@empresa.com` | `atendente123` | ATENDENTE |
| `cliente@empresa.com` | `cliente123` | CLIENTE |
| `bruno.ascielli@empresa.com` | `bruno123` | CLIENTE |

## Endpoints

O contrato completo, com todos os campos e códigos de erro, está em [`../PROMPT-FRONTEND.md`](../PROMPT-FRONTEND.md).

| Método | Rota | Acesso |
|---|---|---|
| POST | `/api/auth/login` | público |
| POST | `/api/auth/cadastrar` | público (cria CLIENTE) |
| GET | `/api/auth/eu` | autenticado |
| GET | `/api/chamados` | autenticado |
| POST | `/api/chamados` | autenticado |
| GET | `/api/chamados/{id}` | autenticado |
| PATCH | `/api/chamados/{id}/status` | ADMIN, ATENDENTE |
| POST | `/api/chamados/{id}/encerrar` | ADMIN, ATENDENTE |
| GET | `/api/chamados/{id}/mensagens` | autenticado (mesma regra de acesso do chamado) |
| POST | `/api/chamados/{id}/mensagens` | autenticado; 409 se o chamado estiver ENCERRADO |
| GET | `/api/chamados/{id}/anexos` | autenticado (mesma regra de acesso do chamado) |
| POST | `/api/chamados/{id}/anexos` | autenticado; 409 se o chamado estiver ENCERRADO |
| GET | `/api/chamados/{id}/anexos/{anexoId}` | autenticado (download) |
| GET | `/api/pesquisa/{token}` | público |
| POST | `/api/pesquisa/{token}` | público |
| GET | `/api/pesquisa/{token}/resposta-rapida/{nota}` | público (clique no cartão do Teams; redireciona pro front) |
| GET | `/api/relatorios/resumo` | só ADMIN |
| GET | `/api/relatorios/avaliacoes` | só ADMIN |

## Organização do código

```
com.faculdade.pesquisa
├── config/       SecurityConfig, tratamento de erros, carga inicial de dados
├── controller/   Endpoints REST
├── domain/       Entidades JPA (Usuario, Chamado, Avaliacao, Mensagem, Anexo) e enums
├── dto/          Records de entrada e saída da API
├── repository/   Interfaces Spring Data JPA
├── security/     JWT (geração, filtro) e UserDetails
└── service/      Regras de negócio
```

## Regras principais do projeto

- Quando um chamado é encerrado (`PATCH .../status` com `ENCERRADO` ou
  `POST .../encerrar`), o `PesquisaService` gera automaticamente uma pesquisa de
  satisfação (nota de 1 a 10, estilo NPS — 1-6 ruim, 7-8 razoável, 9-10 bom) e o
  `TeamsNotificacaoService` dispara um cartão para o Teams com botões de resposta
  rápida. O cliente também pode responder pelo link público, sem precisar de login.
- Chat e anexos ficam disponíveis enquanto o chamado está ABERTO ou EM_ANDAMENTO;
  ao encerrar, viram somente leitura.
- Só o `CLIENTE` responde a própria pesquisa — ADMIN/ATENDENTE não têm acesso ao
  link pelo sistema, só acompanham se já foi respondida.
- O painel de satisfação (`/api/relatorios/**`) é exclusivo do ADMIN. O ATENDENTE
  cuida só do atendimento dos chamados.
