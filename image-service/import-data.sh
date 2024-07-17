# convert output of machine learning model to JSON Lines (jsonl)
gzip -dk ../image-data/photo-results.json.gz
gzip -dk ../image-data/photo-results-info.json.gz

cat ../image-data/photo-results.json | jq .photos | tr -d '\n' | jq -c '.[]' > data.jsonl
cat ../image-data/photo-results-info.json | jq .photos | tr -d '\n' | jq -c '.[]' > data-info.jsonl

# check the jar has been built, if not build it
if [ ! -f target/image-service.jar ]; then
  mvn clean package
fi

java -cp target/image-service.jar com.mycodefu.DogInfoCleaner
mv data-info.jsonl data-info-pre-cleaned.jsonl
mv data-info-cleaned.jsonl data-info.jsonl

# import jsonl to mongo, dropping existing collection
mongoimport --uri "mongodb://localhost" --db ImageSearch --collection photos --drop --file data.jsonl
mongoimport --uri "mongodb://localhost" --db ImageSearch --collection photoinfo --drop --file data-info.jsonl

if [ "$UPLOAD_TO_ATLAS" ]; then
  echo "Uploading data to Atlas..."
  mongoimport --uri $ATLAS_LUKE_CONNECTION_STRING --db ImageSearch --collection photos --drop --file data.jsonl
  mongoimport --uri $ATLAS_LUKE_CONNECTION_STRING --db ImageSearch --collection photoinfo --drop --file data-info.jsonl
else
  echo "Not uploading data to Atlas."
fi

# next step - merge the collections!
./merge-collections.sh