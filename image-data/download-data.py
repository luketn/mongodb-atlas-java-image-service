# use requests to download http://vision.stanford.edu/aditya86/ImageNetDogs/images.tar
#
import requests
import os
import tarfile
import shutil
import json
import sys
import time
from datetime import datetime
from PIL import Image

# set to True to only process a few images
QUICK_RUN = False
def download_and_extract_data(url, filename, extract_path, json_file_name="photos.json"):
    # set to True to only process a few images
    QUICK_RUN = False

    # download the ImageNetDogs dataset
    if not os.path.exists(filename):
        print(f"Downloading {url} to {filename}")
        r = requests.get(url, stream=True)
        with open(filename, 'wb') as f:
            shutil.copyfileobj(r.raw, f)
        print("Download complete")

    # extract the tar file
    if not os.path.exists(extract_path):
        print(f"Extracting {filename} to {extract_path}")
        with tarfile.open(filename) as tar:
            tar.extractall(extract_path)
        print("Extraction complete")

    # write a JSON file with list of photos to process
    print(f"Writing {json_file_name}")
    photos = []
    for root, dirs, files in os.walk(extract_path):
        for file in files:
            if file.endswith(".jpg"):
                photos.append({
                    'filename': os.path.join(root, file),
                })
    with open(json_file_name, "w") as f:
        f.write(json.dumps(photos, indent=4))
    print("Writing complete")


# Usage
download_and_extract_data("http://vision.stanford.edu/aditya86/ImageNetDogs/images.tar", "photos.tar", "photos", "photos.json")
#not needed: download_and_extract_data("http://vision.stanford.edu/aditya86/ImageNetDogs/annotation.tar", "annotations.tar", "annotations", "annotations.json")
