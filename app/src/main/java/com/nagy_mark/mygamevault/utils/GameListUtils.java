package com.nagy_mark.mygamevault.utils;

import com.nagy_mark.mygamevault.models.SavedGameModel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class GameListUtils {

    public static List<SavedGameModel> filterGames(List<SavedGameModel> allGames, String searchText, boolean favoritesOnly) {
        List<SavedGameModel> filteredList = new ArrayList<>();
        String query = searchText != null ? searchText.toLowerCase().trim() : "";

        for (SavedGameModel game : allGames) {
            boolean matchesSearch = true;

            if (!query.isEmpty()) {
                if (game.getGameName() == null || !game.getGameName().toLowerCase().contains(query)) {
                    matchesSearch = false;
                }
            }

            boolean matchesFavorite = true;
            if (favoritesOnly) {
                matchesFavorite = game.isFavorite();
            }

            if (matchesSearch && matchesFavorite) {
                filteredList.add(game);
            }
        }
        return filteredList;
    }

    public static void sortGames(List<SavedGameModel> games, int sortPosition) {
        Collections.sort(games, (g1, g2) -> {
            String name1 = g1.getGameName() != null ? g1.getGameName() : "";
            String name2 = g2.getGameName() != null ? g2.getGameName() : "";

            String year1 = g1.getReleaseYear() != null ? g1.getReleaseYear() : "";
            String year2 = g2.getReleaseYear() != null ? g2.getReleaseYear() : "";

            switch (sortPosition) {
                case 0: return name1.compareToIgnoreCase(name2); // Név A-Z
                case 1: return name2.compareToIgnoreCase(name1); // Név Z-A
                case 2: return year2.compareTo(year1);           // Év csökkenő (Legújabb)
                case 3: return year1.compareTo(year2);           // Év növekvő (Legrégebbi)
                default: return 0;
            }
        });
    }
}
