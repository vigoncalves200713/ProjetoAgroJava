#!/usr/bin/env bash
set -euo pipefail
ROOT="$(cd "$(dirname "$0")" && pwd)"
TMP="$(mktemp -d)"
trap 'rm -rf "$TMP"' EXIT
javac -Xlint:all -d "$TMP" "$ROOT/src/AgroJava.java"
(
  cd "$TMP"
  output="$(java -cp "$TMP" AgroJava < "$ROOT/test-input.txt")"
  grep -q "Média semanal de chuva: 6.50 mm" <<< "$output"
  grep -q "Maior índice: Sexta-feira (20.00 mm)" <<< "$output"
  grep -q "A1 - Crítico" <<< "$output"
  grep -q "Total de alertas: 5" <<< "$output"
  grep -q "Semana mais chuvosa: 10/11/2026" <<< "$output"
  grep -q "Volume acumulado: 45.50 mm" <<< "$output"
  grep -q "AgroJava encerrado" <<< "$output"
  grep -q '^data,seg,ter,qua,qui,sex,sab,dom,media,total$' historico_chuvas.csv
  grep -q '^10/11/2026,0.00,10.50,5.00,0.00,20.00,7.50,2.50,6.50,45.50$' historico_chuvas.csv
  test "$(wc -l < mapa_umidade.csv)" -eq 17
  test "$(wc -l < talhoes_classificados.csv)" -eq 17
  grep -q '^A1,18.00,Crítico$' talhoes_classificados.csv
  grep -q 'Talhão mais seco: C1 - 14.00%' relatorio_fazenda.txt
  # Confirma que o histórico é carregado também após reiniciar o programa.
  printf '5\n4\n' | java -cp "$TMP" AgroJava > reinicio.txt
  grep -q 'Semana: 10/11/2026 | Média: 6.50 mm | Total: 45.50 mm' reinicio.txt
)
echo "Testes do AgroJava (RF01–RF12) executados com sucesso."
