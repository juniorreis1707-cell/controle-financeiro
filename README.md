# Controle Financeiro Pessoal

Aplicação de console em Java para registrar receitas e despesas, com os dados salvos em um arquivo CSV.

## Funcionalidades

- Adicionar receitas e despesas (descrição, categoria, valor e data)
- Ver o saldo geral
- Extrato do mês, com total de receitas, de despesas e saldo do mês
- Resumo das despesas do mês por categoria
- Remover transações
- Os dados ficam em `transacoes.csv`. Se uma linha do arquivo estiver com problema, ela é ignorada e as outras continuam sendo carregadas.

## Estrutura

```
src/main/java/financeiro
├── Main.java                        # menu e leitura do teclado
├── model
│   ├── TipoTransacao.java           # RECEITA ou DESPESA
│   └── Transacao.java               # record com validações
├── repository
│   └── RepositorioTransacoes.java   # leitura e gravação do CSV
└── service
    └── ServicoFinanceiro.java       # regras: saldo, extrato, resumo
```

## Como rodar

Requisitos: JDK 25 e Maven.

```bash
mvn compile
java -cp target/classes financeiro.Main
```

Ou abra o projeto no IntelliJ IDEA e execute a classe `Main`.
