package com.nagy_mark.mygamevault.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.nagy_mark.mygamevault.models.SavedGameModel;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class GameListUtilsTest {
    private List<SavedGameModel> testGames;

    @Before
    public void setUp() {
        testGames = new ArrayList<>();

        SavedGameModel game1 = new SavedGameModel();
        game1.setGameName("A Way Out");
        game1.setReleaseYear("2018");
        game1.setFavorite(true);

        SavedGameModel game2 = new SavedGameModel();
        game2.setGameName("Far Cry 3");
        game2.setReleaseYear("2012");
        game2.setFavorite(false);

        SavedGameModel game3 = new SavedGameModel();
        game3.setGameName("It Takes Two");
        game3.setReleaseYear("2021");
        game3.setFavorite(true);

        SavedGameModel game4 = new SavedGameModel();

        testGames.add(game1);
        testGames.add(game2);
        testGames.add(game3);
        testGames.add(game4);
    }

    @Test
    public void testFilterGames_EmptySearch_ReturnsAll() {
        List<SavedGameModel> result = GameListUtils.filterGames(testGames, "", false);
        assertEquals("Üres keresésnél minden játéknak meg kell maradnia", 4, result.size());
    }

    @Test
    public void testFilterGames_BySearchText_CaseInsensitive() {
        List<SavedGameModel> result = GameListUtils.filterGames(testGames, "WAY", false);
        assertEquals(1, result.size());
        assertEquals("A Way Out", result.get(0).getGameName());
    }

    @Test
    public void testFilterGames_FavoritesOnly() {
        List<SavedGameModel> result = GameListUtils.filterGames(testGames, "", true);
        assertEquals("Csak 2 kedvenc játék van", 2, result.size());
        assertTrue(result.get(0).isFavorite());
        assertTrue(result.get(1).isFavorite());
    }

    @Test
    public void testFilterGames_SearchAndFavorite() {
        List<SavedGameModel> result = GameListUtils.filterGames(testGames, "cry", true);
        assertEquals("A Far Cry 3 benne van a nevében, de nem kedvenc, így 0 kell legyen", 0, result.size());
    }

    @Test
    public void testSortGames_NameAscending() {
        GameListUtils.sortGames(testGames, 0);

        assertNull(testGames.get(0).getGameName());
        assertEquals("A Way Out", testGames.get(1).getGameName());
        assertEquals("Far Cry 3", testGames.get(2).getGameName());
        assertEquals("It Takes Two", testGames.get(3).getGameName());
    }

    @Test
    public void testSortGames_NameDescending() {
        GameListUtils.sortGames(testGames, 1);

        assertEquals("It Takes Two", testGames.get(0).getGameName());
        assertEquals("Far Cry 3", testGames.get(1).getGameName());
        assertEquals("A Way Out", testGames.get(2).getGameName());
        assertNull(testGames.get(3).getGameName());
    }

    @Test
    public void testSortGames_YearDescending() {
        GameListUtils.sortGames(testGames, 2);

        assertEquals("2021", testGames.get(0).getReleaseYear());
        assertEquals("2018", testGames.get(1).getReleaseYear());
        assertEquals("2012", testGames.get(2).getReleaseYear());
        assertNull(testGames.get(3).getReleaseYear());
    }

    @Test
    public void testSortGames_YearAscending() {
        GameListUtils.sortGames(testGames, 3);

        assertNull(testGames.get(0).getReleaseYear());
        assertEquals("2012", testGames.get(1).getReleaseYear());
        assertEquals("2018", testGames.get(2).getReleaseYear());
        assertEquals("2021", testGames.get(3).getReleaseYear());
    }
}
