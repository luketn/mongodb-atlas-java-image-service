package com.mycodefu.data;

import java.util.List;

public record Dog(List<String> colour, String breed, DogSize size) {
}
