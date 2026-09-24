package org.lobid.gnd.ui;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import org.assertj.core.api.Condition;
import org.htmlunit.html.DomAttr;
import org.htmlunit.html.HtmlAnchor;
import org.htmlunit.html.HtmlButton;
import org.htmlunit.html.HtmlInput;
import org.htmlunit.html.HtmlListItem;
import org.htmlunit.html.HtmlPage;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/* Tests for the `/search` results page */
public class SearchTests extends HtmlPageTests {

    private static final String SEARCH = "/search";

    @ParameterizedTest
    @ValueSource(strings = {DEVELOPMENT})
    public void testTrailingSlashSearch(String baseUrl) throws IOException {
        String searchPageText = pageFor(baseUrl, SEARCH + "/").asNormalizedText();
        assertThat(searchPageText).contains("Treffer, zeige 1 bis 10");
    }

    @ParameterizedTest
    @ValueSource(strings = {DEVELOPMENT})
    public void testSearchForm(String baseUrl) throws IOException {
        HtmlPage searchPage = pageFor(baseUrl, SEARCH);
        HtmlInput searchBox = searchPage.getFirstByXPath("//input[@id='gnd-query']");
        assertThat(searchBox).as("search box should exist").isNotNull();
        HtmlButton searchButton = searchPage.getFirstByXPath("//button[@title='Suchen']");
        assertThat(searchButton).as("search button should exist").isNotNull();
    }

    @ParameterizedTest
    @ValueSource(strings = {DEVELOPMENT})
    public void testSearchFormClear(String baseUrl) throws IOException {
        HtmlPage searchPage = pageFor(baseUrl, SEARCH);
        HtmlInput searchBox = searchPage.getFirstByXPath("//input[@id='gnd-query']");
        searchBox.type("Test");
        assertThat(searchBox.getValue()).isEqualTo("Test");
        HtmlButton clearButton =
                searchPage.getFirstByXPath("//button[contains(@class, 'ui-autocomplete-clear')]");
        clearButton.click();
        assertThat(searchBox.getValue()).as("search box should be empty after clearing").isEmpty();
    }

    @ParameterizedTest
    @ValueSource(strings = {DEVELOPMENT})
    public void testPageSize(String baseUrl) throws IOException {
        assertThat(search("Test", baseUrl))
                .as("page size can be switched, default is 10")
                .is(linkActive("10"))
                .is(linkActiveAfterClick("30"))
                .is(linkActiveAfterClick("50"))
                .is(linkActiveAfterClick("100"));
    }

    @ParameterizedTest
    @ValueSource(strings = {DEVELOPMENT})
    public void testPageLinks(String baseUrl) throws IOException {
        assertThat(search("NRW", baseUrl))
                .as("specific page can be selected, default is 1")
                .is(linkActive("1"))
                .is(linkActiveAfterClick("2"))
                .is(linkActiveAfterClick("3"))
                .is(linkActiveAfterClick("4"))
                .is(linkActiveAfterClick("5"))
                .is(linkActiveAfterClick("6"))
                .is(linkActiveAfterClick("7"))
                .is(linkActiveAfterClick("8"))
                .is(linkActiveAfterClick("9"))
                .is(linkActiveAfterClick("10"));
    }

    @ParameterizedTest
    @ValueSource(strings = {DEVELOPMENT})
    public void testSearchResults(String baseUrl) throws IOException {
        HtmlPage searchPage = search("Pseudo-Albert", baseUrl);
        DomAttr detailsLink =
                searchPage.getFirstByXPath("//a[text()='Albertus, Magnus, Heiliger']/@href");
        assertThat(detailsLink.getValue())
                .as("each result should link to its details page")
                .contains("/118637649");
        String searchResults = searchPage.asNormalizedText();
        assertThat(searchResults)
                .as("the search has main navigation, results, and facets")
                .contains("Treffer pro Seite")
                .contains("Treffer, zeige 1 bis")
                .contains("Ergebnisse eingrenzen");
        assertThat(searchResults)
                .as("the results contains details for each entity")
                .contains("118637649")
                .contains(
                        "Katholischer Theologe",
                        "Bischof",
                        "Philosoph",
                        "Alchemist",
                        "Naturwissenschaftler")
                .contains("1193–1280");
        assertThat(searchResults)
                .as("the facets contain values from the search results")
                .contains("GND-Sachgruppe")
                .contains("Personen zu Philosophie")
                .contains("Ländercode")
                .contains("Deutschland")
                .contains("Beruf oder Beschäftigung");
    }

    @ParameterizedTest
    @ValueSource(strings = {DEVELOPMENT})
    public void testFacetLinks(String baseUrl) throws IOException {
        assertThat(search("Pseudo-Albert", baseUrl))
                .has(
                        linkFor(
                                "Personen zu Natur, Naturwissenschaften allgemein",
                                "gndSubjectCategory.id",
                                "\"https://d-nb.info/standards/vocab/gnd/gnd-sc#18p\""))
                .has(
                        linkFor(
                                "Deutschland",
                                "geographicAreaCode.id",
                                "\"https://d-nb.info/standards/vocab/gnd/geographic-area-code#XA-DE\""))
                .has(
                        linkFor(
                                "Katholischer Theologe",
                                "professionOrOccupation.id",
                                "\"https://d-nb.info/gnd/4030020-1\""))
                .has(
                        linkFor(
                                "Alchemist",
                                "professionOrOccupation.id",
                                "\"https://d-nb.info/gnd/4212680-0\""))
                .has(
                        linkFor(
                                "Naturwissenschaftler",
                                "professionOrOccupation.id",
                                "\"https://d-nb.info/gnd/4041423-1\""));
    }

    @ParameterizedTest
    @ValueSource(strings = {DEVELOPMENT})
    public void testFacetFilter(String baseUrl) throws IOException {
        HtmlPage searchPage = search("Pseudo-Albert", baseUrl);
        assertThat(searchPage.getByXPath(linksToRemoveFilter()))
                .as("no filter should be set by default")
                .isEmpty();
        searchPage = addAndAssertFilters(searchPage, "Person", 1);
        searchPage = addAndAssertFilters(searchPage, "Philosophie", 2);
        searchPage = addAndAssertFilters(searchPage, "Deutschland", 3);
        searchPage = addAndAssertFilters(searchPage, "Philosoph", 4);
        searchPage = addAndAssertFilters(searchPage, "Naturwissenschaftler", 5);
        clickAndAssertFilters(searchPage, linksToRemoveFilter(), "remove", 4);
    }

    @ParameterizedTest
    @ValueSource(strings = {DEVELOPMENT})
    public void testAutocomplete(String baseUrl) throws IOException {
        HtmlPage searchPage = pageFor(baseUrl, SEARCH);
        webClient.getOptions().setCssEnabled(false);

        HtmlInput searchBox = searchPage.getFirstByXPath("//input[@id='gnd-query']");
        searchBox.type("Pseudo-Albert");
        webClient.waitForBackgroundJavaScript(1500);
        HtmlListItem suggestion =
                searchPage.getFirstByXPath("//ul[contains(@class, 'ui-autocomplete')]/li");
        assertThat(suggestion.asNormalizedText())
                .as("suggestion should contain details")
                .contains("Albertus, Magnus, Heiliger | 1193–1280")
                .contains("Philosoph; Alchemist; Naturwissenschaftler");

        HtmlPage detailsPage = suggestion.click();
        webClient.waitForBackgroundJavaScript(15);
        assertThat(detailsPage.asNormalizedText())
                .as("details page for selected suggestion should be open: " + detailsPage.getUrl())
                .contains("https://d-nb.info/gnd/118637649")
                .contains("Albertus, de Colonia");
    }

    private HtmlPage search(String searchQuery, String baseUrl) throws IOException {
        HtmlPage searchPage = pageFor(baseUrl, SEARCH);
        HtmlInput searchBox = searchPage.getFirstByXPath("//input[@id='gnd-query']");
        HtmlButton searchButton = searchPage.getFirstByXPath("//button[@title='Suchen']");
        searchBox.type(searchQuery);
        return searchButton.click();
    }

    private Condition<HtmlPage> linkFor(String text, String field, String value) {
        String filterParam = String.format("%s:%s", field, value);
        return new Condition<>(
                page -> hasLink(text, filterParam, page), "link for '%s' with: %s", text, field);
    }

    private boolean hasLink(String text, String filter, HtmlPage page) {
        DomAttr link = page.getFirstByXPath("//a[contains(text(), '" + text + "')]/@href");
        String linkText = URLDecoder.decode(link.getValue(), StandardCharsets.UTF_8);
        assertThat(linkText).contains(String.format("filter=%s", String.format("+(%s)", filter)));
        return true;
    }

    private Condition<HtmlPage> linkActive(String linkText) {
        return new Condition<>(
                page -> isActive(linkPath(linkText), page),
                "parent of '%s' or '%s' itself should be active",
                linkText,
                linkText);
    }

    private Condition<HtmlPage> linkActiveAfterClick(String linkText) {
        return new Condition<>(
                page -> isActiveAfterClick(linkPath(linkText), page),
                "parent of '%s' or '%s' itself should be active",
                linkText,
                linkText);
    }

    private String linkPath(String linkText) {
        return String.format("//a[text()='%s']", linkText);
    }

    private boolean isActive(String linkPath, HtmlPage page) {
        return (page.getFirstByXPath(linkPath + "[contains(@class,'active')]") != null)
                || (page.getFirstByXPath(linkPath + "/parent::*[contains(@class,'active')]")
                        != null);
    }

    private boolean isActiveAfterClick(String linkPath, HtmlPage page) {
        HtmlAnchor link = page.getFirstByXPath(linkPath);
        try {
            HtmlPage clickedPage = link.click();
            return isActive(linkPath, clickedPage);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private HtmlPage addAndAssertFilters(HtmlPage searchPage, String linkText, int expectedFilters)
            throws IOException {
        String linkPath = String.format("//a[contains(text(), '%s')]", linkText);
        return clickAndAssertFilters(searchPage, linkPath, "add", expectedFilters);
    }

    private String linksToRemoveFilter() {
        return "//span[contains(text(), 'Filter entfernen')]/parent::*";
    }

    private HtmlPage clickAndAssertFilters(
            HtmlPage searchPage, String linkPath, String details, int expectedFilters)
            throws IOException {
        HtmlAnchor link = searchPage.getFirstByXPath(linkPath);
        HtmlPage newPage = link.click();
        assertThat(newPage.getByXPath(linksToRemoveFilter()).size())
                .as("click on %s should %s filter", linkPath, details)
                .isEqualTo(expectedFilters);
        return newPage;
    }
}
