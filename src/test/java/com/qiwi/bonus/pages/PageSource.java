package com.qiwi.bonus.pages;

import org.jsoup.Jsoup;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;

import static io.restassured.RestAssured.given;

/**
 * Created by power on 10.11.2017. 2:18
 */
public class PageSource {
    public List<String> getLinksFromPageSourceStartingWith(String pageSource, String schema) {
        Elements hrefElements = Jsoup.parse(pageSource).getElementsByAttributeValueStarting("href", schema);

        List<String> filteredLinks = new ArrayList<>();

        hrefElements.forEach(href -> filteredLinks.add(href.attr("href")));

        return filteredLinks;
    }

    public String getPageSource(String queryLink) {
        return given()
                .when()
                .get(queryLink)
                .getBody()
                .asString();
    }
}
