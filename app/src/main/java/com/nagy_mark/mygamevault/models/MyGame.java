package com.nagy_mark.mygamevault.models;

public class MyGame {
    private String game_name;
    private String release_year;
    private String publisher;
    private Float rating = null;
    private String note = null;
    private String cover;
    private int status_id;
    private String user_id;

    public MyGame(String game_name, String release_year, String publisher, String cover, int status_id, String user_id) {
        this.game_name = game_name;
        this.release_year = release_year;
        this.publisher = publisher;
        this.cover = cover;
        this.status_id = status_id;
        this.user_id = user_id;
    }
}
