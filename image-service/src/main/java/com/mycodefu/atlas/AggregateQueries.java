package com.mycodefu.atlas;

import com.mongodb.client.model.Aggregates;
import com.mongodb.client.model.Field;
import org.bson.Document;
import org.bson.conversions.Bson;

public class AggregateQueries {
    public static final Bson id_to_string = Aggregates.addFields(
            new Field<>("id", new Document("$toString", "$_id"))
    );
    public static final Bson photos_projection = Aggregates.project(
            new Document()
                    .append("_id", 0)
                    .append("runData", 0)
    );
    public static final Bson limit_5 = Aggregates.limit(5);
}
