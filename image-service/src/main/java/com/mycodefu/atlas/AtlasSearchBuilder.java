package com.mycodefu.atlas;

import com.mongodb.client.model.search.CompoundSearchOperator;
import com.mongodb.client.model.search.SearchOperator;
import com.mongodb.client.model.search.SearchOptions;
import com.mycodefu.data.TextMode;
import org.bson.conversions.Bson;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.mongodb.client.model.Aggregates.*;
import static com.mongodb.client.model.search.SearchCollector.facet;
import static com.mongodb.client.model.search.SearchFacet.stringFacet;
import static com.mongodb.client.model.search.SearchOperator.*;
import static com.mongodb.client.model.search.SearchPath.fieldPath;
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

    public List<Bson> buildForFacetCounts(Bson... additionalClauses) {
        Bson searchClause = searchMeta(facet(
                of(buildCompoundQuery()),
                List.of(
                        stringFacet("colours", fieldPath("colours")).numBuckets(1000),
                        stringFacet("breeds", fieldPath("breeds")).numBuckets(1000),
                        stringFacet("sizes", fieldPath("sizes")).numBuckets(1000)
                )
        ), SearchOptions.searchOptions().index("default"));

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

    public AtlasSearchBuilder withCaption(String caption, TextMode mode) {
        if (caption != null && !caption.isEmpty()) {
            SearchOperator clause = switch(mode) {
                case Fuzzy -> fuzzyText("caption", caption);
                case QueryString -> queryString("caption", caption);
                case WildCard -> wildcard("caption", caption);
                case Phrase -> phrase("caption", caption);
            };
            must.add(clause);
        }
        return this;
    }

    public AtlasSearchBuilder withSummary(String summary, TextMode mode) {
        if (summary != null && !summary.isEmpty()) {
            SearchOperator clause = switch(mode) {
                case Fuzzy -> fuzzyText("summary", summary);
                case QueryString -> queryString("summary", summary);
                case WildCard -> wildcard("summary", summary);
                case Phrase -> phrase("summary", summary);
            };
            must.add(clause);
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
            for (String colour : colours) {
                must.add(isEqual("colours", colour));
            }
        }
        return this;
    }

    public AtlasSearchBuilder withBreeds(List<String> breeds) {
        if (breeds != null && !breeds.isEmpty()) {
            for (String breed : breeds) {
                must.add(isEqual("breeds", breed));
            }
        }
        return this;
    }

    public AtlasSearchBuilder withSizes(List<String> sizes) {
        if (sizes != null && !sizes.isEmpty()) {
            for (String size : sizes) {
                must.add(isEqual("sizes", size));
            }
        }
        return this;
    }
}
