package sitemap.tests;

import sitemap.StepsLogger;
import io.restassured.response.Response;
import org.assertj.core.api.SoftAssertions;
import org.jsoup.Jsoup;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static io.restassured.RestAssured.given;

/**
 * Created by Aleksandr Kotlyar on 09.11.2017. 21:44
 */
public class SitemapTest extends StepsLogger {
    private static final String SITEMAP_URL = "https://qiwi.com/sitemap.xml";
    private static String SITEMAP_PAGE_SOURCE;
    private List<String> allSitemapLinks = new ArrayList<>();
    private SoftAssertions softAssertions = new SoftAssertions();

    @Before
    public void checkSitemapIsAvailable() throws Exception {
        given().header("user-agent", "yandex").when().get(SITEMAP_URL).then().statusCode(200);
    }

    /**
     * 1.Get sitemap source
     * 2.Collect all links from sitemap
     * 3.Check each link availability (200 response status code)
     */
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
}