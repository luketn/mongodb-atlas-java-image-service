package com.mycodefu.data;

import org.bson.Document;

import java.util.List;

public record PhotoResults(List<Photo> photos, Document facets) { }
