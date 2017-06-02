package com.Crawler.service;

import com.Crawler.model.*;
import com.Crawler.repository.PlaylistRepository;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONPath;
import com.google.common.collect.ImmutableMap;
import com.Crawler.model.WebPage.PageType;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.util.EntityUtils;
import org.apache.tomcat.util.codec.binary.Base64;
import org.jsoup.Connection;
import org.jsoup.Connection.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.net.URL;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

public class CrawlerThread implements Runnable {

    public static final String BASE_URL = "http://music.163.com/";
    public static final String text = "{\"username\": \"\", \"rememberLogin\": \"true\", \"password\": \"\"}";
    private CrawlerService crawlerService;

    @Autowired
    PlaylistRepository playlistRepository;

    public CrawlerThread() {
        super();
    }

    public CrawlerThread(CrawlerService crawlerService) {
        this.crawlerService = crawlerService;
    }

    @Override
    public void run() {
        while (true) {
            WebPage webPage = null;
            try {
                webPage = crawlerService.getUnCrawlPage();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (webPage == null)
                return; // 拿不到url，说明没有需要爬的url，直接退出
            try {
                if (fetchHtml(webPage))
                    parse(webPage);
                    //Thread.sleep((long) (Math.random()*10000));
            } catch (Exception e) {

            }
        }
    }

    private boolean fetchHtml(WebPage webPage) throws IOException {
        Response response = Jsoup.connect(webPage.getUrl()).execute();
        webPage.setHtml(response.body());
        return response.statusCode() / 100 == 2 ? true : false;
    }

    private void parse(WebPage webPage) throws Exception {
        if (PageType.playlists.equals(webPage.getType()))
            parsePlaylists(webPage).forEach(page -> crawlerService.savePage(page));
        if (PageType.playlist.equals(webPage.getType())) {
            parsePlaylist(webPage).forEach(page -> crawlerService.savePage(page));
            crawlerService.savePlaylist(getPlayListBean(webPage));
        }
        if (PageType.song.equals(webPage.getType()))
            crawlerService.saveSong(parseSong(webPage));
    }

    private List<WebPage> parsePlaylists(WebPage webPage) {
        Elements playlists = Jsoup.parse(webPage.getHtml()).select(".tit.f-thide.s-fc0");
        return playlists.stream().map(e -> new WebPage(BASE_URL + e.attr("href"), PageType.playlist)).collect(Collectors.toList());
    }

    private List<WebPage> parsePlaylist(WebPage webPage) {
        Elements songs = Jsoup.parse(webPage.getHtml()).select("ul.f-hide li a");
        return songs.stream().map(e -> new WebPage(BASE_URL + e.attr("href"), PageType.song, e.html())).collect(Collectors.toList());
    }


    private Song parseSong(WebPage webPage) throws Exception {
        Song s = new Song(webPage.getUrl(), webPage.getTitle(), getCommentCount(webPage.getUrl().split("=")[1]));
        return s;
    }

    private Long getCommentCount(String id) throws Exception {
        String secKey = new BigInteger(100, new SecureRandom()).toString(32).substring(0, 16);
        String encText = aesEncrypt(aesEncrypt(text, "0CoJUm6Qyw8W8jud"), secKey);
        String encSecKey = rsaEncrypt(secKey);
        Response response = Jsoup
                .connect("http://music.163.com/weapi/v1/resource/comments/R_SO_4_" + id + "/?csrf_token=")
                .method(Connection.Method.POST).header("Referer", BASE_URL)
                .data(ImmutableMap.of("params", encText, "encSecKey", encSecKey)).execute();
        return Long.parseLong(JSONPath.eval(JSON.parse(response.body()), "$.total").toString());
    }

    public  MusicCommentMessage parseCommentMessage(String songId) throws Exception {
        String songUrl =  "http://music.163.com/song?id=" + songId;
        URL uri = new URL(songUrl);
        Document msdoc = Jsoup.parse(uri, 3000);

        String secKey = new BigInteger(100, new SecureRandom()).toString(32).substring(0, 16);
        String encText = aesEncrypt(aesEncrypt(text, "0CoJUm6Qyw8W8jud"), secKey);
        String encSecKey = rsaEncrypt(secKey);
        Response response = Jsoup
                .connect("http://music.163.com/weapi/v1/resource/comments/R_SO_4_" + songId + "/?csrf_token=")
                .method(Connection.Method.POST).header("Referer", "http://music.163.com/")
                .data(ImmutableMap.of("params", encText, "encSecKey", encSecKey)).execute();

        Object res = JSON.parse(response.body());

        if (res == null) {
            return null;
        }

        MusicCommentMessage musicCommentMessage = new MusicCommentMessage();

        int commentCount = (int)JSONPath.eval(res, "$.total");
        int hotCommentCount = (int)JSONPath.eval(res, "$.hotComments.size()");
        int latestCommentCount = (int)JSONPath.eval(res, "$.comments.size()");

        String songtitle = msdoc.title().substring(0,msdoc.title().indexOf("-",msdoc.title().indexOf("-")+1));
        musicCommentMessage.setSongTitle(songtitle);
        musicCommentMessage.setSongUrl(songUrl);
        musicCommentMessage.setCommentCount(commentCount);

        List<MusicComment> ls = new ArrayList<MusicComment>();

        if (commentCount != 0 && hotCommentCount != 0) {

            for (int i = 0; i < hotCommentCount; i++) {
                String nickname = JSONPath.eval(res, "$.hotComments[" + i + "].user.nickname").toString();
                String content = JSONPath.eval(res, "$.hotComments[" + i + "].content").toString();
                Long appreciation =Long.parseLong(JSONPath.eval(res, "$.hotComments[" + i + "].likedCount").toString());
                ls.add(new MusicComment(songtitle,"hotComment", nickname, content, appreciation));
            }
        } else if (commentCount != 0) {

            for (int i = 0; i < latestCommentCount; i++) {
                String nickname = JSONPath.eval(res, "$.comments[" + i + "].user.nickname").toString();
                String content = JSONPath.eval(res, "$.comments[" + i + "].content").toString();
                Long appreciation = Long.parseLong(JSONPath.eval(res, "$.comments[" + i + "].likedCount").toString());
                ls.add(new MusicComment(songtitle,"latestCommentCount", nickname,  content, appreciation));
            }
        }
        musicCommentMessage.setComments(ls);
        return musicCommentMessage;
    }

    private String aesEncrypt(String value, String key) throws Exception {
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
        cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(key.getBytes("UTF-8"), "AES"), new IvParameterSpec(
                "0102030405060708".getBytes("UTF-8")));
        return Base64.encodeBase64String(cipher.doFinal(value.getBytes()));
    }

    private String rsaEncrypt(String value) throws UnsupportedEncodingException {
        value = new StringBuilder(value).reverse().toString();
        BigInteger valueInt = hexToBigInteger(stringToHex(value));
        BigInteger pubkey = hexToBigInteger("010001");
        BigInteger modulus = hexToBigInteger("00e0b509f6259df8642dbc35662901477df22677ec152b5ff68ace615bb7b725152b3ab17a876aea8a5aa76d2e417629ec4ee341f56135fccf695280104e0312ecbda92557c93870114af6c9d05c4f7f0c3685b7a46bee255932575cce10b424d813cfe4875d3e82047b97ddef52741d546b8e289dc6935b3ece0462db0a22b8e7");
        return valueInt.modPow(pubkey, modulus).toString(16);
    }

    private BigInteger hexToBigInteger(String hex) {
        return new BigInteger(hex, 16);
    }

    private String stringToHex(String text) throws UnsupportedEncodingException {
        return DatatypeConverter.printHexBinary(text.getBytes("UTF-8"));
    }

    public static Playlist getPlayListBean(WebPage webPage){
        String html = webPage.getHtml();
        if (html != null && !"".equals(html)) {
            Playlist bean = new Playlist();
            Document document = Jsoup.parse(html);
            //设置收藏数
            Elements elements = document.select(".u-btni-fav");
            if (elements != null && elements.size() > 0) {
                String collectCount = elements.first().attr("data-count");
                if (collectCount != null && !"".equals(collectCount)) {
                    bean.setCollectCount(Integer.parseInt(collectCount));
                }
            }
            //设置歌单名
            elements = document.select(".f-ff2.f-brk");
            if(elements != null && elements.size() > 0){
                bean.setName(elements.first().text());
            }
            //设置url链接和id
            elements =document.select("#content-operation");
            if(elements != null && elements.size() > 0){
                String id = elements.first().attr("data-rid");
                bean.setId(Integer.parseInt(id));
                bean.setUrl("http://music.163.com/playlist?id=" + id);
            }
            //设置图片
            elements = document.select(".j-img");
            if(elements != null && elements.size() > 0 ){
                bean.setImage(elements.first().attr("data-src"));
            }
            //设置作者
            elements =document.select(".s-fc7");
            if(elements != null && elements.size() > 0) {
                bean.setAuthor(elements.first().text());
            }

            //设置播放次数
            elements =document.select("#play-count");
            if(elements !=null && elements.size() > 0){
                bean.setPlayCount(Integer.parseInt(elements.first().text()));
            }
            return bean;
        }
        return null;
    }

    public  pl parsePlaylist(String Id) throws IOException {
        String url =  "http://music.163.com/playlist?id=" + Id;
        String html = Jsoup.connect(url).timeout(3000).execute().body();
        if (html != null && !"".equals(html)) {
            pl bean = new pl();
            Document document = Jsoup.parse(html);
            //设置收藏数
            Elements elements = document.select(".u-btni-fav");
            if (elements != null && elements.size() > 0) {
                String collectCount = elements.first().attr("data-count");
                if (collectCount != null && !"".equals(collectCount)) {
                    bean.setCollectCount(Integer.parseInt(collectCount));
                }
            }
            //设置歌单名
            elements = document.select(".f-ff2.f-brk");
            if(elements != null && elements.size() > 0){
                bean.setName(elements.first().text());
            }
            //设置url链接和id
            elements =document.select("#content-operation");
            if(elements != null && elements.size() > 0){
                String id = elements.first().attr("data-rid");
                bean.setId(Integer.parseInt(id));
                bean.setUrl("http://music.163.com/playlist?id=" + id);
            }
            //设置图片
            elements = document.select(".j-img");
            if(elements != null && elements.size() > 0 ){
                bean.setImage(elements.first().attr("data-src"));
            }
            //设置作者
            elements =document.select(".s-fc7");
            if(elements != null && elements.size() > 0) {
                bean.setAuthor(elements.first().text());
            }
            //设置歌单标签
            elements =document.select(".u-tag i");
            if(elements != null && elements.size() > 0){
                String tag;
                tag=elements.text();
                bean.setTag(tag);
            }

            //设置歌单歌曲目录
            elements =document.select("ul.f-hide li a");
            if(elements != null && elements.size() > 0){
                List<Song> songs = new ArrayList<Song>();
                Iterator it = elements.iterator();
                while(it.hasNext()) {
                    Element element = (Element)it.next();
                    songs.add(new Song(BASE_URL+element.attr("href"),element.text()));
                }
                bean.setSongs(songs);
            }

            //设置歌单简介
            elements = document.select("#album-desc-more");
            if(elements != null && elements.size() > 0){
                bean.setIntroduction(elements.outerHtml());
            }

            //设置播放次数
            elements =document.select("#play-count");
            if(elements !=null && elements.size() > 0){
                bean.setPlayCount(Integer.parseInt(elements.first().text()));
            }
            return bean;
        }
        return null;
    }

    public static void main(String arg[]) throws IOException {
        CrawlerThread crawlerThread = new CrawlerThread();
        String id = "599148886";
        pl p = crawlerThread.parsePlaylist(id);
        System.out.println(p.getIntroduction());
        System.out.println(p.getTag());
        System.out.println(p.getSongs());
        List<Song> s=p.getSongs();
        Iterator it = s.iterator();
        while (it.hasNext()){
            Song song = (Song) it.next();
            System.out.println(song.getUrl());
        }

    }
}
