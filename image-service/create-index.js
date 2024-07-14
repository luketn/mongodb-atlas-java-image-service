//create search index on photo collection
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
                "dogs": {
                    "type": "embeddedDocuments",
                    "dynamic": false,
                    "fields": {
                        "colour": {
                            "type": "string",
                            "multi": {
                                "keywordAnalyzer": {
                                    "type": "string",
                                    "analyzer": "lucene.keyword"
                                }
                            }
                        },
                        "size": {
                            "type": "string",
                            "multi": {
                                "keywordAnalyzer": {
                                    "type": "string",
                                    "analyzer": "lucene.keyword"
                                }
                            }
                        },
                        "breed": {
                            "type": "string",
                            "multi": {
                                "keywordAnalyzer": {
                                    "type": "string",
                                    "analyzer": "lucene.keyword"
                                }
                            }
                        }
                    }
                }
            }
        }
    }
);