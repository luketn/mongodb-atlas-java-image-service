# create the atlas search index
INDEX="$(cat create-index.json)"
INDEX=$(echo $INDEX | tr -d '\n')
INDEX=$(echo $INDEX | tr -d ' ')
SCRIPT_WITH_INDEX="$(sed -e "s/INDEX_GOES_HERE/$INDEX/g" create-index.js)"

echo "Creating index with script:\n${SCRIPT_WITH_INDEX}"
echo $SCRIPT_WITH_INDEX | mongosh
