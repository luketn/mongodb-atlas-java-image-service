FROM public.ecr.aws/lambda/python:3.12

#RUN pip install --no-cache-dir torch torchvision --index-url https://download.pytorch.org/whl/cpu
#RUN pip install --no-cache-dir transformers

ADD requirements.txt ${LAMBDA_TASK_ROOT}

RUN pip install -r requirements.txt

ADD main.py ${LAMBDA_TASK_ROOT}

CMD [ "main.handler" ]