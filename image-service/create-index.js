//create search index on photo collection
db.getSiblingDB('ImageSearch').getCollection('photo').dropSearchIndex('default');

db.getSiblingDB('ImageSearch').getCollection('photo').createSearchIndex('default',
    {
        "mappings": {
            "fields": {
                "caption": [
                    {
                        "type": "string"
                    },
                    {
                        "type": "autocomplete"
                    }
                ],
                "summary": [
                    {
                        "type": "string"
                    }
                ],
                "hasPerson": {
                    "type": "boolean"
                },
                "breeds": [
                    {
                        "type": "token"
                    },
                    {
                        "type": "stringFacet"
                    }
                ],
                "colours": [
                    {
                        "type": "token"
                    },
                    {
                        "type": "stringFacet"
                    }
                ],
                "sizes": [
                    {
                        "type": "token"
                    },
                    {
                        "type": "stringFacet"
                    }
                ]
            }
        }
    }
);