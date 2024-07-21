if [ -z "$DOCKER_TAG" ]; then
  echo "DOCKER_TAG is not set. Run from build-deploy.sh..."
  exit 1
fi

npm run deploy