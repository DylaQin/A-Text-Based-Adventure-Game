package edu.uob;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

public class Players extends GameEntity{

    public TreeMap<String, String> inventory;
    public Integer health;
    Locations location;

    public Players(String name) {
        super(name, null);
        inventory = new TreeMap<>();
        health = 3;
    }
}
