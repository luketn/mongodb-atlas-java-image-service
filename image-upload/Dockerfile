FROM public.ecr.aws/lambda/python:3.12

#install dependencies
ADD requirements.txt ${LAMBDA_TASK_ROOT}
RUN pip install --no-cache-dir -r requirements.txt

#download the model
ADD download-model.py ${LAMBDA_TASK_ROOT}
RUN python download-model.py

#setup the lambda function handler
ADD main.py ${LAMBDA_TASK_ROOT}
CMD [ "main.handler" ]