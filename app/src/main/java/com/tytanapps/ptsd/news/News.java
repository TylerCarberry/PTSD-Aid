package com.tytanapps.ptsd.news;

import android.support.annotation.NonNull;

import com.tytanapps.ptsd.Searchable;

import java.io.Serializable;

/**
 * A VA news article that has a title, message, pressId, and a published date
 */
public class News implements Comparable<News>, Serializable, Searchable {

    private static final long serialVersionUID = 1L;

    private String title;
    private String message;
    private int pressId;
    private String pressDate;

    /**
     * Create a new news
     * @param title The title of the article
     * @param message The body of the article
     * @param pressId Unique id of the article
     * @param pressDate The date the article was published
     */
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

    /**
     * Compare the pressId's of two News
     * The most recent one (News with the highest pressId) comes first
     * @param another The news article to compare to
     * @return -1 if this has a greater pressId, 0 if equal, 1 if less
     */
    @Override
    public int compareTo(@NonNull News another) {
        return 0-((Integer)this.getPressId()).compareTo(another.getPressId());
    }

    public boolean search(String searchTerm) {
        if(searchTerm == null || searchTerm.isEmpty())
            return true;
        searchTerm = searchTerm.toLowerCase();
        return  title.toLowerCase().contains(searchTerm) ||
                message.toLowerCase().contains(searchTerm) ||
                pressDate.toLowerCase().contains(searchTerm);

    }
}
