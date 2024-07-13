package com.mycodefu.atlas;

import com.mongodb.client.model.search.SearchOperator;
import com.mongodb.client.model.search.SearchPath;
import com.mongodb.client.model.search.TextSearchOperator;
import org.bson.BsonDateTime;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.regex.Pattern;

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

    public static SearchOperator queryStringOrWildcard(String key, String value) {
        SearchOperator search;
        String cleanValue = cleanWordForSearch(value);
        if (isWord(cleanValue)) {
            search = wildcard(key, cleanValue);
        } else {
            search = queryString(key, "\"" + cleanValue + "\"");
        }
        return SearchOperator.of(search);
    }

    public static Bson queryForList(String path, List<String> values) {
        SearchPath searchPath = SearchPath.fieldPath(path).multi("keywordAnalyzer");
        List<TextSearchOperator> shouldList = values.stream()
                .map(value -> SearchOperator.text(searchPath, value)).toList();
        return SearchOperator.compound().should(shouldList);
    }

    public static SearchOperator queryString(String key, String query) {
        Document queryStringDocument = new Document("queryString", new Document()
                .append("defaultPath", key)
                .append("query", query)
        );
        return SearchOperator.of(queryStringDocument);
    }

    public static SearchOperator wildcard(String key, String value) {
        String valueWithoutAsterix = value;
        if (value.startsWith("*")) {
            valueWithoutAsterix = value.substring(1);
        }
        if (value.endsWith("*")) {
            valueWithoutAsterix = valueWithoutAsterix.substring(0, valueWithoutAsterix.length() - 1);
        }

        return SearchOperator.of(new Document("wildcard", new Document()
                .append("path", key)
                .append("query", "*" + cleanWordForSearch(valueWithoutAsterix) + "*")
                .append("allowAnalyzedField", true)
        ));
    }

    static final Pattern WORD_PATTERN = Pattern.compile("^[a-zA-Z0-9_]+$");
    public static boolean isWord(String value) {
        return WORD_PATTERN.matcher(value).matches();
    }

    public static boolean isSpecialChar(char c) {
        char[] queryChars = new char[]{'+', '-', '&', '|', '{', '}', '!', '(', ')', '\"', '~', '*', '?', '\\'};
        for (char queryChar : queryChars) {
            if (c == queryChar) {
                return true;
            }
        }
        return false;
    }

    public static String cleanWordForSearch(String text) {
        StringBuilder cleanedText = new StringBuilder();
        for (char c : text.toCharArray()) {
            if (isSpecialChar(c)) {
                cleanedText.append('\\');
            }
            cleanedText.append(Character.toLowerCase(c));
        }
        return cleanedText.toString().trim();
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
