# create the atlas search index
mongosh < create-index.js

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