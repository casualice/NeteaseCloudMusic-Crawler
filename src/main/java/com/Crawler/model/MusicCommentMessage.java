package com.Crawler.model;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;



/*歌曲相关信息POJO*/
@Entity
public class MusicCommentMessage {
    //歌名+歌手
    private String songTitle;

    //歌曲链接
    @Id
    private String songUrl;

    private int commentCount;

    //评论
    @OneToMany(cascade=CascadeType.ALL,fetch= FetchType.LAZY)
    private List<MusicComment> comments = new ArrayList<MusicComment>();


    public MusicCommentMessage() {}
    public MusicCommentMessage(String songTitle, String songUrl, int commentCount, List<MusicComment> comments) {
        this.songTitle = songTitle;
        this.songUrl = songUrl;
        this.commentCount = commentCount;
        this.comments = comments;
    }

    public String getSongTitle() {
        return songTitle;
    }

    public void setSongTitle(String songTitle) {
        this.songTitle = songTitle;
    }

    public String getSongUrl() {
        return songUrl;
    }

    public void setSongUrl(String songUrl) {
        this.songUrl = songUrl;
    }

    public int getCommentCount() {
        return commentCount;
    }

    public void setCommentCount(int commentCount) {
        this.commentCount = commentCount;
    }

    public void setComments(List<MusicComment> comments) {
        this.comments = comments;
    }

    public List<MusicComment> getComments() {
        return comments;
    }

    @Override
    public String toString() {
        return "歌名与歌手:" + songTitle + ", 歌曲url：" + songUrl + ", 评论数：" + commentCount
                + "\n评论：" + comments ;
    }
}
