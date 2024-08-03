package com.mycodefu.data;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * The full document stored in the database, including internal redundant fields only used for searching.
 */
public record PhotoFull(String summary, String caption, String url, Boolean hasPerson, List<Dog> dogs, List<String> colours, List<String> breeds, List<DogSize> sizes) {
    public PhotoFull(String summary, String caption, String url, Boolean hasPerson, List<Dog> dogs) {
        this(summary, caption, url, hasPerson, dogs, getColours(dogs), getBreeds(dogs), getSizes(dogs));
    }
    private static List<DogSize> getSizes(List<Dog> dogs) {
        Set<DogSize> sizes = new HashSet<>();
        if (dogs != null) {
            for (Dog dog : dogs) {
                sizes.add(dog.size());
            }
        }
        return List.copyOf(sizes);
    }

    private static List<String> getBreeds(List<Dog> dogs) {
        Set<String> breeds = new HashSet<>();
        if (dogs != null) {
            for (Dog dog : dogs) {
                breeds.add(dog.breed());
            }
        }
        return List.copyOf(breeds);
    }

    private static List<String> getColours(List<Dog> dogs) {
        Set<String> colours = new HashSet<>();
        if (dogs != null) {
            for (Dog dog : dogs) {
                colours.addAll(dog.colour());
            }
        }
        return List.copyOf(colours);
    }
}
