package com.mycodefu.atlas;

import com.mongodb.client.model.search.CompoundSearchOperator;
import com.mongodb.client.model.search.SearchOperator;
import com.mongodb.client.model.search.SearchOptions;
import org.bson.conversions.Bson;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.mongodb.client.model.Aggregates.*;
import static com.mongodb.client.model.search.SearchOperator.*;
import static com.mycodefu.atlas.AtlasSearchUtils.*;

public class AtlasSearchBuilder {
    final List<SearchOperator> must = new ArrayList<>();
    final List<SearchOperator> mustNot = new ArrayList<>();
    final List<SearchOperator> should = new ArrayList<>();

    public static AtlasSearchBuilder builder() {
        return new AtlasSearchBuilder();
    }

    public List<Bson> build(Bson... additionalClauses) {
        Bson searchClause = search(
                of(buildCompoundQuery()),
                SearchOptions.searchOptions().index("default")
        );

        List<Bson> clauses = new ArrayList<>();
        clauses.add(searchClause);
        clauses.addAll(Arrays.asList(additionalClauses));
        return clauses;
    }

    private Bson buildCompoundQuery() {
        CompoundSearchOperator result = compound().must(must);
        if (!mustNot.isEmpty()) {
            result = result.mustNot(mustNot);
        }
        if (!should.isEmpty()) {
            result = result.should(should).minimumShouldMatch(1);
        }
        return result;
    }

    public AtlasSearchBuilder withCaption(String caption) {
        if (caption != null && !caption.isEmpty()) {
            must.add(fuzzyText("caption", caption));
        }
        return this;
    }

    public AtlasSearchBuilder withSummary(String summary) {
        if (summary != null && !summary.isEmpty()) {
            must.add(phrase("summary", summary));
        }
        return this;
    }

    public AtlasSearchBuilder hasPerson(Boolean hasPerson) {
        if (hasPerson != null) {
            must.add(isEqual("hasPerson", hasPerson));
        }
        return this;
    }

    public AtlasSearchBuilder withColours(List<String> colours) {
        if (colours != null && !colours.isEmpty()) {
            must.add(in("colours", colours));
        }
        return this;
    }

    public AtlasSearchBuilder withBreeds(List<String> breeds) {
        if (breeds != null && !breeds.isEmpty()) {
            must.add(in("breeds", breeds));
        }
        return this;
    }

    public AtlasSearchBuilder withSizes(List<String> sizes) {
        if (sizes != null && !sizes.isEmpty()) {
            must.add(in("sizes", sizes));
        }
        return this;
    }
}
