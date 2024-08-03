import os
import time

os.environ["PYTORCH_ENABLE_MPS_FALLBACK"] = "1"
os.environ["HF_ENDPOINT"] = "https://huggingface.co"
os.environ["ACCELERATE_USE_MPS_DEVICE"] = "True"

import numpy as np
from PIL import Image
import torch
from transformers import AutoModelForCausalLM
from transformers import AutoProcessor
from accelerate import Accelerator

if torch.cuda.is_available():
    print("Running on NVIDIA GPU")
    device = torch.device("cuda")
# Phi3 does not support Apple M1 GPU (yet)
elif torch.backends.mps.is_available() and torch.backends.mps.is_built():
    print("Running on Apple M1 GPU")
    device = torch.device("mps")
else:
    print("Running on CPU. Expect __VERY__ slow performance.")
    device = torch.device("cpu")


model_name = "microsoft/Phi-3-vision-128k-instruct"
accelerator = Accelerator()
print('Accelerator device: ', accelerator.device)

tm_start = time.time()
processor = AutoProcessor.from_pretrained(model_name, trust_remote_code=True, device=device)
model = AutoModelForCausalLM.from_pretrained(model_name,
                                             trust_remote_code=True,
                                             torch_dtype="auto",
                                             _attn_implementation="eager",
                                             offload_folder="offload",
                                             offload_state_dict=True,
                                         ).to(device)
tm_end = time.time()
print(f'Loaded in {tm_end - tm_start} seconds.')

user_prompt = '<|user|>\n'
assistant_prompt = '<|assistant|>\n'
prompt_suffix = "<|end|>\n"

def generate_response(input_text, input_image):
    prompt = f"{user_prompt}"
    images = None

    if isinstance(input_image, np.ndarray):
        prompt += f"<|image_1|>\n"
        images = [Image.fromarray(input_image)]
    elif isinstance(input_image, Image.Image):
        prompt += f"<|image_1|>\n"
        images = [input_image]

    if input_text != "":
        prompt += f"{input_text}"

    prompt += f"{prompt_suffix}{assistant_prompt}"
    print(f">>> Prompt\n{prompt}")

    inputs = processor(prompt, images=images, return_tensors="pt").to(device)

    generate_ids = model.generate(**inputs,
                                  max_new_tokens=1000,
                                  eos_token_id=processor.tokenizer.eos_token_id,
                                  )

    generate_ids = generate_ids[:, inputs['input_ids'].shape[1]:]

    response = processor.batch_decode(generate_ids,
                                      skip_special_tokens=True,
                                      clean_up_tokenization_spaces=False)[0]

    print(f'>>> Response\n{response}')

    return response


generate_response("What is shown in this image?",
                  Image.open('photos/Images/n02085620-Chihuahua/n02085620_7.jpg')
)