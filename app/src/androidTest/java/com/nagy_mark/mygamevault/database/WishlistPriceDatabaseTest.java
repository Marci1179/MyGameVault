package com.nagy_mark.mygamevault.database;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import android.content.Context;

import androidx.room.Room;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class WishlistPriceDatabaseTest {
    private AppDatabase db;
    private WishlistPriceDao priceDao;

    @Before
    public void createDb() {
        Context context = ApplicationProvider.getApplicationContext();
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase.class).build();
        priceDao = db.wishlistPriceDao();
    }

    @After
    public void closeDb() {
        db.close();
    }

    @Test
    public void insertAndGetPrice() {
        WishlistPriceEntity price = new WishlistPriceEntity(999, 14.99, "Steam");
        priceDao.insertOrUpdatePrice(price);

        WishlistPriceEntity fetchedPrice = priceDao.getPriceForGame(999);
        assertNotNull("Az adatbázis nem adhat vissza null-t, ha sikeres volt a mentés", fetchedPrice);
        assertEquals(14.99, fetchedPrice.getLastKnownPrice(), 0.001);
        assertEquals("Steam", fetchedPrice.getStoreName());
    }

    @Test
    public void updateExistingPrice() {
        WishlistPriceEntity initialPrice = new WishlistPriceEntity(123, 19.99, "Ubisoft");
        priceDao.insertOrUpdatePrice(initialPrice);

        WishlistPriceEntity updatedPrice = new WishlistPriceEntity(123, 9.99, "Ubisoft");
        priceDao.insertOrUpdatePrice(updatedPrice);

        WishlistPriceEntity fetchedPrice = priceDao.getPriceForGame(123);
        assertNotNull(fetchedPrice);
        assertEquals(9.99, fetchedPrice.getLastKnownPrice(), 0.001);
    }

    @Test
    public void deletePrice() {
        WishlistPriceEntity price = new WishlistPriceEntity(555, 4.99, "Epic Games");
        priceDao.insertOrUpdatePrice(price);

        priceDao.deletePrice(555);

        WishlistPriceEntity fetchedAfterDelete = priceDao.getPriceForGame(555);
        assertNull("A törlés után az adatbázisnak null-t kell visszaadnia", fetchedAfterDelete);
    }
}
