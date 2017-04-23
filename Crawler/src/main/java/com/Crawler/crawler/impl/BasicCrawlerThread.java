package com.Crawler.crawler.impl;

import com.Crawler.crawler.HtmlParser;
import com.Crawler.crawler.model.WebPage;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;


public class BasicCrawlerThread implements Runnable{

    private final HtmlParser htmlParser = new HtmlParser();
    private final BasicCrawler crawler;
    public BasicCrawlerThread(BasicCrawler basicCrawler) {
        super();
        this.crawler = basicCrawler;
    }

    @Override
    public void run() {
        crawler.initCrawlerList();
        WebPage webPage;
        while ((webPage = crawler.getUnCrawlPage()) != null) {
            if(WebPage.PageType.playlists.equals(webPage.getType())){
                crawler.addToCrawlList(htmlParser.parsePlaylists(webPage.getUrl()));
            }
            if(WebPage.PageType.playlist.equals(webPage.getType())){
                crawler.addToCrawlList(htmlParser.parsePlaylist(webPage.getUrl()));
            }
            if(WebPage.PageType.song.equals(webPage.getType())){
                System.out.println(webPage.getTitle()+" "+htmlParser.parseSong(webPage.getUrl()));;
            }
        }
    }
    public static final Integer MAX_THREADS = 20;

    public void runThreads() throws InterruptedException {
        ExecutorService executorService = Executors.newFixedThreadPool(MAX_THREADS);
        for(int i = 0; i < MAX_THREADS; i++) {
            executorService.execute(new BasicCrawlerThread(this.crawler));
        }
        executorService.shutdown();
        executorService.awaitTermination(Long.MAX_VALUE, TimeUnit.DAYS);
    }

    public  static<T> void main(String args[]){
        BasicCrawler crawler = new BasicCrawler();
        BasicCrawlerThread basiCrawlerThread = new BasicCrawlerThread(crawler);
        try {
            basiCrawlerThread.runThreads();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
