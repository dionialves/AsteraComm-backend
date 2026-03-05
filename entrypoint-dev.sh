#!/bin/bash
set -e

echo "[DEV] Compilando projeto..."
mvn compile -q

echo "[DEV] Iniciando Spring Boot com DevTools..."
mvn spring-boot:run &

echo "[DEV] Monitorando alteracoes em src/main..."
touch /tmp/last_compile

while true; do
    sleep 2
    CHANGED=$(find /app/src/main -name "*.java" -newer /tmp/last_compile 2>/dev/null | wc -l)
    if [ "$CHANGED" -gt 0 ]; then
        echo "[DEV] Alteracao detectada. Recompilando..."
        touch /tmp/last_compile
        mvn compile -q 2>&1 || echo "[DEV] Erro na compilacao — verifique o codigo"
    fi
done
