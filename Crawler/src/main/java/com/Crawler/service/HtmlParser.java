package com.Crawler.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONPath;
import com.google.common.collect.ImmutableMap;
import org.apache.tomcat.util.codec.binary.Base64;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.List;
import java.util.stream.Collectors;
import com.Crawler.model.WebPage;
import com.Crawler.model.Song;
import com.Crawler.model.WebPage.PageType;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;

public class HtmlParser {
    public static final String BASE_URL = "http://music.163.com/";
    public static final String text = "{\"username\": \"\", \"rememberLogin\": \"true\", \"password\": \"\"}";


    private static boolean fetchHtml(WebPage webPage) throws IOException {
        Connection.Response response = Jsoup.connect(webPage.getUrl()).timeout(3000).execute();
        webPage.setHtml(response.body());
        return response.statusCode() / 100 == 2 ? true : false;
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
        return new Song(webPage.getUrl(), webPage.getTitle(), getCommentCount(webPage.getUrl().split("=")[1]));
    }

    private Long getCommentCount(String id) throws Exception {
        String secKey = new BigInteger(100, new SecureRandom()).toString(32).substring(0, 16);
        String encText = aesEncrypt(aesEncrypt(text, "0CoJUm6Qyw8W8jud"), secKey);
        String encSecKey = rsaEncrypt(secKey);
        Connection.Response response = Jsoup
                .connect("http://music.163.com/weapi/v1/resource/comments/R_SO_4_" + id + "/?csrf_token=")
                .method(Connection.Method.POST).header("Referer", BASE_URL)
                .data(ImmutableMap.of("params", encText, "encSecKey", encSecKey)).execute();
        return Long.parseLong(JSONPath.eval(JSON.parse(response.body()), "$.total").toString());
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
    public static void main(String[] args) throws Exception {
        WebPage playlists = new WebPage("http://music.163.com/#/discover/playlist/?cat=%E6%B8%85%E6%96%B0&order=hot", PageType.playlists);
        HtmlParser crawlerThread = new HtmlParser();
        HtmlParser.fetchHtml(playlists);
        crawlerThread.parsePlaylists(playlists);

        System.out.println(playlists.getHtml());
    }
}
