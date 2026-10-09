package org.lobid.gnd.ui;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.util.List;
import org.htmlunit.ElementNotFoundException;
import org.htmlunit.html.DomElement;
import org.htmlunit.html.HtmlAnchor;
import org.htmlunit.html.HtmlCanvas;
import org.htmlunit.html.HtmlDivision;
import org.htmlunit.html.HtmlPage;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/* Tests for the `details.html` template */
public class DetailsTests extends HtmlPageTests {

    @ParameterizedTest
    @ValueSource(strings = {DEVELOPMENT})
    public void testDetailsViewFields(String baseUrl) throws IOException {
        HtmlPage detailsPage = pageFor(baseUrl, ALBERT);
        assertThat(detailsPage.getTitleText()).isEqualTo("Albertus, Magnus, Heiliger");
        assertThat(detailsPage.asNormalizedText())
                .contains("https://d-nb.info/gnd/118637649")
                .contains("Köln")
                .contains("Albert, the Great")
                .contains("Alberto, Magno")
                .contains("Philosoph")
                .contains("Alchemist");
    }

    @ParameterizedTest
    @ValueSource(strings = {DEVELOPMENT})
    public void testDetailsViewHeader(String baseUrl) throws IOException {
        HtmlPage detailsPage = pageFor(baseUrl, ALBERT);
        assertThat(detailsPage.getElementsByTagName("h1").getFirst().getTextContent())
                .as("Main header of full page as XML: \n%s", detailsPage.asXml())
                .contains("Albertus, Magnus, Heiliger")
                .contains("Person")
                .contains("Individualisierte Person")
                .contains("1193–1280");
    }

    @ParameterizedTest
    @ValueSource(strings = {DEVELOPMENT})
    public void testDetailsViewLinks(String baseUrl) throws IOException {
        assertThat(pageFor(baseUrl, ALBERT).getElementsByTagName("a").toString())
                .contains("Person")
                .contains("DifferentiatedPerson")
                .contains("118637649.json")
                .contains("https://d-nb.info/standards/vocab/gnd/geographic-area-code#XA-DE")
                .contains("http://viaf.org/viaf/316875564")
                .contains("http://www.hls-dhs-dss.ch/textes/d/D12458.php")
                .contains("https://www.dnb.de/lds")
                .contains("https://d-nb.info/gnd/118637649/about/lds.rdf")
                .contains("https://d-nb.info/gnd/118637649/about/lds.ttl")
                .contains("https://www.dnb.de/entityfacts")
                .contains("http://hub.culturegraph.org/entityfacts/118637649")
                .contains("https://creativecommons.org/publicdomain/zero/1.0/");
    }

    @ParameterizedTest
    @ValueSource(strings = {DEVELOPMENT})
    @Disabled // enable for AP4, see
    // https://service-wiki.hbz-nrw.de/spaces/SEM/pages/1227915278/Biographisches+Portal+NRW+NWBio
    public void testDetailsViewMap(String baseUrl) throws IOException {
        List<DomElement> mapElements = pageFor(baseUrl, ALBERT).getElementsById("authority-map");
        assertThat(mapElements).isNotEmpty();
        assertThat(mapElements.getFirst().getElementsByTagName("a").toString())
                .containsPattern("https?://leafletjs.com")
                .contains("http://osm.org/copyright");
    }

    @ParameterizedTest
    @ValueSource(strings = {DEVELOPMENT})
    public void testDetailsViewImage(String baseUrl) throws IOException {
        HtmlPage detailsPage = pageFor(baseUrl, ALBERT);
        assertThat(detailsPage.getByXPath("//img[@alt='Albertus, Magnus, Heiliger']")).isNotEmpty();
        assertThat(detailsPage.getBody().asNormalizedText())
                .contains("Bildquelle")
                .contains("Wikimedia Commons")
                .contains("CC");
        assertThat(detailsPage.getElementsByTagName("a").toString())
                .contains("https://commons.wikimedia.org")
                .contains("https://creativecommons.org");
    }

    @ParameterizedTest
    @ValueSource(strings = {DEVELOPMENT})
    public void testDetailsViewPersonHistorical(String baseUrl) throws IOException {
        HtmlPage detailsPage = pageFor(baseUrl, PERSON_HISTORICAL);
        assertThat(detailsPage.asNormalizedText())
                .contains("Beruf oder Beschäftigung")
                .contains("Adelstitel")
                .contains("Geburtsdatum")
                .contains("Sterbedatum")
                .contains("Beziehung, Bekanntschaft, Freundschaft")
                .contains("Titelangabe");
        assertThrows(
                ElementNotFoundException.class,
                () -> detailsPage.getHtmlElementById("meta-person"));
    }

    @ParameterizedTest
    @ValueSource(strings = {DEVELOPMENT})
    public void testDetailsViewPersonAlive(String baseUrl) throws IOException {
        HtmlPage detailsPage = pageFor(baseUrl, PERSON_ALIVE);
        HtmlDivision personBox = detailsPage.getHtmlElementById("meta-person");
        assertThat(personBox).isNotNull();
        String linkPath = "//a[@data-toggle='collapse' or @data-bs-toggle='collapse']";
        HtmlAnchor link = (HtmlAnchor) personBox.getByXPath(linkPath).get(0);
        assertThat(link).isNotNull();
        assertThat(link.getTextContent()).contains("Sind Sie").contains("Klicken Sie hier");
        String text = "Diese Seite zeigt einen Datensatz aus der Gemeinsamen Normdatei";
        assertThat(detailsPage.asNormalizedText()).doesNotContain(text);
        assertThat(((HtmlPage) link.click()).asNormalizedText()).contains(text);
    }

    @ParameterizedTest
    @ValueSource(strings = {DEVELOPMENT})
    public void testDetailsViewRelationshipsGraphExists(String baseUrl) throws IOException {
        HtmlPage detailsPage = pageFor(baseUrl, PERSON_WITH_RELATIONSHIPS);
        HtmlDivision networkDiv = detailsPage.getHtmlElementById("gnd-network");
        assertThat(networkDiv.getFirstChild().getFirstChild()).isInstanceOf(HtmlCanvas.class);
    }

    @ParameterizedTest
    @ValueSource(strings = {DEVELOPMENT})
    public void testDetailsViewRelationshipsGraphData(String baseUrl) throws IOException {
        HtmlPage detailsPage = pageFor(baseUrl, PERSON_WITH_RELATIONSHIPS);
        var nodesResult = detailsPage.executeJavaScript("window.network.body.data.nodes.length");
        var edgesResult = detailsPage.executeJavaScript("window.network.body.data.edges.length");
        assertThat(((Number) nodesResult.getJavaScriptResult()).intValue()).isGreaterThan(30);
        assertThat(((Number) edgesResult.getJavaScriptResult()).intValue()).isGreaterThan(35);
    }

    @ParameterizedTest
    @ValueSource(strings = {DEVELOPMENT})
    public void testDetailsViewRelationshipsNodeLabels(String baseUrl) throws IOException {
        HtmlPage detailsPage = pageFor(baseUrl, PERSON_WITH_RELATIONSHIPS);
        assertThat(getString(detailsPage, "nodes", PERSON_WITH_RELATIONSHIPS, "label"))
                .isEqualTo("Heine,\nHeinrich");
    }

    @ParameterizedTest
    @ValueSource(strings = {DEVELOPMENT})
    public void testDetailsViewRelationshipsEdgeLabels(String baseUrl) throws IOException {
        HtmlPage detailsPage = pageFor(baseUrl, PERSON_WITH_RELATIONSHIPS);
        assertThat(getString(detailsPage, "edges", "pseudonym_1287690238", "label"))
                .isEqualTo("Pseudonym");
    }

    @ParameterizedTest
    @ValueSource(strings = {DEVELOPMENT})
    public void testDetailsViewRelationshipsEdgeTitles(String baseUrl) throws IOException {
        HtmlPage detailsPage = pageFor(baseUrl, PERSON_WITH_RELATIONSHIPS);
        assertThat(getString(detailsPage, "edges", "pseudonym_1287690238", "title"))
                .isEqualTo("Einträge mit Pseudonym 'Riesenharf, Sy. Freudhold' suchen");
    }

    private String getString(HtmlPage detailsPage, String kind, String id, String field) {
        var script = String.format("window.network.body.data.%s.get('%s').%s", kind, id, field);
        return (String) detailsPage.executeJavaScript(script).getJavaScriptResult();
    }
}
