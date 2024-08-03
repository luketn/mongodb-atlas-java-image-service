set -e

echo "$(date): Starting local Atlas docker..."
docker compose down -v
docker compose up -d
