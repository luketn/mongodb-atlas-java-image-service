import json
import os
from datetime import datetime

from transformers import AutoProcessor, AutoModelForCausalLM
from PIL import Image


QUICK_RUN = False

model_id = 'microsoft/Florence-2-large'
model = AutoModelForCausalLM.from_pretrained(model_id, trust_remote_code=True).eval().cuda()
processor = AutoProcessor.from_pretrained(model_id, trust_remote_code=True)

# get some details about the current GPU
gpu_details = os.popen('nvidia-smi').read().split('\n')
machine_details = {
    'model_id': model_id,
    'gpu_details': gpu_details,
}

# open a JSON file with list of photos to process
path_data_raw = open("photos.json").read()
path_data = json.loads(path_data_raw)

# open a JSON file with list of photos _already_ processed
if os.path.exists("photo-results.json"):
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

def run_florence2(task_prompt, text_input=None):
    if text_input is None:
        prompt = task_prompt
    else:
        prompt = task_prompt + text_input
    inputs = processor(text=prompt, images=image, return_tensors="pt")
    generated_ids = model.generate(
      input_ids=inputs["input_ids"].cuda(),
      pixel_values=inputs["pixel_values"].cuda(),
      max_new_tokens=1024,
      early_stopping=False,
      do_sample=False,
      num_beams=3,
    )
    generated_text = processor.batch_decode(generated_ids, skip_special_tokens=False)[0]
    parsed_answer = processor.post_process_generation(
        generated_text,
        task=task_prompt,
        image_size=(image.width, image.height)
    )

    return parsed_answer


count_processed = 0
total_time_start = datetime.now()

# iterate all the photo objects in the JSON
for photo in path_data:
    filename = photo['filename']

    # skip if we have already processed this photo
    if filename in last_run_map:
        photo_last_run = last_run_map[filename]
        time_taken_seconds = photo_last_run['caption_time_seconds']
        if 'error' in photo_last_run:
            error = photo_last_run['error']
            caption = None
        elif 'caption' in photo_last_run:
            caption = photo_last_run['caption']
            error = None
    else:
        # continue if the file does not exist
        if not os.path.exists(filename):
            print("File does not exist:", filename)
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
            result = run_florence2('<MORE_DETAILED_CAPTION>')
            caption = result['<MORE_DETAILED_CAPTION>']
            # trim trailing and leading spaces
            caption = caption.strip()
            error = None
        except Exception as e:
            print(f"Error processing {filename}: {str(e)}")
            caption = None
            error = str(e)

        end = datetime.now()
        time_taken_seconds = round((end - start).total_seconds(), 2)


    photo_with_caption = {
        'caption_time_seconds': time_taken_seconds,
        'filename': filename,
    }
    if error is not None:
        photo_with_caption['error'] = error
    if caption is not None:
        photo_with_caption['caption'] = caption

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
