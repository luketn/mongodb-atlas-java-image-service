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
docker build --platform linux/arm64 -t image-service .

if [ -z "$REPOSITORY_TAG" ]; then
  echo "DOCKER_TAG is not set. Exiting..."
  exit 1
fi

if [ -z "$CI" ]; then
  aws ecr --profile "${AWS_PROFILE}" get-login-password --region "${AWS_REGION}" | docker login --username AWS --password-stdin "${REGISTRY}"
fi
echo "$(date): Building and pushing ${REPOSITORY_TAG}..."
docker buildx build --push -t "${REPOSITORY_TAG}" -t "${REPOSITORY_TAG_LATEST}" --platform linux/arm64 .
