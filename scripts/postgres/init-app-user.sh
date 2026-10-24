#!/bin/sh
set -eu

psql \
  --username "$POSTGRES_USER" \
  --dbname "$POSTGRES_DB" \
  --set=app_password="$POSTGRES_APP_PASSWORD" \
  --set=database_name="$POSTGRES_DB" <<'SQL'
CREATE ROLE infrapilot LOGIN PASSWORD :'app_password';
GRANT CONNECT, TEMPORARY ON DATABASE :"database_name" TO infrapilot;
GRANT USAGE, CREATE ON SCHEMA public TO infrapilot;
SQL
