package com.Crawler.controller;

import com.Crawler.model.MusicComment;
import com.Crawler.repository.MusicCommentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class CommentController {
    @Autowired
    MusicCommentRepository musicCommentRepository;

    @GetMapping({"/Comments",""})
    public String Comment(Model model, @PageableDefault(size = 100, sort = "appreciation", direction = Sort.Direction.DESC) Pageable pageable){
        model.addAttribute("Comments", musicCommentRepository.findAll(pageable));
        return "Comments";

    }
}
