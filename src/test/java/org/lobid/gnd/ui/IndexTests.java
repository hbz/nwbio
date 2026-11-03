package org.lobid.gnd.ui;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import org.htmlunit.html.DomElement;
import org.htmlunit.html.HtmlInput;
import org.htmlunit.html.HtmlPage;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/* Tests for the `index.html` template */
public class IndexTests extends HtmlPageTests {

    @ParameterizedTest
    @ValueSource(strings = {DEVELOPMENT})
    public void testIndexContent(String baseUrl) throws IOException {
        assertThat(pageFor(baseUrl, "").asNormalizedText())
                .contains("Entwicklungsprojekt")
                .contains("Biographisches Portal NRW")
                .contains("Der Mensch in seiner Region")
                .contains(
                        "Das Portal bündelt biographische Informationen über Nordrhein-Westfalen.");
    }

    @ParameterizedTest
    @ValueSource(strings = {DEVELOPMENT})
    public void testIndexLinks(String baseUrl) throws IOException {
        assertThat(pageFor(baseUrl, "").getElementsByTagName("a").toString())
                .contains("https://www.landesbibliotheken.nrw")
                .contains("https://www.hbz-nrw.de");
    }

    @ParameterizedTest
    @ValueSource(strings = {DEVELOPMENT})
    public void testIndexImage(String baseUrl) throws IOException {
        assertThat(pageFor(baseUrl, "").getElementsByTagName("img").toString())
                .contains("wappen.png");
    }

    @ParameterizedTest
    @ValueSource(strings = {DEVELOPMENT})
    public void testIndexMapKreiseDefault(String baseUrl) throws IOException {
        HtmlPage indexPage = pageFor(baseUrl, "");
        DomElement map = indexPage.getHtmlElementById("map");
        assertThat(map).isNotNull();
        assertThat(map.getAttribute("data-scope")).isEqualTo("kreise");
        assertThat(indexPage.asNormalizedText()).contains("Kreisen (oder Gemeinden)");
        assertThat(indexPage.getElementsByTagName("a").toString()).contains("/?map=gemeinden");
    }

    @ParameterizedTest
    @ValueSource(strings = {DEVELOPMENT})
    public void testIndexMapGemeindenScope(String baseUrl) throws IOException {
        HtmlPage indexPage = pageFor(baseUrl, "?map=gemeinden");
        DomElement map = indexPage.getHtmlElementById("map");
        assertThat(map).isNotNull();
        assertThat(map.getAttribute("data-scope")).isEqualTo("gemeinden");
        assertThat(indexPage.asNormalizedText()).contains("Gemeinden (oder Kreisen)");
        assertThat(indexPage.getElementsByTagName("a").toString()).contains("/?map=kreise");
    }

    @ParameterizedTest
    @ValueSource(strings = {DEVELOPMENT})
    public void testIndexMapRendersSubunits(String baseUrl) throws IOException {
        HtmlPage indexPage = pageFor(baseUrl, "");
        var result =
                indexPage.executeJavaScript(
                        "document.querySelectorAll('#map path.map-svg-subunit').length");
        assertThat(((Number) result.getJavaScriptResult()).intValue()).isGreaterThan(50);
    }

    @ParameterizedTest
    @ValueSource(strings = {DEVELOPMENT})
    public void testIndexMapClickStartsLocationSearch(String baseUrl) throws IOException {
        HtmlPage indexPage = pageFor(baseUrl, "");
        indexPage.executeJavaScript(
                "document.querySelector('#map path.map-svg-subunit').dispatchEvent("
                        + "new MouseEvent('click', {bubbles: true}))");
        /* After the navigation triggered by the click, the top window's page is the search page: */
        HtmlPage searchPage =
                (HtmlPage) indexPage.getEnclosingWindow().getTopWindow().getEnclosedPage();
        String url = URLDecoder.decode(searchPage.getUrl().toString(), StandardCharsets.UTF_8);
        assertThat(url).contains("/search?location=");
        assertThat(url).matches(".*location=[^|]+\\|[a-z0-9+]+.*");
        HtmlInput searchBox = searchPage.getFirstByXPath("//input[@id='gnd-query']");
        assertThat(searchBox.getValue()).isNotEmpty();
        assertThat(searchPage.asNormalizedText()).contains("Treffer, zeige 1 bis");
    }
}
