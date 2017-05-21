package com.Crawler.controller;

import com.Crawler.service.CrawlerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.expression.AccessException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CrawlerController {
    
    @Autowired
    private CrawlerService crawlerService;
    
    @Value("${auth.key}")
    private String key;
    
    @ModelAttribute
    public void AuthConfig(@RequestParam String auth) throws AccessException {
        System.err.print("auth=================================="+auth);
        if(!key.equals(auth)) {
           throw new AccessException("auth failed"); 
        }
    }
    
    @GetMapping("/init")
    public void init() {
       System.out.print("初始化歌曲分类");
        crawlerService.init();
    }
    
    @GetMapping("/crawl")
    public void crawl() throws InterruptedException {
        System.out.print("开始抓取歌曲列表已经歌曲评论数");
        crawlerService.crawl();
    }
    
    @GetMapping("/update")
    @Scheduled(cron = "0 1 0 * * ?")
    public void update() throws InterruptedException {
        crawlerService.update();
    }
    
}
