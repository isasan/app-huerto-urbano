package com.huertoapp.data;

import com.huertoapp.model.User;

import java.util.List;
import java.util.Map;

public class SeedingCalendarData {

    public record PlantInfo(
            String name,
            String emoji,
            List<Integer> sewingMonths,
            List<Integer> transplantMonths,
            List<Integer> harvestMonths,
            String difficulty,
            int daysToHarvest
    ) {}

    private static final List<PlantInfo> NORTE = List.of(
            new PlantInfo("Tomate", "🍅", List.of(2,3,4), List.of(4,5), List.of(7,8,9), "MEDIA", 90),
            new PlantInfo("Lechuga", "🥬", List.of(2,3,8,9), List.of(3,4,9,10), List.of(5,6,11,12), "FACIL", 60),
            new PlantInfo("Zanahoria", "🥕", List.of(3,4,5,8), List.of(), List.of(6,7,8,11), "FACIL", 75),
            new PlantInfo("Pimiento", "🌶️", List.of(2,3), List.of(4,5), List.of(7,8,9,10), "MEDIA", 100),
            new PlantInfo("Pepino", "🥒", List.of(3,4), List.of(5), List.of(7,8,9), "FACIL", 60),
            new PlantInfo("Calabacín", "🥗", List.of(3,4,5), List.of(5,6), List.of(6,7,8,9), "FACIL", 55),
            new PlantInfo("Espinacas", "🌿", List.of(2,3,8,9), List.of(3,4,9,10), List.of(4,5,10,11), "FACIL", 45),
            new PlantInfo("Cebolla", "🧅", List.of(1,2,3), List.of(3,4), List.of(6,7,8), "MEDIA", 120),
            new PlantInfo("Ajo", "🧄", List.of(10,11), List.of(), List.of(5,6), "FACIL", 180),
            new PlantInfo("Perejil", "🌱", List.of(3,4,5), List.of(4,5,6), List.of(5,6,7,8,9), "FACIL", 70),
            new PlantInfo("Albahaca", "🌿", List.of(3,4,5), List.of(5,6), List.of(6,7,8,9), "MEDIA", 60),
            new PlantInfo("Fresas", "🍓", List.of(2,3), List.of(3,4), List.of(5,6,7), "DIFICIL", 90)
    );

    private static final List<PlantInfo> SUR = List.of(
            new PlantInfo("Tomate", "🍅", List.of(8,9,10), List.of(10,11), List.of(1,2,3), "MEDIA", 90),
            new PlantInfo("Lechuga", "🥬", List.of(8,9,2,3), List.of(9,10,3,4), List.of(11,12,5,6), "FACIL", 60),
            new PlantInfo("Zanahoria", "🥕", List.of(9,10,11,2), List.of(), List.of(12,1,2,5), "FACIL", 75),
            new PlantInfo("Pimiento", "🌶️", List.of(8,9), List.of(10,11), List.of(1,2,3,4), "MEDIA", 100),
            new PlantInfo("Pepino", "🥒", List.of(9,10), List.of(11), List.of(1,2,3), "FACIL", 60),
            new PlantInfo("Calabacín", "🥗", List.of(9,10,11), List.of(11,12), List.of(12,1,2,3), "FACIL", 55),
            new PlantInfo("Espinacas", "🌿", List.of(8,9,2,3), List.of(9,10,3,4), List.of(10,11,4,5), "FACIL", 45),
            new PlantInfo("Cebolla", "🧅", List.of(7,8,9), List.of(9,10), List.of(12,1,2), "MEDIA", 120),
            new PlantInfo("Ajo", "🧄", List.of(4,5), List.of(), List.of(11,12), "FACIL", 180),
            new PlantInfo("Perejil", "🌱", List.of(9,10,11), List.of(10,11,12), List.of(11,12,1,2,3), "FACIL", 70),
            new PlantInfo("Albahaca", "🌿", List.of(9,10,11), List.of(11,12), List.of(12,1,2,3), "MEDIA", 60),
            new PlantInfo("Fresas", "🍓", List.of(8,9), List.of(9,10), List.of(11,12,1), "DIFICIL", 90)
    );

    public static List<PlantInfo> getPlants(User.Hemisphere hemisphere) {
        return hemisphere == User.Hemisphere.SUR ? SUR : NORTE;
    }
}
