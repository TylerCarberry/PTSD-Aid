package com.tytanapps.ptsd.va.news;

import android.support.annotation.NonNull;

import com.tytanapps.ptsd.va.Searchable;

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
        if (searchTerm == null || searchTerm.isEmpty())
            return true;
        searchTerm = searchTerm.toLowerCase();
        return  title.toLowerCase().contains(searchTerm) ||
                message.toLowerCase().contains(searchTerm) ||
                pressDate.toLowerCase().contains(searchTerm);

    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        News news = (News) o;

        if (pressId != news.pressId) return false;
        if (title != null ? !title.equals(news.title) : news.title != null) return false;
        if (message != null ? !message.equals(news.message) : news.message != null) return false;
        return pressDate != null ? pressDate.equals(news.pressDate) : news.pressDate == null;

    }

    @Override
    public int hashCode() {
        int result = title != null ? title.hashCode() : 0;
        result = 31 * result + (message != null ? message.hashCode() : 0);
        result = 31 * result + pressId;
        result = 31 * result + (pressDate != null ? pressDate.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "News{" + "title='" + title + '\'' + ", message='" + message + '\'' + ", pressId=" + pressId + ", pressDate='" + pressDate + '\'' + '}';
    }
}
