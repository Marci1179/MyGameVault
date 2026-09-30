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
        SavedGameModel.GameDataNested data1 = new SavedGameModel.GameDataNested();
        data1.gameName = "A Way Out";
        data1.releaseDate = "2018-03-23";
        game1.setGameData(data1);
        game1.setFavorite(true);
        game1.setStatusId(1);

        SavedGameModel game2 = new SavedGameModel();
        SavedGameModel.GameDataNested data2 = new SavedGameModel.GameDataNested();
        data2.gameName = "Far Cry 3";
        data2.releaseDate = "2012-11-29";
        game2.setGameData(data2);
        game2.setFavorite(false);
        game2.setStatusId(2);

        SavedGameModel game3 = new SavedGameModel();
        SavedGameModel.GameDataNested data3 = new SavedGameModel.GameDataNested();
        data3.gameName = "It Takes Two";
        data3.releaseDate = "2021-03-26";
        game3.setGameData(data3);
        game3.setFavorite(true);
        game3.setStatusId(3);

        SavedGameModel game4 = new SavedGameModel();
        game4.setStatusId(1);

        testGames.add(game1);
        testGames.add(game2);
        testGames.add(game3);
        testGames.add(game4);
    }

    @Test
    public void testFilterGames_EmptySearch_ReturnsAll() {
        List<SavedGameModel> result = GameListUtils.filterGames(testGames, "", false, 0);
        assertEquals("Üres keresésnél minden játéknak meg kell maradnia", 4, result.size());
    }

    @Test
    public void testFilterGames_BySearchText_CaseInsensitive() {
        List<SavedGameModel> result = GameListUtils.filterGames(testGames, "WAY", false, 0);
        assertEquals(1, result.size());
        assertEquals("A Way Out", result.get(0).getGameName());
    }

    @Test
    public void testFilterGames_FavoritesOnly() {
        List<SavedGameModel> result = GameListUtils.filterGames(testGames, "", true, 0);
        assertEquals("Csak 2 kedvenc játék van", 2, result.size());
        assertTrue(result.get(0).isFavorite());
        assertTrue(result.get(1).isFavorite());
    }

    @Test
    public void testFilterGames_SearchAndFavorite() {
        List<SavedGameModel> result = GameListUtils.filterGames(testGames, "cry", true, 0);
        assertEquals("A Far Cry 3 benne van a nevében, de nem kedvenc, így 0 kell legyen", 0, result.size());
    }

    @Test
    public void testFilterGames_ByStatusOwned() {
        List<SavedGameModel> result = GameListUtils.filterGames(testGames, "", false, 1);
        assertEquals("Két birtokolt (status = 1) játék van a listában", 2, result.size());
        assertEquals(1, result.get(0).getStatusId());
    }

    @Test
    public void testFilterGames_SearchAndStatus() {
        List<SavedGameModel> result = GameListUtils.filterGames(testGames, "cry", false, 2);
        assertEquals(1, result.size());
        assertEquals("Far Cry 3", result.get(0).getGameName());
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
    public void testSortGames_DateDescending() {
        GameListUtils.sortGames(testGames, 2);

        assertEquals("2021-03-26", testGames.get(0).getReleaseDate());
        assertEquals("2018-03-23", testGames.get(1).getReleaseDate());
        assertEquals("2012-11-29", testGames.get(2).getReleaseDate());
        assertNull(testGames.get(3).getReleaseDate());
    }

    @Test
    public void testSortGames_DateAscending() {
        GameListUtils.sortGames(testGames, 3);

        assertNull(testGames.get(0).getReleaseDate());
        assertEquals("2012-11-29", testGames.get(1).getReleaseDate());
        assertEquals("2018-03-23", testGames.get(2).getReleaseDate());
        assertEquals("2021-03-26", testGames.get(3).getReleaseDate());
    }
}
