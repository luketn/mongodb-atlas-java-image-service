db.getSiblingDB('ImageSearch').getCollection('photo').dropSearchIndex('default');
db.getSiblingDB('ImageSearch').getCollection('photo').createSearchIndex('default',
INDEX_GOES_HERE
);