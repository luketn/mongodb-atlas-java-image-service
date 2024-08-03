from transformers import AutoModelForCausalLM
from transformers import AutoProcessor
import os

model_id = "microsoft/Phi-3-vision-128k-instruct"
model = AutoModelForCausalLM.from_pretrained(model_id, trust_remote_code=True, torch_dtype="auto", _attn_implementation='eager') # use _attn_implementation='eager' to disable flash attention
processor = AutoProcessor.from_pretrained(model_id, trust_remote_code=True)

path = "./model"
if not os.path.exists(path):
    os.makedirs(path)

model.save_pretrained(path, safe_serialization=False)
processor.save_pretrained(path, safe_serialization=False)

print("Model and processor downloaded successfully!")