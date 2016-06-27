package com.tytanapps.ptsd;

/**
 * Created by Tyler on 6/27/16.
 */
public class News implements Comparable<News>{

    private String title;
    private String message;
    private int pressId;
    private String pressDate;

    public News(String title, String message, int pressId, String pressDate) {
        this.title = title;
        this.message = message;
        this.pressId = pressId;
        this.pressDate = pressDate;
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

    public int getPressId() {
        return pressId;
    }

    public void setPressId(int pressId) {
        this.pressId = pressId;
    }

    public String getPressDate() {
        return pressDate;
    }

    public void setPressDate(String pressDate) {
        this.pressDate = pressDate;
    }

    @Override
    public int compareTo(News another) {
        return 0-((Integer)this.getPressId()).compareTo(another.getPressId());
    }
}
