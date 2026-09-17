# Sistema de Pesquisa de Satisfação Automatizada para Atendimento ao Cliente

Projeto desenvolvido como parte do Projeto Bootcamp Extensionista, no módulo de Bootcamp Computação em Nuvem, do curso de Análise e Desenvolvimento de Sistemas.

## Sobre o projeto

O projeto surgiu a partir de uma situação real no atendimento ao cliente.

Após o encerramento de um chamado, é realizada uma pesquisa para saber como foi a experiência do cliente com o atendimento. Porém, poucas pessoas acabam respondendo.

Com isso, a equipe recebe poucas avaliações e fica mais difícil acompanhar a satisfação dos clientes e identificar pontos que podem ser melhorados.

A proposta do grupo é desenvolver um sistema que automatize esse processo, facilitando o envio da pesquisa e o registro das respostas.

## Objetivo

Desenvolver um protótipo para:

- Enviar uma pesquisa após o encerramento de um atendimento;
- Permitir que o cliente avalie o atendimento;
- Registrar as respostas;
- Organizar os dados das avaliações;
- Facilitar a análise da satisfação dos clientes.

## Arquitetura e tecnologias

A solução será desenvolvida utilizando serviços de computação em nuvem.

A arquitetura prevista utiliza:

- AWS Lambda
- Amazon API Gateway
- Amazon DynamoDB
- Amazon S3
- Amazon CloudWatch

## Fluxo da solução

1. O atendimento é encerrado;
2. A pesquisa de satisfação é disponibilizada ao cliente;
3. O cliente realiza a avaliação;
4. A resposta é recebida pelo sistema;
5. A avaliação é armazenada;
6. Os dados podem ser utilizados para acompanhar os resultados das pesquisas.

Para a demonstração, serão utilizados dados fictícios ou anonimizados.

## Equipe

- **Miguel Guilherme Prando** — Desenvolvimento do projeto
- **Gabriel Pomini de Souza** — Desenvolvimento e integração
- **Bruno Henrique Ascielli** — Desenvolvimento do projeto e contato com a instituição
- **Lucas Kauã Silveira da Conceição** — Coordenação e levantamento de requisitos
- **Victor Yuji Fujiyama** — Arquitetura e infraestrutura em nuvem
- **Gabriel Mistroni Ramos Souza** — Documentação, organização das evidências e apresentação

## Período

01/09/2026 a 26/09/2026

## Estrutura do projeto

```
├── src/       # Código das funções Lambda e da lógica da aplicação
├── infra/     # Configuração da infraestrutura em nuvem (API Gateway, DynamoDB, S3)
├── web/       # Interface web simples de resposta à pesquisa
└── docs/      # Documentação do projeto e evidências
```
