FROM python:3.9-slim-buster

RUN pip install --no-cache-dir torch torchvision --index-url https://download.pytorch.org/whl/cpu
RUN pip install --no-cache-dir transformers

WORKDIR /phi3
ADD main.py ./

ENTRYPOINT ["python", "main.py"]
