set -e

docker build -t image-upload .
docker images --filter reference=image-upload:latest

if [ -z "$DOCKER_TAG" ]; then
  echo "DOCKER_TAG is not set. Exiting..."
  exit 1
fi

aws ecr --profile personal get-login-password --region ap-southeast-2  | docker login --username AWS --password-stdin 204244381428.dkr.ecr.ap-southeast-2.amazonaws.com
REPOSITORY_TAG="204244381428.dkr.ecr.ap-southeast-2.amazonaws.com/image-upload:${DOCKER_TAG}"
REPOSITORY_TAG_LATEST="204244381428.dkr.ecr.ap-southeast-2.amazonaws.com/image-upload:latest"
echo "$(date): Building and pushing ${REPOSITORY_TAG}..."
docker buildx build --push -t "${REPOSITORY_TAG}" -t "${REPOSITORY_TAG_LATEST}" --platform linux/arm64 .
