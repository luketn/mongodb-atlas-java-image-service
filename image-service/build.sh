set -e

mvn clean package
rm -rf ./temp-build
mkdir temp-build
cp target/image-service.jar ./temp-build/
mv ./temp-build/image-service.jar ./temp-build/image-service.zip
cd temp-build
unzip image-service.zip
rm image-service.zip

cd ..
rm -rf ./docker/code
mkdir -p ./docker/code
cp -r ./temp-build/* ./docker/code/
rm -rf ./temp-build

cd docker
docker build -t image-service .

if [ -z "$DOCKER_TAG" ]; then
  echo "DOCKER_TAG is not set. Exiting..."
  exit 1
fi

aws ecr --profile personal get-login-password --region ap-southeast-2  | docker login --username AWS --password-stdin 204244381428.dkr.ecr.ap-southeast-2.amazonaws.com
REPOSITORY_TAG="204244381428.dkr.ecr.ap-southeast-2.amazonaws.com/image-service:${DOCKER_TAG}"
REPOSITORY_TAG_LATEST="204244381428.dkr.ecr.ap-southeast-2.amazonaws.com/image-service:latest"
echo "$(date): Building and pushing ${REPOSITORY_TAG}..."
docker buildx build --push -t "${REPOSITORY_TAG}" -t "${REPOSITORY_TAG_LATEST}" --platform linux/arm64 .
