package com.mycodefu.util;

import com.mycodefu.data.Colours;
import com.mycodefu.data.DogSize;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

import static com.mycodefu.util.Serializer.fromJson;
import static com.mycodefu.util.Serializer.toJson;
import static java.util.Map.entry;

public class DogInfoCleaner {
    static Map<String, String> colourMap = Map.ofEntries(
            entry("Black And White", "Black"),
            entry("Dark Brown", "Black"),
            entry("Darker Brown", "Black"),
            entry("Dark", "Black"),
            entry("Dark Grey", "Black"),
            entry("Dark Gray", "Black"),
            entry("Tan", "Brown"),
            entry("Beige", "Brown"),
            entry("Blonde", "Brown"),
            entry("Blond", "Brown"),
            entry("Brownish", "Brown"),
            entry("Brownish-Brown", "Brown"),
            entry("Brownish-Red", "Brown"),
            entry("Reddish-Brown", "Brown"),
            entry("Brownish-Orange", "Brown"),
            entry("Brown And White", "Brown"),
            entry("Yellowish-Brown", "Brown"),
            entry("Golden Brown", "Brown"),
            entry("Golden-Brown", "Brown"),
            entry("Light Brown", "Brown"),
            entry("Light Tan", "Brown"),
            entry("Orange", "Brown"),
            entry("Red", "Brown"),
            entry("Yellow", "Brown"),
            entry("Chestnut", "Brown"),
            entry("Cream", "White"),
            entry("Bluish-Grey", "Grey"),
            entry("Light Grey", "Grey"),
            entry("Light Gray", "Grey"),
            entry("Silver", "Grey"),
            entry("Gray", "Grey"),
            entry("Gold", "Golden"),
            entry("Light Golden", "Golden")
    );


    //record for {"time_taken_seconds":7,"url":"https://image-search.mycodefu.com/photos/Images/n02091134-whippet/n02091134_19308.jpg","info":{"detailedCaption":"Two white whippets standing in a grassy area with trees in the background.","hasPerson":false,"dogs":[{"colour":["White"],"size":"Medium","breed":"Whippet"},{"colour":["White"],"size":"Small","breed":"Whippet"}]},"filename":"photos/Images/n02091134-whippet/n02091134_19308.jpg"}
    private record Dog(String breed, DogSize size, List<String> colour) { }
    private record Info(String detailedCaption, boolean hasPerson, List<Dog> dogs) { }
    private record Photo(String url, Info info, String filename, int time_taken_seconds, String error) { }

    public static void main(String[] args) throws IOException {
        var jsonLines = Files.readAllLines(Paths.get("data-info.jsonl"));
        try (OutputStreamWriter writer = new OutputStreamWriter(Files.newOutputStream(Paths.get("data-info-cleaned.jsonl")))) {
            Set<String> coloursRemoved = new HashSet<>();
            Set<String> coloursMapped = new HashSet<>();
            for (var line : jsonLines) {
                Photo photo = fromJson(line, Photo.class);
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

                String cleanJson = toJson(cleanPhoto);
                writer.write(cleanJson + "\n");
            }
            System.out.println("Colours removed: " + coloursRemoved);
            System.out.println("Colours mapped: " + coloursMapped);
        }
    }
}
