package com.nagy_mark.mygamevault.models;

import com.google.gson.annotations.SerializedName;

public class CheapSharkDealInfo {
    @SerializedName("storeID")
    private String storeId;

    @SerializedName("price")
    private String price;

    public String getStoreId() {
        return storeId;
    }

    public String getPrice() {
        return price;
    }
}