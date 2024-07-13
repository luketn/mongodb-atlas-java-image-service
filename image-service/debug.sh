echo "Running the Docker image with RIE for local lambda API Gateway debugging..."
docker kill image-service || true
docker rm image-service || true

# ref: https://github.com/aws/aws-lambda-runtime-interface-emulator?tab=readme-ov-file#installing

echo "Starting the Docker image with RIE for local lambda API Gateway debugging, to test run:"
echo "curl -XPOST \"http://localhost:9000/2015-03-31/functions/function/invocations\" -d '{\"requestContext\": {\"http\": {\"method\": \"GET\"}}}'"

docker run \
    --name image-service \
    -it \
    --platform linux/arm64 \
    -v ~/.aws-lambda-rie:/aws-lambda \
    -p 9000:8080 \
    --entrypoint /aws-lambda/aws-lambda-rie \
    image-service \
    java -cp './*:/var/runtime/lib/*' com.amazonaws.services.lambda.runtime.api.client.AWSLambda com.mycodefu.Main::handleRequest
