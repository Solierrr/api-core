#!/bin/sh
set -e

INFISICAL_TOKEN=$(infisical login --method=universal-auth \
  --client-id="$INFISICAL_CLIENT_ID" \
  --client-secret="$INFISICAL_CLIENT_SECRET" \
  --silent --plain)

exec infisical run \
  --token="$INFISICAL_TOKEN" \
  --projectId=2296d19c-5f3b-41e1-afa3-fcde39966a71 \
  --env="${INFISICAL_ENV:-qa}" \
  --path=/database --path=/redis --path=/cloudinary --path=/google --path=/auth --path=/otel --path=/service-urls \
  -- java -jar app.jar
