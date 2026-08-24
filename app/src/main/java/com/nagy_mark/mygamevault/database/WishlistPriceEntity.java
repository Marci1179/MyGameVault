package com.nagy_mark.mygamevault.database;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "wishlist_prices")
public class WishlistPriceEntity {
    @PrimaryKey
    private int gameId;

    private double lastKnownPrice;

    private String storeName;

    public WishlistPriceEntity(int gameId, double lastKnownPrice, String storeName) {
        this.gameId = gameId;
        this.lastKnownPrice = lastKnownPrice;
        this.storeName = storeName;
    }

    public int getGameId() {
        return gameId;
    }

    public void setGameId(int gameId) {
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
