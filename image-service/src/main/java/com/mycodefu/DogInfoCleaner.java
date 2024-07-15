package com.mycodefu;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mycodefu.data.Colours;
import com.mycodefu.data.DogSize;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

import static com.mycodefu.data.Serializer.objectMapper;
import static java.util.Map.entry;

public class DogInfoCleaner {
    static Map<String, String> colourMap = Map.ofEntries(
            entry("Tan", "Brown"),
            entry("Beige", "Brown"),
            entry("Blonde", "Brown"),
            entry("Blond", "Brown"),
            entry("Brownish", "Brown"),
            entry("Brownish-Brown", "Brown"),
            entry("Brownish-Red", "Brown"),
            entry("Dark", "Brown"),
            entry("Reddish-Brown", "Brown"),
            entry("Yellowish-Brown", "Brown"),
            entry("Golden Brown", "Brown"),
            entry("Dark Brown", "Brown"),
            entry("Darker Brown", "Brown"),
            entry("Light Brown", "Brown"),
            entry("Light Tan", "Brown"),
            entry("Orange", "Brown"),
            entry("Red", "Brown"),
            entry("Yellow", "Brown"),
            entry("Cream", "White"),
            entry("Dark Grey", "Grey"),
            entry("Light Grey", "Grey"),
            entry("Light Gray", "Grey"),
            entry("Silver", "Grey"),
            entry("Gray", "Grey"),
            entry("Gold", "Golden")
    );


    //record for {"time_taken_seconds":7,"url":"https://image-search.mycodefu.com/photos/Images/n02091134-whippet/n02091134_19308.jpg","info":{"detailedCaption":"Two white whippets standing in a grassy area with trees in the background.","hasPerson":false,"dogs":[{"colour":["White"],"size":"Medium","breed":"Whippet"},{"colour":["White"],"size":"Small","breed":"Whippet"}]},"filename":"photos/Images/n02091134-whippet/n02091134_19308.jpg"}
    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private record Dog(String breed, DogSize size, List<String> colour) { }
    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private record Info(String detailedCaption, boolean hasPerson, List<Dog> dogs) { }
    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private record Photo(String url, Info info, String filename, int time_taken_seconds, String error) { }

    public static void main(String[] args) throws IOException {
        var jsonLines = Files.readAllLines(Paths.get("data-info.jsonl"));
        try (OutputStreamWriter writer = new OutputStreamWriter(Files.newOutputStream(Paths.get("data-info-cleaned.jsonl")))) {
            Set<String> coloursRemoved = new HashSet<>();
            Set<String> coloursMapped = new HashSet<>();
            for (var line : jsonLines) {
                Photo photo = objectMapper.readValue(line, Photo.class);
                if (photo.error != null) {
                    System.out.println("Error in " + photo.filename + ": " + photo.error);
                    continue;
                }

                Info updatedInfo;
                if (photo.info != null) {
                    if (photo.info.dogs == null) {
                        System.out.println("No dogs in " + photo.url);
                        updatedInfo = photo.info;
                    } else {

                        List<Dog> updatedDogs = new ArrayList<>();
                        for (Dog dog : photo.info.dogs) {
                            Set<String> coloursFiltered = new HashSet<>();
                            for (String colour : dog.colour) {
                                if (colourMap.containsKey(colour)) {
                                    coloursMapped.add(colour);
                                    colour = colourMap.get(colour);
                                }
                                if (!Colours.allowed.contains(colour)) {
                                    coloursRemoved.add(colour);

                                } else {
                                    coloursFiltered.add(colour);
                                }
                            }
                            if (!coloursFiltered.isEmpty()) {
                                Dog updatedDog = new Dog(dog.breed, dog.size, coloursFiltered.stream().toList());
                                updatedDogs.add(updatedDog);
                            } else {
                                updatedDogs.add(dog);
                            }
                        }
                        updatedInfo = new Info(photo.info.detailedCaption, photo.info.hasPerson, updatedDogs);
                    }
                } else {
                    updatedInfo = null;
                }
                Photo cleanPhoto = new Photo(photo.url, updatedInfo, photo.filename, photo.time_taken_seconds, photo.error);

                String cleanJson = objectMapper.writeValueAsString(cleanPhoto);
                writer.write(cleanJson + "\n");
            }
            System.out.println("Colours removed: " + coloursRemoved);
            System.out.println("Colours mapped: " + coloursMapped);
        }
    }
}
