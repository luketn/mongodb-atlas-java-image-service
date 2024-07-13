FROM public.ecr.aws/lambda/java:21
COPY ./code/ ${LAMBDA_TASK_ROOT}
CMD [ "com.mycodefu.Main::handleRequest" ]