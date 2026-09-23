-- Estrutura do banco no Supabase (Postgres) para o sistema de chamados
-- e pesquisa de satisfacao (trabalho de faculdade).
--
-- Este projeto Supabase e COMPARTILHADO com outro sistema (um bot de Teams
-- para pesquisa de satisfacao, que usa as tabelas "usuarios" e "chamados" no
-- schema "public"). Para nao colidir com esse trabalho em paralelo, todas as
-- tabelas deste projeto ficam num schema Postgres separado: "pesquisa_satisfacao".
--
-- Rodar uma vez no SQL Editor do Supabase (dashboard do projeto -> SQL Editor -> New query).

create schema if not exists pesquisa_satisfacao;

create table if not exists pesquisa_satisfacao.usuarios (
    id          bigserial primary key,
    nome        varchar(120) not null,
    email       varchar(160) not null unique,
    senha_hash  varchar(255) not null,
    perfil      varchar(20)  not null,
    ativo       boolean      not null default true,
    criado_em   timestamptz  not null default now()
);

create table if not exists pesquisa_satisfacao.chamados (
    id             bigserial primary key,
    titulo         varchar(160)  not null,
    descricao      varchar(2000) not null,
    status         varchar(20)   not null,
    prioridade     varchar(20)   not null,
    solicitante_id bigint        not null references pesquisa_satisfacao.usuarios (id),
    responsavel_id bigint        references pesquisa_satisfacao.usuarios (id),
    criado_em      timestamptz   not null default now(),
    atualizado_em  timestamptz   not null default now(),
    encerrado_em   timestamptz
);

create index if not exists idx_chamados_solicitante on pesquisa_satisfacao.chamados (solicitante_id);
create index if not exists idx_chamados_status on pesquisa_satisfacao.chamados (status);

create table if not exists pesquisa_satisfacao.avaliacoes (
    id               bigserial primary key,
    chamado_id       bigint       not null unique references pesquisa_satisfacao.chamados (id),
    token            varchar(60)  not null unique,
    nota             integer      check (nota between 1 and 10),
    comentario       varchar(1000),
    criada_em        timestamptz  not null default now(),
    respondida_em    timestamptz,
    -- Momento em que o e-mail da pesquisa deve sair (encerramento + atraso
    -- configurado em app.email.atraso-minutos) e quando de fato saiu.
    enviar_em        timestamptz  not null default now(),
    email_enviado_em timestamptz
);

create index if not exists idx_avaliacoes_token on pesquisa_satisfacao.avaliacoes (token);
create index if not exists idx_avaliacoes_pendentes_envio on pesquisa_satisfacao.avaliacoes (enviar_em)
    where email_enviado_em is null;

-- Rodar isto se a tabela avaliacoes ja existia antes desta mudanca (adiciona
-- as colunas sem apagar dados; se a tabela acabou de ser criada acima, e um no-op).
alter table pesquisa_satisfacao.avaliacoes add column if not exists enviar_em timestamptz not null default now();
alter table pesquisa_satisfacao.avaliacoes add column if not exists email_enviado_em timestamptz;

create table if not exists pesquisa_satisfacao.mensagens (
    id         bigserial primary key,
    chamado_id bigint        not null references pesquisa_satisfacao.chamados (id),
    autor_id   bigint        not null references pesquisa_satisfacao.usuarios (id),
    texto      varchar(2000) not null,
    criada_em  timestamptz   not null default now()
);

create index if not exists idx_mensagens_chamado on pesquisa_satisfacao.mensagens (chamado_id, criada_em);

create table if not exists pesquisa_satisfacao.anexos (
    id            bigserial primary key,
    chamado_id    bigint        not null references pesquisa_satisfacao.chamados (id),
    autor_id      bigint        not null references pesquisa_satisfacao.usuarios (id),
    nome_arquivo  varchar(255)  not null,
    tipo_conteudo varchar(100)  not null,
    tamanho_bytes bigint        not null,
    conteudo      bytea         not null,
    criado_em     timestamptz   not null default now()
);

create index if not exists idx_anexos_chamado on pesquisa_satisfacao.anexos (chamado_id);

-- Boa pratica: o backend acessa via JDBC direto (usuario dono das tabelas, que
-- ignora RLS por padrao no Postgres), entao habilitar RLS sem politicas so
-- bloqueia o acesso via API publica do Supabase (chave anon/authenticated),
-- que este projeto nao usa.
alter table pesquisa_satisfacao.usuarios enable row level security;
alter table pesquisa_satisfacao.chamados enable row level security;
alter table pesquisa_satisfacao.avaliacoes enable row level security;
alter table pesquisa_satisfacao.mensagens enable row level security;
alter table pesquisa_satisfacao.anexos enable row level security;
