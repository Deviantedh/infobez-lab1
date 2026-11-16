#!/usr/bin/env bash
cd "$(dirname "$0")"

# Отключаем строгую проверку прав файловой системы Buildx для путей с кириллицей (как в ВПСТ)
export BUILDX_BAKE_ENTITLEMENTS_FS=0

docker compose up -d "$@"
echo ""
echo "API запущено в Docker!"
echo "Swagger UI: http://localhost:8080/swagger-ui.html"
