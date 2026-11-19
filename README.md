# AgroJava

Sistema Integrado do Agronegócio desenvolvido para o projeto de **Lógica de Programação e Algoritmos** do SENAI-SP.

## Funcionalidades implementadas

- **RF01–RF03:** cadastro de chuva dos sete dias em array, média e maior índice; cadastro da matriz 4x4 de umidade; mapa e alertas; menu de terminal.
- **RF04:** registro de semana agrícola com data, precipitação diária, média e total em `historico_chuvas.csv`, acrescentando registros sem sobrescrever semanas anteriores.
- **RF05–RF07:** consulta de todo o histórico persistido, identificação da semana mais chuvosa e dashboard com acumulado, média semanal, semana mais seca e mais chuvosa e quantidade de semanas.
- **RF08–RF09:** exportação dos 16 talhões para `mapa_umidade.csv` e `talhoes_classificados.csv`, com classificação automática: abaixo de 20% = Crítico; de 20% a 30% = Atenção; acima de 30% = Adequado.
- **RF10:** relatório de irrigação que exibe apenas talhões Críticos ou em Atenção e informa a quantidade de alertas.
- **RF11–RF12:** indicadores gerenciais do campo (média, talhões críticos e adequados, percentual que precisa de irrigação, talhão mais seco/úmido) e exportação para `relatorio_fazenda.txt`.

As opções originais do menu foram preservadas (1–4); as opções 5–8 acrescentam histórico, semana mais chuvosa, dashboard e exportação gerencial. Os dados são mantidos em arrays/matrizes durante a execução e persistidos nos arquivos CSV/TXT descritos acima. Não há banco de dados ou interface gráfica.

## Requisitos técnicos

- Java 11 ou superior.
- Entrada e interação via terminal com `Scanner`.
- Bibliotecas padrão do Java; sem dependências externas.

## Como executar

Na raiz do projeto:

```bash
javac -Xlint:all -d out src/AgroJava.java
java -cp out AgroJava
```

Na opção **1**, informe a data no formato `dd/MM/aaaa`, a chuva de segunda-feira a domingo em milímetros e a umidade de A1 a D4 em porcentagem. Chuvas não podem ser negativas e umidades devem estar entre 0% e 100%. O CSV de chuvas é acumulativo; os arquivos do mapa e do relatório representam o cadastro mais recente.

## Testes

Execute `./test.sh`. A suíte compila o projeto, percorre cadastro, mapa, alertas, histórico, dashboard e exportação e verifica que o histórico pode ser consultado após reiniciar o programa. Os arquivos de teste são criados em um diretório temporário.

## Estrutura

```text
AgroJava/
├── src/AgroJava.java
├── test.sh
├── test-input.txt
├── README.md
└── .gitignore
```
