# convert output of machine learning model to JSON Lines (jsonl)
cat ../image-data/photo-results.json | jq .photos | tr -d '\n' | jq -c '.[]' > data.jsonl
# import jsonl to mongo, dropping existing collection
mongoimport --db ImageSearch --collection photos --drop --file data.jsonl