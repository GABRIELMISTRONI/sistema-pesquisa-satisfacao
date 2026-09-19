# Sistema de Pesquisa de Satisfação Automatizada para Atendimento ao Cliente

Projeto desenvolvido como parte do Projeto Bootcamp Extensionista, no módulo de Bootcamp Computação em Nuvem, do curso de Análise e Desenvolvimento de Sistemas.

## Sobre o projeto

O projeto surgiu a partir de uma situação real no atendimento ao cliente.

Após o encerramento de um chamado, é realizada uma pesquisa para saber como foi a experiência do cliente com o atendimento. Porém, poucas pessoas acabam respondendo.

Com isso, a equipe recebe poucas avaliações e fica mais difícil acompanhar a satisfação dos clientes e identificar pontos que podem ser melhorados.

A proposta do grupo foi desenvolver um sistema completo de chamados que automatiza esse processo: ao encerrar um atendimento, a pesquisa de satisfação é gerada e enviada automaticamente, sem depender de o cliente lembrar de acessar um link avulso.

## Objetivo

Desenvolver um sistema para:

- Abrir, acompanhar e encerrar chamados de atendimento, com chat e anexos;
- Gerar automaticamente uma pesquisa de satisfação ao encerrar um chamado;
- Permitir que o cliente avalie o atendimento (nota de 1 a 10, estilo NPS) sem precisar de login;
- Notificar a equipe pelo Microsoft Teams, com resposta rápida direto pelo cartão;
- Organizar os dados das avaliações e apresentar um painel de relatórios para o administrador.

## Arquitetura e tecnologias

| Camada | Tecnologia |
|---|---|
| **Backend** | Java 21 + Spring Boot 3.5 — API REST com autenticação JWT |
| **Banco de dados** | PostgreSQL, hospedado no Supabase (schema próprio `pesquisa_satisfacao`) |
| **Frontend** | React 18 + Vite |
| **Notificações** | Microsoft Teams (webhook / conector), com botões de resposta rápida no cartão |

> A proposta inicial previa uma arquitetura serverless na AWS (Lambda, API Gateway, DynamoDB, S3). Durante o desenvolvimento, o grupo optou por uma stack Java/Spring + PostgreSQL (Supabase) + React, por ser mais aderente ao que o grupo já dominava e por cobrir com mais robustez as funcionalidades de chamados, chat e anexos.

## Perfis de usuário

- **ADMIN** — acesso ao painel de relatórios e acompanhamento geral das avaliações;
- **ATENDENTE** — cuida do atendimento dos chamados (chat, status, encerramento);
- **CLIENTE** — abre chamados e responde à própria pesquisa de satisfação.

## Fluxo da solução

1. O cliente abre um chamado (com possibilidade de chat e anexos enquanto estiver ABERTO ou EM_ANDAMENTO);
2. O atendente conduz o atendimento e, ao final, encerra o chamado;
3. O sistema gera automaticamente uma pesquisa de satisfação vinculada a esse chamado;
4. Uma notificação é enviada ao Teams, com botões de resposta rápida (nota de 1 a 10);
5. O cliente também pode responder pelo link público da pesquisa, sem precisar de login;
6. A nota é classificada automaticamente (1–6 ruim, 7–8 razoável, 9–10 bom);
7. O administrador acompanha os resultados consolidados no painel de relatórios.

## Como rodar

### Backend

```bash
set DB_URL=jdbc:postgresql://db.<project-ref>.supabase.co:5432/postgres
set DB_USUARIO=postgres
set DB_SENHA=<senha do banco>
cd backend
mvn spring-boot:run
```

A API sobe em `http://localhost:8080`. Instruções completas, variáveis opcionais e usuários de teste estão em [`backend/README.md`](./backend/README.md).

### Frontend

```bash
cd frontend
npm install
npm run dev
```

A interface sobe em `http://localhost:5173`.

## Estrutura do projeto

```
├── backend/    # API REST em Java + Spring Boot (chamados, autenticação, pesquisa, relatórios)
├── frontend/   # Interface em React + Vite
└── README.md
```

## Equipe

- **Miguel Guilherme Prando** — Desenvolvimento do projeto
- **Gabriel Pomini de Souza** — Desenvolvimento e integração
- **Bruno Henrique Ascielli** — Desenvolvimento do projeto e contato com a instituição
- **Lucas Kauã Silveira da Conceição** — Coordenação e levantamento de requisitos
- **Victor Yuji Fujiyama** — Arquitetura e infraestrutura em nuvem
- **Gabriel Mistroni Ramos Souza** — Documentação, organização das evidências e apresentação

## Período

01/09/2026 a 26/09/2026
