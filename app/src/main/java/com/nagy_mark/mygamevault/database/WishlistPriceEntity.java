package com.nagy_mark.mygamevault.database;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "wishlist_prices")
public class WishlistPriceEntity {
    @PrimaryKey
    private int gameId;

    private double lastKnownPrice;

    public WishlistPriceEntity(int gameId, double lastKnownPrice) {
        this.gameId = gameId;
        this.lastKnownPrice = lastKnownPrice;
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
}
