//aggregate two collections together photos and photoinfo based on a $lookup using filename
db.getSiblingDB('ImageSearch').getCollection('photos').aggregate([
    {
        $lookup: {
            from: 'photoinfo',
            localField: 'filename',
            foreignField: 'filename',
            as: 'info'
        }
    },
    {
        $unwind: {
            path: '$info',
            preserveNullAndEmptyArrays: true
        }
    },
    {
        $project: {
            _id: 1,
            url: '$info.url',
            caption: '$caption',
            summary: '$info.info.detailedCaption',
            hasPerson: '$info.info.hasPerson',
            dogs: '$info.info.dogs',
            runData: {
                filename: '$filename',
                captionTimeTakenSeconds: '$caption_time_seconds',
                infoTimeTakenSeconds: '$info.time_taken_seconds',
            }
        }
    },
    {
        $out: 'photo'
    }
]);
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
                },
            }
        }
    }
);