# convert output of machine learning model to JSON Lines (jsonl)
cat ../image-data/photo-results.json | jq .photos | tr -d '\n' | jq -c '.[]' > data.jsonl
cat ../image-data/photo-results-info-temp.json | jq .photos | tr -d '\n' | jq -c '.[]' > data-info.jsonl
# import jsonl to mongo, dropping existing collection
mongoimport --db ImageSearch --collection photos --drop --file data.jsonl
mongoimport --db ImageSearch --collection photoinfo --drop --file data-info.jsonl