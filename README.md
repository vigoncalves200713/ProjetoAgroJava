# AgroJava

Sistema Integrado do Agronegócio desenvolvido para o projeto de **Lógica de Programação e Algoritmos** do SENAI-SP.

## Requisitos atendidos

- Cadastro do volume de chuva dos 7 dias em um `double[7]`.
- Cálculo da média semanal de pluviosidade.
- Identificação do dia com maior índice de chuva.
- Cadastro da umidade de 16 talhões em uma matriz `double[4][4]`.
- Exibição formatada do mapa do campo.
- Alertas para talhões com umidade abaixo de 30%.
- Menu interativo com `do-while` e `switch-case`.
- Entrada de dados via `Scanner`.
- Dados mantidos somente em memória, sem banco de dados ou interface gráfica.

## Requisitos técnicos

- Java 11 ou superior (desenvolvimento validado com JDK 21; compatível com JDK 26).
- Terminal/console.

## Como executar

Na raiz do projeto:

```bash
javac -d out src/AgroJava.java
java -cp out AgroJava
```

Os valores de chuva devem ser informados em milímetros. Os valores de umidade devem estar entre 0% e 100%. O sistema rejeita entradas fora dos intervalos válidos.

## Estrutura

```text
AgroJava/
├── src/
│   └── AgroJava.java
├── .gitignore
└── README.md
```
