package com.Crawler.repository;

import com.Crawler.model.MusicComment;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Created by 40383 on 2017/5/28.
 */
public interface MusicCommentRepository extends JpaRepository<MusicComment, String> {
}
