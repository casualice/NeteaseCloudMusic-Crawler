package com.Crawler.crawler;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;

import org.jsoup.Connection;
import org.jsoup.Connection.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONPath;
import com.google.common.collect.ImmutableMap;
import com.Crawler.crawler.model.WebPage;
import com.Crawler.crawler.model.WebPage.PageType;

public class HtmlParser {
    
    private static final HtmlFetcher HTML_FETCHER = new HtmlFetcher();
    private static final String BASE_URL = "http://music.163.com/";
    private static final String text = "{\"username\": \"\", \"rememberLogin\": \"true\", \"password\": \"\"}";
    
    public List<WebPage> parsePlaylists(String url) {
        Document document = Jsoup.parse(HTML_FETCHER.fetch(url));
        Elements playlists = document.select(".tit.f-thide.s-fc0");
        return playlists.stream().map(e -> new WebPage(BASE_URL + e.attr("href"), PageType.playlist)).collect(Collectors.toList());
    }
    
    public List<WebPage> parsePlaylist(String url) {
        Elements songs = Jsoup.parse(HTML_FETCHER.fetch(url)).select("ul.f-hide li a");
        return songs.stream().map(e -> new WebPage(BASE_URL + e.attr("href"), PageType.song, e.html())).collect(Collectors.toList());
    }
    
    public Long parseSong(String url) {
        try {
            return getCommentCount(url.split("=")[1]);
        } catch (Exception e) {
            return 0L;
        }
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

    private String aesEncrypt(String value, String key) throws Exception {
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
        cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(key.getBytes("UTF-8"), "AES"), new IvParameterSpec(
                "0102030405060708".getBytes("UTF-8")));
        return java.util.Base64.getEncoder().encodeToString(cipher.doFinal(value.getBytes()));
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
    
    public static <T> void main(String[] args) throws Exception {
        HtmlParser htmlParser = new HtmlParser();
        htmlParser.parsePlaylists("http://music.163.com/discover/playlist/?order=hot&cat=%E5%85%A8%E9%83%A8&limit=35&offset=0")
        .forEach(playlist -> System.out.println(playlist));
        System.out.println("=====================");
        htmlParser.parsePlaylist("http://music.163.com/playlist?id=454016843").forEach(song -> System.out.println(song));
        System.out.println("=====================");
        System.out.println(htmlParser.parseSong("http://music.163.com/song?id=29999506"));

    }
}
