set -e
export DOCKER_TAG="build-$(date +%Y-%d-%m-%H-%M-%S)"
./build.sh
./deploy.sh

