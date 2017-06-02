package com.Crawler.repository;

import com.Crawler.model.Playlist;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Created by 40383 on 2017/5/29.
 */
public interface PlaylistRepository extends JpaRepository<Playlist, String> {

}
