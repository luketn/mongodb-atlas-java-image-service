package com.mycodefu.data;

import java.util.List;

public record Photo(String summary, String url, Boolean hasPerson, List<Dog> dogs) {
}
