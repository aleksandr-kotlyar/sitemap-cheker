package tests;

import io.restassured.response.Response;
import org.assertj.core.api.SoftAssertions;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import source.type.Html;
import source.type.Xml;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Created by Aleksandr Kotlyar on 09.11.2017. 21:44
 */
public class SitemapTest extends StepsLogger {
    private String SITEMAP_URL;
    private String SITEMAP_PAGE_SOURCE;
    private List<String> allLinks = new ArrayList<>();
    private SoftAssertions softAssertions = new SoftAssertions();

    @Before
    public void checkSitemapIsAvailable() {
        SITEMAP_URL = getSitemapUrl();
        given().header("user-agent", "yandex").get(SITEMAP_URL).then().statusCode(200);
    }

    /**
     * 1.Get sitemap source
     * 2.Collect all links from sitemap
     * 3.Check each link availability (200 response status code)
     */
    @Category(Xml.class)
    @Test
    public void testAvailabilityOfAllSitemapLinks() {
        System.setProperty("java.util.concurrent.ForkJoinPool.common.parallelism", "20");
        arrange("Get sitemap source", () ->
                SITEMAP_PAGE_SOURCE = given().header("user-agent", "yandex").get(SITEMAP_URL).getBody().asString());

        act("Collect all links from sitemap", () ->
                Jsoup.parse(SITEMAP_PAGE_SOURCE).getElementsByTag("loc").forEach(href -> allLinks.add(href.text())));

        assertion("Check each link availability (200 response status code)", () -> {
            info(String.format("%s", allLinks.size()));
            allLinks.parallelStream().forEach(this::assertStatusCode);
            softAssertions.assertAll();
        });
    }


    /**
     * 1.Get page source
     * 2.Collect all links from page
     * 3.Check each link availability (200 response status code)
     */
    @Category(Html.class)
    @Test
    public void testAvailabilityOfAllHtmlPageLinks() {
        System.setProperty("java.util.concurrent.ForkJoinPool.common.parallelism", "10");

        arrange("Get page source", () ->
                SITEMAP_PAGE_SOURCE = given().get(SITEMAP_URL).getBody().asString());

        act("Collect all links from sitemap", () ->
                allLinks = getLinksFromPageSource(SITEMAP_PAGE_SOURCE, getDomain()));

        assertion("Check each link availability (200 response status code)", () -> {
            info(String.format("%s", allLinks.size()));
            allLinks.parallelStream().forEach(this::assertStatusCode);
            softAssertions.assertAll();
        });
    }


    private void assertStatusCode(String link) {
        Response response = given().header("user-agent", "yandex").redirects().follow(false)
                .when().head(link);
        softAssertions.assertThat(response.statusCode()).as("link: '" + link + "'").isEqualTo(200);
    }

    private List<String> getLinksFromPageSource(String pageSource, String domain) {
        assertThat(SITEMAP_URL).contains(domain);
        Elements hrefElements = Jsoup.parse(pageSource).getElementsByTag("a");
        List<String> filteredLinks = new ArrayList<>();
        for (Element href : hrefElements) {
            if (href.attr("href").contains("@")) continue;
            if (href.attr("href").contains(domain)) {
                filteredLinks.add(href.attr("href"));
            } else if (href.attr("href").startsWith("/")) {
                filteredLinks.add(SITEMAP_URL + href.attr("href"));
            }
        }
        return filteredLinks;
    }

    private String getDomain() {
        String url = getProperty("domain");
        Assert.assertNotEquals("Target domain is empty! Set domain in pom.xml or ${DOMAIN} in Gitlab-CI", "", url);
        return url;
    }

    private String getSitemapUrl() {
        String url = getProperty("sitemapUrl");
        Assert.assertNotEquals("Target sitemapUrl is empty! Set sitemapUrl in pom.xml or ${SITEMAPURL} in Gitlab-CI", "", url);
        return url;
    }

    private String getProperty(String propertyName) {
        Properties properties = new Properties();
        try {
            properties.load(SitemapTest.class.getClassLoader().getResourceAsStream("Sitemap.properties"));
        } catch (IOException e) {
            e.printStackTrace();
            Assert.fail();
        }
        return properties.getProperty(propertyName);
    }
}