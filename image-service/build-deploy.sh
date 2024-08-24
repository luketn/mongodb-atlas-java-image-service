set -e
if [ -z "$CI" ]; then
  echo "Running locally. Logging into Docker..." #TODO: If you are running locally, update these with your own values / profile
  export REGISTRY="204244381428.dkr.ecr.ap-southeast-2.amazonaws.com"
  export REPOSITORY="image-service"
  export AWS_PROFILE="personal"
  export AWS_REGION="ap-southeast-2"
fi
DOCKER_TAG="build-$(date +%Y-%d-%m-%H-%M-%S)"
export REPOSITORY_TAG="${REGISTRY}/${REPOSITORY}:${DOCKER_TAG}"
export REPOSITORY_TAG_LATEST="${REGISTRY}/${REPOSITORY}:latest"

./build.sh
./deploy.sh

