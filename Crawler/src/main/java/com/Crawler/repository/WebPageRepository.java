package com.Crawler.repository;

import javax.transaction.Transactional;

import com.Crawler.model.WebPage.PageType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import com.Crawler.model.WebPage;
import com.Crawler.model.WebPage.Status;

import java.util.List;

public interface WebPageRepository extends JpaRepository<WebPage, String> {

    WebPage findTopByStatus(Status status);

    @Modifying
    @Transactional
    @Query("update WebPage w set w.status = ?1")
    void resetStatus(Status status);

    List<WebPage> findByType(PageType pageType);
}

