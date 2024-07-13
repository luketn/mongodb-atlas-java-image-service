package com.mycodefu.atlas;

import com.mongodb.client.model.search.SearchOperator;
import org.bson.BsonDateTime;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.time.Instant;
import java.util.Collection;
import java.util.List;

public class AtlasSearchUtils {

    public static SearchOperator fuzzyText(String path, String text) {
        return SearchOperator.of(new Document("text", new Document()
                .append("path", path)
                .append("query", text)
                .append("fuzzy", new Document()
//                        .append("maxEdits", 2)
//                        .append("prefixLength", 1)
//                        .append("maxExpansions", 100)
                )
        ));
    }

    public static SearchOperator in(String path, List<?> value) {
        return searchOperator("in", path, value);
    }

    public static SearchOperator isEqual(String path, Object value) {
        return searchOperator("equals", path, value);
    }

    public static SearchOperator searchOperator(String operator, String path, Object value) {
        return SearchOperator.of(new Document(operator, new Document()
                .append("path", path)
                .append("value", value)
        ));
    }

    public static SearchOperator range(String key, Instant from, Instant to) {
        Document rangeDocument = new Document()
                .append("path", key)
                .append("gte", new BsonDateTime(from.toEpochMilli()));

        if (to != null) {
            rangeDocument.append("lte", new BsonDateTime(to.toEpochMilli()));
        }
        return SearchOperator.of(new Document("range", rangeDocument));
    }

    public static Document range(String key, Long from, Long to) {
        Document rangeDocument = new Document()
                .append("path", key)
                .append("gte", from);

        if (to != null) {
            rangeDocument.append("lte", to);
        }
        return new Document("range", rangeDocument);
    }

    public static <T> SearchOperator mustClauseForCollection(String path, Collection<T> values) {
        Bson clause;
        if (values.size() == 1) {
            switch (values.iterator().next()) {
                case String s -> clause = isEqual(path, s);
                case Number n -> clause = isEqual(path, n);
                case Boolean b -> clause = isEqual(path, b);
                case Object o -> throw new IllegalArgumentException("Unsupported type: " + o.getClass());
            }
        } else {
            clause = new Document("in", new Document()
                    .append("path", path)
                    .append("value", values)
            );
        }
        return SearchOperator.of(clause);
    }
}
