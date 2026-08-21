package com.nagy_mark.mygamevault.models;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class CheapSharkGameDetailResponse {
    @SerializedName("deals")
    private List<CheapSharkDealInfo> deals;

    public List<CheapSharkDealInfo> getDeals() {
        return deals;
    }
}