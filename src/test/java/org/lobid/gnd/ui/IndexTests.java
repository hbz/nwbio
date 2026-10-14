package org.lobid.gnd.ui;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import org.htmlunit.html.HtmlFigureCaption;
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
    public void testIndexImages(String baseUrl) throws IOException {
        HtmlPage indexPage = pageFor(baseUrl, "");
        assertThat(indexPage.getElementsByTagName("img").toString())
                .contains("wappen.png")
                .contains("https://commons.wikimedia.org/wiki/Special:FilePath");
        HtmlFigureCaption figcaption = indexPage.getFirstByXPath("//figcaption");
        assertThat(figcaption.getTextContent())
                .contains("Bildquelle")
                .contains("Wikimedia Commons");
    }
}
