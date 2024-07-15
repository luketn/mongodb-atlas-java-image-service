//aggregate two collections together photos and photoinfo based on a $lookup using filename

db.getSiblingDB('ImageSearch').getCollection('photo').drop()
db.getSiblingDB('ImageSearch').getCollection('photoinfo').createIndex(
    {
        filename: 1
    }
)
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
db.getSiblingDB('ImageSearch').getCollection('photos').drop();
db.getSiblingDB('ImageSearch').getCollection('photoinfo').drop();

//patch the collection with the url field based on combining a string field 'runData.filename' with a constant string https://image-search.mycodefu.com/
db.getSiblingDB('ImageSearch').getCollection("photo").updateMany({url: {$exists: false}},
    [
        {
            $set: {
                url: {
                    $concat: [
                        "https://image-search.mycodefu.com/",
                        "$runData.filename"
                    ]
                }
            }
        }
    ]
);
db.getSiblingDB('ImageSearch').getCollection("photo").updateMany({},
    [
        {
            $set: {
                url: {
                    $concat: [
                        "https://image-search.mycodefu.com/",
                        "$runData.filename"
                    ]
                },
                breeds: {
                    $reduce: {
                        input: "$dogs.breed",
                        initialValue: [],
                        in: { $setUnion: ["$$value", { $concatArrays: [["$$this"]] }] }
                    }
                },
                colours: {
                    $reduce: {
                        input: "$dogs.colour",
                        initialValue: [],
                        in: { $setUnion: ["$$value", "$$this"] }
                    }
                },
                sizes: {
                    $reduce: {
                        input: "$dogs.size",
                        initialValue: [],
                        in: { $setUnion: ["$$value", { $concatArrays: [["$$this"]] }] }
                    }
                }
            }
        }
    ]
);