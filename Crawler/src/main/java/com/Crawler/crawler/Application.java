package com.Crawler.crawler;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableAsync;

import java.io.IOException;
import java.net.URISyntaxException;

@SpringBootApplication
@EnableAsync
@EnableCaching
public class Application {

    public static void main(String[] args) throws IOException, URISyntaxException {
        SpringApplication.run(Application.class);
    }

}