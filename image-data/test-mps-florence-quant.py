import os
import time

# workaround for unnecessary flash_attn requirement
from unittest.mock import patch
from transformers.dynamic_module_utils import get_imports


def fixed_get_imports(filename: str | os.PathLike) -> list[str]:
    if not str(filename).endswith("modeling_florence2.py"):
        return get_imports(filename)
    imports = get_imports(filename)
    imports.remove("flash_attn")
    return imports


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


model_name = "unclecode/folrence-2-large-4bit"
accelerator = Accelerator()
print('Accelerator device: ', accelerator.device)



tm_start = time.time()
processor = AutoProcessor.from_pretrained(model_name, trust_remote_code=True, device=device)
with patch("transformers.dynamic_module_utils.get_imports", fixed_get_imports): #workaround for unnecessary flash_attn requirement
    model = AutoModelForCausalLM.from_pretrained(model_name, attn_implementation="sdpa", torch_dtype="cpu", trust_remote_code=True)

# model = AutoModelForCausalLM.from_pretrained(model_name,
#                                              trust_remote_code=True,
#                                              torch_dtype="auto",
#                                              _attn_implementation="eager",
#                                              offload_folder="offload",
#                                              offload_state_dict=True,
#                                          ).to(device)
tm_end = time.time()
print(f'Loaded in {tm_end - tm_start} seconds.')

def generate_response(input_text, image):
    prompt = f"{input_text}"

    inputs = processor(text=prompt, images=[image], return_tensors="pt")
    generated_ids = model.generate(
      input_ids=inputs["input_ids"].to(device),
      pixel_values=inputs["pixel_values"].to(device),
      max_new_tokens=1024,
      early_stopping=False,
      do_sample=False,
      num_beams=3,
    )
    generated_text = processor.batch_decode(generated_ids, skip_special_tokens=False)[0]
    parsed_answer = processor.post_process_generation(
        generated_text,
        task=input_text,
        image_size=(image.width, image.height)
    )

    return parsed_answer


print(generate_response('<MORE_DETAILED_CAPTION>',
                  Image.open('photos/Images/n02085620-Chihuahua/n02085620_7.jpg')
))