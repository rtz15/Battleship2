package ficha5.tiago.pages;

import com.codeborne.selenide.SelenideElement;
import ficha5.eduardo.pages.PaperGamesBattleshipPage;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.Arrays;
import java.util.stream.Collectors;

import static com.codeborne.selenide.Condition.enabled;
import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.$x;
import static com.codeborne.selenide.Selenide.Wait;
import static com.codeborne.selenide.Selenide.executeJavaScript;
import static com.codeborne.selenide.WebDriverConditions.urlContaining;
import static com.codeborne.selenide.WebDriverRunner.getWebDriver;
import static com.codeborne.selenide.WebDriverRunner.url;
import static com.codeborne.selenide.Selenide.webdriver;

/**
 * Page object for Tiago's Ficha 5 acceptance suite.
 */
public class PaperGamesBattleshipSessionPage {
	private final PaperGamesBattleshipPage onboardingPage = new PaperGamesBattleshipPage();

	public PaperGamesBattleshipSessionPage openLandingPage() {
		onboardingPage.openLandingPage();
		return this;
	}

	public PaperGamesBattleshipSessionPage startRobotGame() {
		onboardingPage.startRobotGame();
		return this;
	}

	public PaperGamesBattleshipSessionPage chooseNickname(String nickname) {
		onboardingPage.chooseNickname(nickname);
		return this;
	}

	public PaperGamesBattleshipSessionPage assertRankingsAndLeaderboardAreVisible() {
		elementContaining("Daily leaderboard").shouldBe(visible);
		elementContaining("RANKA").shouldBe(visible);
		elementContaining("General ranking since 30 days.").shouldBe(visible);
		return this;
	}

	public PaperGamesBattleshipSessionPage assertTournamentLinksAreAvailable() {
		linkContaining("Create tournament", "/en/t/create-tournament").shouldBe(visible);
		linkContaining("My tournaments", "/en/t/my-tournaments").shouldBe(visible);
		return this;
	}

	public PaperGamesBattleshipSessionPage assertInitialBattleState(String nickname) {
		webdriver().shouldHave(urlContaining("/r/"));
		bodyShouldContainAny(nickname);
		bodyShouldContainAny("Paper Man");
		elementContaining("Your boats").shouldBe(visible);
		elementContaining("Your opponent's boats").shouldBe(visible);
		elementContaining("Attack your opponent!").shouldBe(visible);
		return this;
	}

	public PaperGamesBattleshipSessionPage openAbortConfirmation() {
		buttonContaining("Abort game").shouldBe(visible, enabled).click();
		elementContaining("Are you sure you want to continue?").shouldBe(visible);
		return this;
	}

	public PaperGamesBattleshipSessionPage assertAbortConfirmationIsVisible() {
		elementContaining("Are you sure you want to continue?").shouldBe(visible);
		buttonContaining("Cancel").shouldBe(visible);
		lastButtonContaining("Abort game").shouldBe(visible, enabled);
		return this;
	}

	public PaperGamesBattleshipSessionPage confirmAbortGame() {
		lastButtonContaining("Abort game").shouldBe(visible, enabled).click();
		webdriver().shouldHave(urlContaining("/en/battleship"));
		bodyShouldContainAny("Battleship Online");
		return this;
	}

	public PaperGamesBattleshipSessionPage assertLandingPageRestoredAfterAbort() {
		bodyShouldContainAny("Battleship Online");
		assertRankingsAndLeaderboardAreVisible();
		return this;
	}

	public String currentUrl() {
		return url();
	}

	private SelenideElement buttonContaining(String... labels) {
		return $x("//*[" + containsAnyNormalizedText(labels) + " and self::button]");
	}

	private SelenideElement lastButtonContaining(String... labels) {
		return $x("(//*[self::button and (" + containsAnyNormalizedText(labels) + ")])[last()]");
	}

	private SelenideElement linkContaining(String label, String hrefFragment) {
		return $x("//a[contains(normalize-space(.), " + xpathLiteral(label) + ") and contains(@href, "
				+ xpathLiteral(hrefFragment) + ")]");
	}

	private SelenideElement elementContaining(String... labels) {
		return $x("//*[" + containsAnyNormalizedText(labels) + "]");
	}

	private void bodyShouldContainAny(String... expectedTexts) {
		Wait().until(driver -> {
			String bodyText = $("body").getText();
			return Arrays.stream(expectedTexts).anyMatch(bodyText::contains);
		});
	}

	private void waitForClientApp() {
		Wait().until(driver -> "complete".equals(executeJavaScript("return document.readyState")));
		try {
			new WebDriverWait(getWebDriver(), Duration.ofSeconds(5))
					.until(driver -> Boolean.TRUE.equals(((JavascriptExecutor) driver).executeScript(
							"if (!window.getAllAngularTestabilities) { return true; }"
									+ "return window.getAllAngularTestabilities().every(function(testability) {"
									+ "return testability.isStable();"
									+ "});")));
		} catch (TimeoutException ignored) {
			// The live site keeps background requests active, so document readiness is enough for these tests.
		}
	}

	private void prepareVisiblePage() {
		waitForClientApp();
		executeJavaScript(
				"document.querySelectorAll(\"[class^='fc-'], [class*=' fc-'], iframe[id^='googlefc']\")"
						+ ".forEach(function(element) { element.remove(); });");
	}

	private String containsAnyNormalizedText(String... labels) {
		prepareVisiblePage();
		return Arrays.stream(labels)
				.map(label -> "contains(normalize-space(.), " + xpathLiteral(label) + ")")
				.collect(Collectors.joining(" or "));
	}

	private String xpathLiteral(String value) {
		if (!value.contains("'")) {
			return "'" + value + "'";
		}
		if (!value.contains("\"")) {
			return "\"" + value + "\"";
		}
		return "concat('" + value.replace("'", "', \"'\", '") + "')";
	}
}
