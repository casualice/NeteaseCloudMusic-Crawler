package com.Crawler.controller;

import com.Crawler.repository.PlaylistRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class Playlist2Controller {
    @Autowired
    PlaylistRepository playlistRepository;

    @GetMapping({"/playlists2",""})
    public String playlists(Model model, @PageableDefault(size = 100, sort = "collectCount", direction = Sort.Direction.DESC) Pageable pageable){
        model.addAttribute("playlists2", playlistRepository.findAll(pageable));
        return "playlists2";
    }
}
