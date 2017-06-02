package com.Crawler.controller;

import com.Crawler.model.MusicCommentMessage;
import com.Crawler.model.Song;
import com.Crawler.model.pl;
import com.Crawler.repository.MusicCommentMessageRepository;
import com.Crawler.service.CrawlerService;
import com.Crawler.service.CrawlerThread;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
public class WebController {

    @Autowired
    MusicCommentMessageRepository musicCommentRepository;

    @RequestMapping("/")
    public String page(){
        return "index";
    }

    @RequestMapping("/get")
    public String get(){
        return "get";
    }

    @RequestMapping("/getpl")
    public String getpl(){
        return "getpl";
    }

    @RequestMapping("/crawl")
    public String crawl(){
        return "crawl";
    }

    @RequestMapping("/init")
    public String init(){
        return "init";
    }

    @RequestMapping("/songmsg")
    public String songmsg(@RequestParam("url") String url,Model model){
        CrawlerThread msg = new CrawlerThread();
        CrawlerService c = new CrawlerService();
        CrawlerThread msg2 = new CrawlerThread();
        try {
            MusicCommentMessage musicCommentMessage = msg.parseCommentMessage(url.split("=")[1]);
            model.addAttribute("songmsg",musicCommentMessage);
            musicCommentRepository.saveAndFlush(musicCommentMessage);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "/songmsg";
    }

    @RequestMapping("/plmsg")
    public String plmsg(@RequestParam("plurl") String url,Model model){
        CrawlerThread plmsg = new CrawlerThread();
        try {
            pl playlist = plmsg.parsePlaylist(url.split("=")[1]);
            List<Song> song = playlist.getSongs();
            model.addAttribute("plmsg",playlist);
        }catch (Exception e){
            e.printStackTrace();
        }
        return "/plmsg";
    }
}
