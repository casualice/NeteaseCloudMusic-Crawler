package com.Crawler.crawler.impl;

import com.Crawler.crawler.Crawler;
import com.Crawler.crawler.CrawlerThread;
import com.google.common.collect.Lists;
import com.Crawler.crawler.model.Song;
import com.Crawler.crawler.model.WebPage;
import com.Crawler.crawler.model.WebPage.PageType;
import com.Crawler.crawler.model.WebPage.Status;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Ehcache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static sun.security.krb5.internal.ccache.CredentialsCache.cacheName;

public class CrawlerService implements Crawler {
    
    private final CrawlerThread htmlParser = new CrawlerThread();
    public List<WebPage> crawlerList;
    public List<Song> songs = new ArrayList<>();
    public static final Integer MAX_THREADS = 20;
    private final CacheManager cacheManager;
    private final String cacheName = "com.crawler.Songs";



    public CrawlerService() {
        cacheManager = CacheManager.getInstance();
    }
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

    @Async
    public void crawl() throws InterruptedException {
        ExecutorService executorService = Executors.newFixedThreadPool(MAX_THREADS);
        //  ExecutorService executorService = Executors.newSingleThreadExecutor();
        for(int i = 0; i < MAX_THREADS; i++) {
            executorService.execute(new CrawlerThread(this));
        }
        executorService.shutdown();
        executorService.awaitTermination(Long.MAX_VALUE, TimeUnit.DAYS);
        Ehcache ehcache = cacheManager.getEhcache(cacheName);
        ehcache.removeAll();
    }

    @Async
    public void init() {
        init("全部");
        init("华语");
        init("欧美");
        init("日语");
        init("韩语");
        init("粤语");
        init("小语种");
        init("流行");
        init("摇滚");
        init("民谣");
        init("电子");
        init("舞曲");
        init("说唱");
        init("轻音乐");
        init("爵士");
        init("乡村");
        init("R&B/Soul");
        init("古典");
        init("民族");
        init("英伦");
        init("金属");
        init("朋克");
        init("蓝调");
        init("雷鬼");
        init("世界音乐");
        init("拉丁");
        init("另类/独立");
        init("New Age");
        init("古风");
        init("后摇");
        init("Bossa Nova");
        init("清晨");
        init("夜晚");
        init("学习");
        init("工作");
        init("午休");
        init("下午茶");
        init("地铁");
        init("驾车");
        init("运动");
        init("旅行");
        init("散步");
        init("酒吧");
        init("怀旧");
        init("清新");
        init("浪漫");
        init("性感");
        init("伤感");
        init("治愈");
        init("放松");
        init("孤独");
        init("感动");
        init("兴奋");
        init("快乐");
        init("安静");
        init("思念");
        init("影视原声");
        init("ACG");
        init("校园");
        init("游戏");
        init("70后");
        init("80后");
        init("90后");
        init("网络歌曲");
        init("KTV");
        init("经典");
        init("翻唱");
        init("吉他");
        init("钢琴");
        init("器乐");
        init("儿童");
        init("榜单");
        init("00后");

        System.out.print("初始化歌曲分类完成");
    }

    public static <T> void main(String[] args) throws Exception {
        Date startTime = new Date();
        Crawler crawler = new CrawlerService();
        crawler.doRun();
//        for(Song song : crawler.getSongs()) {
//            System.out.println(song);
//        }
        System.out.println("花费时间：" + (new Date().getTime() - startTime.getTime()));
    }
    
}
