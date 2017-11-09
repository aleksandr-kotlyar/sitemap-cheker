package com.qiwi.bonus.tests.api;

import com.qiwi.bonus.StepsLogger;
import com.qiwi.bonus.pages.PageSource;
import io.qameta.allure.Issue;
import io.qameta.allure.restassured.AllureRestAssured;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.assertj.core.api.JUnitSoftAssertions;
import org.jsoup.Jsoup;
import org.jsoup.select.Elements;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static io.restassured.RestAssured.given;

/**
 * Created by alexander.kotlyar on 09.11.2017. 21:44
 */
public class BonusSiteMapLinksTest extends StepsLogger {
    private static final String HTTPS_BONUS_QIWI_COM_SITEMAP = "https://bonus.qiwi.com/sitemap";
    private static final String BONUS_LINK_SCHEMA = "https://bonus.qiwi.com";
    private static String BONUS_SITEMAP_PAGE_SOURCE;
    private List<String> allBonusSiteMapLinks;
    private PageSource pageSource = new PageSource();

    @Rule
    public final JUnitSoftAssertions softly = new JUnitSoftAssertions();

    @Before
    public void checkBonusSiteMapIsAvailable() throws Exception {
        RestAssured.filters(new AllureRestAssured());
        given()
                .when()
                .get(HTTPS_BONUS_QIWI_COM_SITEMAP)
                .then()
                .statusCode(200);
    }

    /**
     * 1.Get sitemap pageSource
     * 2.Collect all links from sitemap
     * 3.Check each link availability (200 response status code)
     */
    @Test
    @Issue("1")
    public void testAvailabilityOfAllBonusSiteMapLinks() throws Exception {
        System.setProperty("java.util.concurrent.ForkJoinPool.common.parallelism", "15");
        arrange("Get sitemap pageSource", () ->
                BONUS_SITEMAP_PAGE_SOURCE = pageSource.getPageSource(HTTPS_BONUS_QIWI_COM_SITEMAP));

        act("Collect all links from sitemap", () ->
                allBonusSiteMapLinks = pageSource.getLinksFromPageSourceStartingWith(BONUS_SITEMAP_PAGE_SOURCE, BONUS_LINK_SCHEMA));

        assertion("Check each link availability (200 response status code)", () ->
                //allBonusSiteMapLinks.forEach(this::assertStatusCode)); //12 minutes
                allBonusSiteMapLinks.parallelStream().forEach(this::assertStatusCode)); //1 minute
    }

    /*

        These links had status 500 between 23:22:22 and 23:23:15 on Nov.9.2017:

            [link: 'https://bonus.qiwi.com/offers/sovest']
            [link: 'https://bonus.qiwi.com/offers/ssylka-dlya-mfo-na-qiwi-com']

        */

    private void assertStatusCode(String link) {
        Response response = given()
                .when()
                .head(link);

        softly.assertThat(response.statusCode()).as("link: '" + link + "'").isEqualTo(200);
    }
}
