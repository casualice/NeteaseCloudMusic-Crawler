package com.Crawler.repository;

import com.Crawler.model.MusicCommentMessage;
import org.springframework.data.jpa.repository.JpaRepository;


public interface MusicCommentMessageRepository extends JpaRepository<MusicCommentMessage, String> {
    MusicCommentMessage findOne(String songUrl);
}
