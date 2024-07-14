//aggregate two collections together photos and photoinfo based on a $lookup using filename
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
