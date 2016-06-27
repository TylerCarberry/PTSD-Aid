package com.tytanapps.ptsd;

/**
 * Created by Tyler on 6/27/16.
 */
public class News {

    private String title;
    private String message;

    public News(String title, String message) {
        this.title = title;
        this.message = message;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
