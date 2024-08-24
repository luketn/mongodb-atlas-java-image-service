if [ -z "${REPOSITORY_TAG}" ]; then
  echo "REPOSITORY_TAG is not set. Run from build-deploy.sh..."
  exit 1
fi

npm run deploy