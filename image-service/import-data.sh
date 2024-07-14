# convert output of machine learning model to JSON Lines (jsonl)
cat ../image-data/photo-results.json | jq .photos | tr -d '\n' | jq -c '.[]' > data.jsonl
cat ../image-data/photo-results-info-temp.json | jq .photos | tr -d '\n' | jq -c '.[]' > data-info.jsonl

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

./merge-collections.sh