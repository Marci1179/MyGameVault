package com.nagy_mark.mygamevault.database;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "wishlist_prices")
public class WishlistPriceEntity {
    @PrimaryKey
    private long gameId;

    private double lastKnownPrice;

    private String storeName;

    public WishlistPriceEntity(long gameId, double lastKnownPrice, String storeName) {
        this.gameId = gameId;
        this.lastKnownPrice = lastKnownPrice;
        this.storeName = storeName;
    }

    public long getGameId() {
        return gameId;
    }

    public void setGameId(long gameId) {
        this.gameId = gameId;
    }

    public double getLastKnownPrice() {
        return lastKnownPrice;
    }

    public void setLastKnownPrice(double lastKnownPrice) {
        this.lastKnownPrice = lastKnownPrice;
    }

    public String getStoreName() {
        return storeName;
    }

    public void setStoreName(String storeName) {
        this.storeName = storeName;
    }
}
