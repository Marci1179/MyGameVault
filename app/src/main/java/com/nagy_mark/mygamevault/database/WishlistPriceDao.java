package com.nagy_mark.mygamevault.database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

@Dao
public interface WishlistPriceDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertOrUpdatePrice(WishlistPriceEntity priceEntity);

    @Query("SELECT * FROM wishlist_prices WHERE gameId = :gameId LIMIT 1")
    WishlistPriceEntity getPriceForGame(int gameId);

    @Query("DELETE FROM wishlist_prices WHERE gameId = :gameId")
    void deletePrice(int gameId);
}
