package com.Crawler.crawler.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.Crawler.crawler.Crawler;
import com.Crawler.crawler.HtmlParser;
import com.Crawler.crawler.model.Song;
import com.Crawler.crawler.model.WebPage;

public class BasicCrawler implements Crawler {
    
    private final HtmlParser htmlParser = new HtmlParser();
    public List<WebPage> crawlerList;
    public List<Song> songs = new ArrayList<>();

    @Override
    public void initCrawlerList() {
        crawlerList = new ArrayList<WebPage>();
        for(int i = 0; i < 43; i++) {
            crawlerList.add(new WebPage("http://music.163.com/discover/playlist/?order=hot&cat=%E5%85%A8%E9%83%A8&limit=35&offset="  + (i * 35), WebPage.PageType.playlists));
        }
        crawlerList.add(new WebPage("http://music.163.com/playlist?id=454016843", WebPage.PageType.playlist));
    }

    @Override
    public WebPage getUnCrawlPage() {
        if(crawlerList.size()>0) {
            if (crawlerList.get(crawlerList.size() - 1).getStatus().equals(WebPage.Status.uncrawl)) {
                WebPage w = crawlerList.get(crawlerList.size() - 1);
                crawlerList.remove(crawlerList.size() - 1);
                return w;
            } else {
                return null;
            }
        }
        else
            return null;
    }

    @Override
    public List<WebPage> addToCrawlList(List<WebPage> webPages) {
        for (int i=0;i<webPages.size();i++)
            crawlerList.add(webPages.get(i));
        return null;
    }

    @Override
    public Song saveSong(Song song) {
        songs.add(song);
        return null;
    }

    @Override
    public void doRun() {
        WebPage webPage;
        while ((webPage = getUnCrawlPage()) != null) {
            if(WebPage.PageType.playlists.equals(webPage.getType())){
                addToCrawlList(htmlParser.parsePlaylists(webPage.getUrl()));
            }
            if(WebPage.PageType.playlist.equals(webPage.getType())){
                addToCrawlList(htmlParser.parsePlaylist(webPage.getUrl()));
            }
            if(WebPage.PageType.song.equals(webPage.getType())){
                System.out.println(webPage.getTitle()+" "+htmlParser.parseSong(webPage.getUrl()));;
            }
        }
    }

    @Override
    public List<Song> getSongs() {
        songs.get(songs.size());
        return null;
    }

    public static <T> void main(String[] args) throws Exception {
        Date startTime = new Date();
        Crawler crawler = new BasicCrawler();
        crawler.doRun();
//        for(Song song : crawler.getSongs()) {
//            System.out.println(song);
//        }
        System.out.println("花费时间：" + (new Date().getTime() - startTime.getTime()));
    }
    
}
