db.getSiblingDB('ImageSearch').getCollection('photos').createSearchIndex('default',
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
                ]
            }
        }
    }
);