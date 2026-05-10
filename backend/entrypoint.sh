#!/bin/bash
set -e

echo "Starting Spring Boot service..."

# Sealos recommends entrypoint.sh only do startup.
# Prefer prebuilt jar, and fallback to mvn spring-boot:run for dev convenience.
JAR_PATH=$(ls target/*.jar 2>/dev/null | grep -v "original-" | head -n 1 || true)

if [ -n "$JAR_PATH" ]; then
  echo "Using jar: $JAR_PATH"
  exec java -jar "$JAR_PATH" --server.port="${PORT:-8080}" --server.address=0.0.0.0
fi

echo "No jar found in target/, fallback to Maven run..."
exec mvn spring-boot:run -Dspring-boot.run.arguments="--server.port=${PORT:-8080} --server.address=0.0.0.0"
