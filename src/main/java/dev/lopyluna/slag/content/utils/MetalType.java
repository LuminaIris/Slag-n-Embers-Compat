package dev.lopyluna.slag.content.utils;

import java.util.ArrayList;
import java.util.List;

public class MetalType {
    public static List<MetalType> metalTypesEntries = new ArrayList<>();

    public String id;
    public String name;
    public int damage;
    public int speed;

    public MetalType(String name, int damage, int speed) {
        this.name = name;
        this.id = name.toLowerCase().replace(" ", "_");
        this.damage = damage;
        this.speed = speed;
        metalTypesEntries.add(this);
    }
}
