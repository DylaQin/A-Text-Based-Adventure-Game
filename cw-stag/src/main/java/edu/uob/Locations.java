package edu.uob;


import java.util.TreeMap;

public class Locations extends GameEntity{

    TreeMap<String, String> artefacts;
    TreeMap<String, String> furniture;
    TreeMap<String, String> characters;
    TreeMap<String, Locations> destination;

    public Locations(String name, String description) {
        super(name, description);
        artefacts=new TreeMap<>();
        furniture=new TreeMap<>();
        characters=new TreeMap<>();
        destination=new TreeMap<>();
    }
}
