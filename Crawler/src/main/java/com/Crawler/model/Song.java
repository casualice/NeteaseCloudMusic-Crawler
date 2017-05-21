package com.Crawler.model;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class Song {
    
    @Id
    private String url;
    private String title;
    private Long commentCount;
    
    public Song() {
        super();
    }
    
    public Song(String url, String title, Long commentCount) {
        super();
        this.url = url;
        this.title = title;
        this.commentCount = commentCount;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Long getCommentCount() {
        return commentCount;
    }

    public void setCommentCount(Long commentCount) {
        this.commentCount = commentCount;
    }

    @Override
    public String toString() {
        return "Song [url=" + url + ", title=" + title + ", commentCount=" + commentCount + "]";
    }

}
