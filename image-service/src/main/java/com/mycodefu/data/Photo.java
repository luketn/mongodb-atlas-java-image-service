package com.mycodefu.data;

import java.util.List;

public record Photo(String summary, String caption, String url, Boolean hasPerson, List<Dog> dogs, List<String> colours, List<String> breeds, List<DogSize> sizes) {
}
