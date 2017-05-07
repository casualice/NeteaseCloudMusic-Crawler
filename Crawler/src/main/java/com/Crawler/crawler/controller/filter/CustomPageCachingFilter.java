package com.Crawler.crawler.controller.filter;

import net.sf.ehcache.constructs.web.filter.SimpleCachingHeadersPageCachingFilter;

public class CustomPageCachingFilter extends SimpleCachingHeadersPageCachingFilter {

    private final String customCacheName;

    public CustomPageCachingFilter(String name){
        this.customCacheName = name;
    }

    @Override
    protected String getCacheName() {
        return customCacheName;
    }

}