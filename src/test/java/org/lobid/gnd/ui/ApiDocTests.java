package org.lobid.gnd.ui;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import org.htmlunit.html.HtmlAnchor;
import org.htmlunit.html.HtmlButton;
import org.htmlunit.html.HtmlElement;
import org.htmlunit.html.HtmlInput;
import org.htmlunit.html.HtmlPage;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/* Tests for the `/api` documentation page */
public class ApiDocTests extends HtmlPageTests {

    private static final String API_DOC = "/api";

    @ParameterizedTest
    @ValueSource(strings = {DEVELOPMENT})
    public void testApiDocPageTitle(String baseUrl) throws IOException {
        assertThat(pageFor(baseUrl, API_DOC).getTitleText()).isEqualTo("nwbio - API");
    }

    @ParameterizedTest
    @ValueSource(strings = {DEVELOPMENT})
    public void testApiDocPageHeaders(String baseUrl) throws IOException {
        assertThat(pageFor(baseUrl, API_DOC).asNormalizedText())
                .contains("lobid-gnd API")
                .contains("Richtlinien zur API-Nutzung")
                .contains("Suche: /search?q=text")
                .contains("Direktzugriff: /<id>.json")
                .contains("Inhaltstypen")
                .contains("Bulk-Downloads")
                .contains("Autovervollständigung")
                .contains("JSON-LD")
                .contains("OpenRefine");
    }

    @ParameterizedTest
    @ValueSource(strings = {DEVELOPMENT})
    public void testSearchAllExample(String baseUrl) throws IOException {
        assertThat(pageFor(baseUrl, API_DOC).asNormalizedText())
                .contains("Alles")
                .contains("/search?q=*&format=json");
    }

    @ParameterizedTest
    @ValueSource(strings = {DEVELOPMENT})
    public void testSearchAllJsonEndpoint(String baseUrl) throws IOException, InterruptedException {
        assertThat(fetchHttpResponse(baseUrl, "search?q=*&format=json"))
                .is(validJson())
                .contains("\"totalItems\"")
                .contains("\"member\"");
    }

    @ParameterizedTest
    @ValueSource(strings = {DEVELOPMENT})
    public void testSearchAllFieldsExample(String baseUrl) throws IOException {
        assertThat(pageFor(baseUrl, API_DOC).asNormalizedText())
                .contains("Alle Felder")
                .contains("/search?q=london&format=json");
    }

    @ParameterizedTest
    @ValueSource(strings = {DEVELOPMENT})
    public void testSearchAllFieldsJsonEndpoint(String baseUrl)
            throws IOException, InterruptedException {
        assertThat(fetchHttpResponse(baseUrl, "search?q=london&format=json"))
                .is(validJson())
                .contains("\"totalItems\"")
                .contains("London");
    }

    @ParameterizedTest
    @ValueSource(strings = {DEVELOPMENT})
    public void testFieldSearchExample(String baseUrl) throws IOException {
        assertThat(pageFor(baseUrl, API_DOC).asNormalizedText())
                .contains("Feldsuche")
                .contains("/search?q=preferredName:Twain&format=json");
    }

    @ParameterizedTest
    @ValueSource(strings = {DEVELOPMENT})
    public void testFilterSearchExample(String baseUrl) throws IOException {
        assertThat(pageFor(baseUrl, API_DOC).asNormalizedText())
                .contains("Filter")
                .contains("filter=type:Person");
    }

    @ParameterizedTest
    @ValueSource(strings = {DEVELOPMENT})
    public void testPaginationExample(String baseUrl) throws IOException {
        assertThat(pageFor(baseUrl, API_DOC).asNormalizedText())
                .contains("Paginierung")
                .contains("from=50")
                .contains("size=100");
    }

    @ParameterizedTest
    @ValueSource(strings = {DEVELOPMENT})
    public void testSortingExample(String baseUrl) throws IOException {
        assertThat(pageFor(baseUrl, API_DOC).asNormalizedText())
                .contains("Sortierung")
                .contains("sort=preferredName.keyword:asc");
    }

    @ParameterizedTest
    @ValueSource(strings = {DEVELOPMENT})
    public void testAsciiSearchExample(String baseUrl) throws IOException {
        assertThat(pageFor(baseUrl, API_DOC).asNormalizedText())
                .contains("ASCII")
                .contains("preferredName.ascii:Chor")
                .contains("variantName.ascii:Chor");
    }

    @ParameterizedTest
    @ValueSource(strings = {DEVELOPMENT})
    public void testDirectAccessExamples(String baseUrl) throws IOException {
        assertThat(pageFor(baseUrl, API_DOC).asNormalizedText())
                .contains("Direktzugriff: /<id>.json")
                .contains("London")
                .contains("/4074335-4.json")
                .contains("hbz")
                .contains("/2047974-8.json")
                .contains("Goethe")
                .contains("/118540238.json");
    }

    @ParameterizedTest
    @ValueSource(strings = {DEVELOPMENT})
    public void testDirectAccessAlbertJsonEndpoint(String baseUrl)
            throws IOException, InterruptedException {
        assertThat(fetchHttpResponse(baseUrl, ALBERT + ".json"))
                .is(validJson())
                .contains("\"id\"")
                .contains(ALBERT);
    }

    @ParameterizedTest
    @ValueSource(strings = {DEVELOPMENT})
    public void testContentTypeDocumentation(String baseUrl) throws IOException {
        assertThat(pageFor(baseUrl, API_DOC).asNormalizedText())
                .contains("Content-Negotiation")
                .contains("Accept-Header")
                .contains("application/json")
                .contains("text/html")
                .contains("format=json");
    }

    @ParameterizedTest
    @ValueSource(strings = {DEVELOPMENT})
    public void testRdfSerializationDocumentation(String baseUrl) throws IOException {
        assertThat(pageFor(baseUrl, API_DOC).asNormalizedText())
                .contains("RDF")
                .contains("application/rdf+xml")
                .contains("text/turtle")
                .contains("application/n-triples")
                .contains(".rdf")
                .contains(".ttl")
                .contains(".nt");
    }

    @ParameterizedTest
    @ValueSource(strings = {DEVELOPMENT})
    public void testRdfXmlFormatEndpoint(String baseUrl) throws IOException, InterruptedException {
        assertThat(fetchHttpResponse(baseUrl, ALBERT + ".rdf"))
                .contains("rdf:RDF")
                .contains(ALBERT);
    }

    @ParameterizedTest
    @ValueSource(strings = {DEVELOPMENT})
    public void testTurtleFormatEndpoint(String baseUrl) throws IOException, InterruptedException {
        assertThat(fetchHttpResponse(baseUrl, ALBERT + ".ttl"))
                .contains("@prefix")
                .contains(ALBERT);
    }

    @ParameterizedTest
    @ValueSource(strings = {DEVELOPMENT})
    public void testNTriplesFormatEndpoint(String baseUrl)
            throws IOException, InterruptedException {
        String response = fetchHttpResponse(baseUrl, ALBERT + ".nt");
        assertThat(response).contains(ALBERT);
        assertThat(response.split("\n").length).isGreaterThan(0);
    }

    @ParameterizedTest
    @ValueSource(strings = {DEVELOPMENT})
    public void testBulkDownloadsDocumentation(String baseUrl) throws IOException {
        assertThat(pageFor(baseUrl, API_DOC).asNormalizedText())
                .contains("Bulk-Downloads")
                .contains("JSON lines")
                .contains("application/x-jsonlines")
                .contains("format=jsonl")
                .contains("gzip")
                .contains("Accept-Encoding");
    }

    @ParameterizedTest
    @ValueSource(strings = {DEVELOPMENT})
    public void testBulkDownloadJsonLinesEndpoint(String baseUrl)
            throws IOException, InterruptedException {
        String[] lines =
                fetchHttpResponse(baseUrl, "search?q=type:Family&format=jsonl").split("\n");
        assertThat(lines.length).isGreaterThan(0);
        assertThat(lines[0]).is(validJson());
    }

    @ParameterizedTest
    @ValueSource(strings = {DEVELOPMENT})
    public void testAutocompleteDocumentation(String baseUrl) throws IOException {
        assertThat(pageFor(baseUrl, API_DOC).asNormalizedText())
                .contains("Autovervollständigung")
                .contains("json:suggest")
                .contains("json:preferredName")
                .contains("professionOrOccupation")
                .contains("Feld-Templates");
    }

    @ParameterizedTest
    @ValueSource(strings = {DEVELOPMENT})
    public void testAutocompleteCodeExample(String baseUrl) throws IOException {
        assertThat(pageFor(baseUrl, API_DOC).asNormalizedText())
                .contains("input.search-gnd")
                .contains(".autocomplete")
                .contains("url")
                .contains(": \"/search\"")
                .contains("dataType")
                .contains(": \"jsonp\"")
                .contains("q")
                .contains(": request.term")
                .contains("format")
                .contains(": \"json:preferredName,professionOrOccupation\"");
    }

    @ParameterizedTest
    @ValueSource(strings = {DEVELOPMENT})
    public void testAutocompleteSuggestEndpoint(String baseUrl)
            throws IOException, InterruptedException {
        assertThat(fetchHttpResponse(baseUrl, "search?q=Pseudo-Albertus&format=json:suggest"))
                .is(validJson())
                .contains("Albertus, Magnus");
    }

    @ParameterizedTest
    @ValueSource(strings = {DEVELOPMENT})
    public void testApiCallEntityFromBrowser(String baseUrl)
            throws IOException, InterruptedException {
        assertApiEntityCallsContain(baseUrl, "json", "\"@context\" :");
        assertApiEntityCallsContain(baseUrl, "ttl", "@prefix schema");
        assertApiEntityCallsContain(baseUrl, "rdf", "rdf:RDF");
        assertApiEntityCallsContain(baseUrl, "nt", "\"Köln\" .");
        assertApiEntityCallsContain(baseUrl, "html", "<title>Albertus, Magnus, Heiliger</title>");
    }

    @ParameterizedTest
    @ValueSource(strings = {DEVELOPMENT})
    public void testApiCallSearchFromBrowser(String baseUrl)
            throws IOException, InterruptedException {
        assertApiSearchCallContains(baseUrl, "json", "\"@context\" :");
        assertApiSearchCallContains(baseUrl, "json:suggest", "\"category\" :");
        assertApiSearchCallContains(baseUrl, "json:preferredName", "\"category\" :");
    }

    private void assertApiSearchCallContains(String baseUrl, String format, String content)
            throws IOException, InterruptedException {
        assertThat(fetchHttpResponse(baseUrl, "search?format=" + format, "text/html"))
                .contains(content);
    }

    private void assertApiEntityCallsContain(String baseUrl, String format, String content)
            throws IOException, InterruptedException {
        assertThat(entity(baseUrl, "." + format)).contains(content);
        assertThat(entity(baseUrl, "?format=" + format)).contains(content);
    }

    private String entity(String baseUrl, String suffix) throws IOException, InterruptedException {
        return fetchHttpResponse(baseUrl, HtmlPageTests.ALBERT + suffix, "text/html");
    }

    @ParameterizedTest
    @ValueSource(strings = {DEVELOPMENT})
    public void testAutocompleteSuggestExample(String baseUrl) throws IOException {
        HtmlPage apiPage = pageFor(baseUrl, API_DOC);

        HtmlInput labelInput = apiPage.getFirstByXPath("//input[@id='label']");
        assertThat(labelInput).as("form input for label should exist").isNotNull();
        HtmlInput idInput = apiPage.getFirstByXPath("//input[@id='id']");
        assertThat(idInput).as("form input for ID should exist").isNotNull();
        HtmlButton searchButton = apiPage.getFirstByXPath("//button[contains(text(), 'Suchen')]");
        assertThat(searchButton).as("search button should exist").isNotNull();

        labelInput.type("Pseudo-Albert");
        webClient.waitForBackgroundJavaScript(3000);
        HtmlElement suggestion =
                apiPage.getFirstByXPath("//ul[contains(@class, 'ui-autocomplete')]/li/*[1]");
        String suggestionText =
                "Albertus, Magnus, Heiliger | Katholischer Theologe; Bischof; Philosoph; Alchemist;"
                        + " Naturwissenschaftler; Heiliger";
        assertThat(suggestion.asNormalizedText())
                .as("suggestion should contain details")
                .contains(suggestionText);

        if (suggestion instanceof HtmlAnchor) {
            suggestion.click();
        } else {
            apiPage.executeJavaScript(
                    "var menu = $('input.search-gnd').autocomplete('instance').menu;\n"
                            + "menu.focus(null, menu.element.find('.ui-menu-item'));\n"
                            + "menu.select();");
        }
        webClient.waitForBackgroundJavaScript(1000);

        assertThat(labelInput.getValue())
                .as("form should be filled with details for selected suggestion")
                .contains(suggestionText);
        assertThat(idInput.getValue())
                .as("form should be filled with ID search for selected suggestion")
                .contains("id:\"https://d-nb.info/gnd/118637649\"");
        HtmlPage searchResults = searchButton.click();
        assertThat(searchResults.asNormalizedText())
                .as("search results should contain the label for the searched ID")
                .contains("Albertus, Magnus");
    }

    @ParameterizedTest
    @ValueSource(strings = {DEVELOPMENT})
    public void testJsonLdDocumentation(String baseUrl) throws IOException {
        assertThat(pageFor(baseUrl, API_DOC).asNormalizedText())
                .contains("JSON-LD")
                .contains("JSON-LD Playground")
                .contains("JSON-LD Context")
                .contains("/context.jsonld")
                .contains("RDF-Konvertierung")
                .contains("jsonld-cli")
                .contains("N-Quads");
    }

    @ParameterizedTest
    @ValueSource(strings = {DEVELOPMENT})
    public void testJsonLdContextEndpoint(String baseUrl) throws IOException, InterruptedException {
        assertThat(fetchHttpResponse(baseUrl, "context.jsonld"))
                .is(validJson())
                .contains("\"@context\"");
    }

    @ParameterizedTest
    @ValueSource(strings = {DEVELOPMENT})
    public void testJsonLdCodeExamples(String baseUrl) throws IOException {
        assertThat(pageFor(baseUrl, API_DOC).asNormalizedText())
                .contains("jsonld format")
                .contains("curl")
                .contains("--header \"Accept: application/rdf+xml\"")
                .contains("--header \"Accept: text/turtle\"")
                .contains("--header \"Accept: application/n-triples\"");
    }

    @ParameterizedTest
    @ValueSource(strings = {DEVELOPMENT})
    public void testOpenRefineDocumentation(String baseUrl) throws IOException {
        assertThat(pageFor(baseUrl, API_DOC).asNormalizedText())
                .contains("OpenRefine")
                .contains("GND");
    }

    @ParameterizedTest
    @ValueSource(strings = {DEVELOPMENT})
    public void testApiDocLinksExist(String baseUrl) throws IOException {
        assertThat(pageFor(baseUrl, API_DOC).getElementsByTagName("a").toString())
                .contains("/search?q=")
                .contains("/4074335-4")
                .contains("/context.jsonld")
                .contains("http://json-ld.org/playground/")
                .contains("https://github.com/digitalbazaar/jsonld-cli")
                .contains("/reconcile")
                .contains("http://lobid.org/usage-policy")
                .contains("http://blog.lobid.org");
    }
}
