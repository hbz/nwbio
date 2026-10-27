package org.lobid.gnd.ui;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import org.htmlunit.html.DomElement;
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
}
