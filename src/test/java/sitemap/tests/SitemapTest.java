package sitemap.tests;

import io.restassured.response.Response;
import org.assertj.core.api.SoftAssertions;
import org.jsoup.Jsoup;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import sitemap.StepsLogger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import static io.restassured.RestAssured.given;

/**
 * Created by Aleksandr Kotlyar on 09.11.2017. 21:44
 */
public class SitemapTest extends StepsLogger {
    private static String SITEMAP_PAGE_SOURCE;
    private String SITEMAP_URL;
    private List<String> allSitemapLinks = new ArrayList<>();
    private SoftAssertions softAssertions = new SoftAssertions();

    @Before
    public void checkSitemapIsAvailable() throws Exception {
        SITEMAP_URL = getSitemapUrl();
        given().header("user-agent", "yandex").when().get(SITEMAP_URL).then().statusCode(200);
    }

    /**
     * 1.Get sitemap source
     * 2.Collect all links from sitemap
     * 3.Check each link availability (200 response status code)
     */
    @Category(Xml.class)
    @Test
    public void testAvailabilityOfAllSitemapLinks() throws Exception {
        System.setProperty("java.util.concurrent.ForkJoinPool.common.parallelism", "150");
        arrange("Get sitemap source", () ->
                SITEMAP_PAGE_SOURCE = given().header("user-agent", "yandex").when().get(SITEMAP_URL).getBody().asString());

        act("Collect all links from sitemap", () ->
                Jsoup.parse(SITEMAP_PAGE_SOURCE).getElementsByTag("loc").forEach(href -> allSitemapLinks.add(href.text())));

        assertion("Check each link availability (200 response status code)", () -> {
            info(String.format("%s", allSitemapLinks.size()));
            allSitemapLinks.parallelStream().forEach(this::assertStatusCode);
            softAssertions.assertAll();
        });
    }

    private void assertStatusCode(String link) {
        Response response = given().when().head(link);
        info(String.format("%2d;%s", response.statusCode(), link));
        softAssertions.assertThat(response.statusCode()).as("link: '" + link + "'").isEqualTo(200);
    }

    private String getSitemapUrl() {
        String url = getProperty("sitemapUrl");
        Assert.assertNotEquals("Target url is empty! Set sitemapUrl in pom.xml or ${SITEMAPURL} in Gitlab-CI", "", url);
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