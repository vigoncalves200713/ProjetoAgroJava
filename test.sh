#!/usr/bin/env bash
set -euo pipefail

rm -rf out
javac -Xlint:all -d out src/AgroJava.java

output="$(java -cp out AgroJava < test-input.txt)"
grep -q "Média semanal de chuva: 6.50 mm" <<< "$output"
grep -q "Maior índice: Quinta-feira (20.00 mm)" <<< "$output"
grep -q "Talhão \[1\]\[1\] com 25.00%" <<< "$output"
grep -q "AgroJava encerrado" <<< "$output"

echo "Testes do AgroJava executados com sucesso."
