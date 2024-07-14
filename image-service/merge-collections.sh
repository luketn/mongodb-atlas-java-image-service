# create the atlas search index
mongosh < merge-collections.js
if [ "$UPLOAD_TO_ATLAS" ]; then
  echo "Merging collections on Atlas..."
  mongosh $ATLAS_LUKE_CONNECTION_STRING < merge-collections.js
else
  echo "Not merging collections on Atlas."
fi

./create-index.sh

# example search: db.getCollection("photos").aggregate([
#    {$search: {
#  index: "default",
#  text: {
#    query: "french",
#    path: "caption",
#    fuzzy: {}
#  }
#}}
#])