package com.Crawler.model;

import javax.persistence.*;

@Entity
public class MusicComment {
    //评论类型

    public String songTitle;
    private String type;

    private String nickname;

    @Id
    private String content;
    //获赞数
    private Long appreciation;
    public MusicComment() {}

    public MusicComment(String songTitle,String type, String nickname,  String content, Long appreciation) {
        this.songTitle=songTitle;
        this.type = type;
        this.nickname = nickname;
        this.content = content;
        this.appreciation = appreciation;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }


    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Long getAppreciation() {
        return appreciation;
    }

    public void setAppreciation(Long appreciation) {
        this.appreciation = appreciation;
    }

    @Override
    public String toString() {
        return "评论用户:" + nickname + "<br>评论内容:"
                + content + "<br>点赞数:" + appreciation + "<br><br>";
    }
}
