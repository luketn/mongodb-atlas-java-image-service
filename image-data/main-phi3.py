import json
import os
import platform
from datetime import datetime

import torch
from transformers import AutoProcessor, AutoModelForCausalLM
from PIL import Image


QUICK_RUN = False

kwargs = {'torch_dtype': torch.bfloat16}

model_id = "microsoft/Phi-3-vision-128k-instruct"
processor = AutoProcessor.from_pretrained(model_id, trust_remote_code=True)
if torch.cuda.is_available():
    print("Running on NVIDIA GPU")
    device = torch.device("cuda", 0)
    model = AutoModelForCausalLM.from_pretrained(model_id, device_map="cuda", trust_remote_code=True, torch_dtype="auto", _attn_implementation="flash_attention_2").to(device)
    gpu_details = os.popen('nvidia-smi').read().split('\n')
# Phi3 does not support Apple M1 GPU (yet)
# elif torch.backends.mps.is_available() and torch.backends.mps.is_built():
#     print("Running on Apple M1 GPU")
#     device = torch.device("mps")
#     model = AutoModelForCausalLM.from_pretrained(model_name, device_map="mps", trust_remote_code=True, torch_dtype="auto", _attn_implementation="eager").to(device)
#     gpu_details = os.popen('system_profiler SPDisplaysDataType').read().split('\n')
else:
    print("Running on CPU. Expect __VERY__ slow performance.")
    device = torch.device("cpu")
    model = AutoModelForCausalLM.from_pretrained(model_id, device_map="cpu", trust_remote_code=True, torch_dtype="auto", _attn_implementation="eager").to(device)
    if platform.system() == 'Darwin':
        gpu_details = os.popen('system_profiler SPDisplaysDataType').read().split('\n')
    else:
        gpu_details = None

user_prompt = '<|user|>\n'
assistant_prompt = '<|assistant|>\n'
prompt_suffix = "<|end|>\n"

machine_details = {
    'model_id': model_id,
    'gpu_details': gpu_details,
}

# open a JSON file with list of photos to process
path_data_raw = open("photos.json").read()
path_data = json.loads(path_data_raw)

# open a JSON file with list of photos _already_ processed
if not QUICK_RUN and os.path.exists("photo-results.json"):
    last_run_captions_raw = open("photo-results.json").read()
    last_run_captions = json.loads(last_run_captions_raw)
    # move file to last-run{date}.json (overwriting any existing file)
    last_run_filename = "last-run-" + datetime.now().strftime("%Y%m%d-%H%M%S") + ".json"
    os.rename("photo-results.json", last_run_filename)

    # create a map from filename to photo object
    last_run_map = {}
    for photo in last_run_captions['photos']:
        last_run_map[photo['filename']] = photo
else:
    last_run_captions = None
    last_run_map = {}

photos_detailed_captions = []

folder = "photos/"
# Count the number of .jpg photos in the folder
num_photos = len(path_data)

def write_results_to_file():
    with open("photo-results.json", "w") as f:
        f.write(json.dumps({
            'machine_details': machine_details,
            'num_photos': num_photos,
            'photos': photos_detailed_captions
        }, indent=4))

print(f"Starting processing {len(path_data)} photos on machine:\n{json.dumps(machine_details, indent=4)}")
write_results_to_file()

def run_phi3_vision(task):
    prompt = f"{user_prompt}<|image_1|>\n{task}{prompt_suffix}{assistant_prompt}"
    inputs = processor(prompt, image, return_tensors="pt").to(device)

    generate_ids = model.generate(**inputs,
                                  max_new_tokens=1000,
                                  eos_token_id=processor.tokenizer.eos_token_id,
                                  )
    generate_ids = generate_ids[:, inputs['input_ids'].shape[1]:]

    response = processor.batch_decode(generate_ids,
                                      skip_special_tokens=True,
                                      clean_up_tokenization_spaces=False)[0]

    # strip out the ```{json} start line and ``` end line
    response = response.strip()
    response = response[response.find("{"):]
    response = response[:response.rfind("}") + 1]

    # read from json string to dict
    response = json.loads(response)

    return response


count_processed = 0
total_time_start = datetime.now()

# iterate all the photo objects in the JSON
for photo in path_data:
    filename = photo['filename']

    # skip if we have already processed this photo
    if filename in last_run_map:
        photo_last_run = last_run_map[filename]
        time_taken_seconds = photo_last_run['time_taken_seconds']
        if 'error' in photo_last_run:
            error = photo_last_run['error']
            info = None
            url = None
        elif 'info' in photo_last_run and 'url' in photo_last_run:
            info = photo_last_run['info']
            url = photo_last_run['url']
            error = None
        else:
            raise Exception("Invalid data in last run")
    else:
        # continue if the file does not exist
        if not os.path.exists(filename):
            print(f"Image file {filename} does not exist.")
            continue

        try:
            # if a png, convert to jpg
            if filename.endswith('.png'):
                # check we haven't already converted to jpg
                if os.path.exists(filename.replace('.png', '.jpg')):
                    filename = filename.replace('.png', '.jpg')
                else:
                    im = Image.open(filename)
                    im = im.convert('RGB')
                    im.save(filename.replace('.png', '.jpg'))
                    filename = filename.replace('.png', '.jpg')

            image = Image.open(filename)
        except Exception as e:
            print(f"Error opening {filename}: {str(e)}")
            continue

        # time the following
        start = datetime.now()
        try:
            # get the third segment of the filename '/' delimited
            breed_filename=filename.split('/')[2]
            # get the folder without the first segment, '-' delimited
            breed_filename=breed_filename.split('-')[1]
            breed_filename=breed_filename+'.jpg'

            message = f'Produce only JSON from the image "{breed_filename}" in the ImageInfo structure:\n' \
                   '```\n' \
                   'interface Dog {\n' \
                   '  colour: string[];\n' \
                   '  size: "Small" | "Medium" | "Large";\n' \
                   '  breed: string;\n' \
                   '}\n' \
                   'interface ImageInfo {\n' \
                   '  detailedCaption: string;\n' \
                   '  hasPerson: boolean;\n' \
                   '  dogs: Dog[];\n' \
                   '}\n' \
                   '```'

            info = run_phi3_vision(message)
            url = f'https://image-search.mycodefu.com/{filename}'
            error = None

            # fix capitalization of colour + breed
            if info and 'dogs' in info:
                for dog in info['dogs']:
                    if 'breed' in dog:
                        dog['breed'] = dog['breed'].title()
                    if 'colour' in dog:
                        fixed_colours = []
                        did_fix = False
                        for colour in dog['colour']:
                            fixed_colour = colour.title()
                            fixed_colours.append(fixed_colour)
                            if colour != fixed_colour:
                                did_fix = True
                        if did_fix:
                            dog['colour'] = fixed_colours


        except Exception as e:
            print(f"Error processing {filename}: {str(e)}")
            info = None
            url = None
            error = str(e)

        end = datetime.now()
        time_taken_seconds = round((end - start).total_seconds(), 2)


    photo_with_caption = {
        'time_taken_seconds': time_taken_seconds,
        'url': url,
    }
    if error is not None:
        photo_with_caption['error'] = error
    if info is not None:
        photo_with_caption['info'] = info

    photo_with_caption['filename'] = filename

    print(json.dumps(photo_with_caption))

    photos_detailed_captions.append(photo_with_caption)

    count_processed += 1
    if QUICK_RUN and count_processed > 10:
        break

    percentage_complete = round(count_processed / num_photos * 100, 2)
    time_taken_so_far_seconds = (datetime.now() - total_time_start).total_seconds()
    if percentage_complete < 0.01:
        time_remaining_string = '(too soon to estimate)'
    else:
        time_remaining_minutes = round((time_taken_so_far_seconds / percentage_complete * (100 - percentage_complete)) / 60, 2)
        time_remaining_string = str(round(time_remaining_minutes / 60, 2)) + " hrs"

    if count_processed % 10 == 0:
        write_results_to_file()
        print(f"Estimated time remaining: {time_remaining_string}, Processed {count_processed} of {num_photos} ({percentage_complete}%), Time taken so far: {round(time_taken_so_far_seconds)}s")

write_results_to_file()
