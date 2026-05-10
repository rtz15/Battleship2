package ficha5.eduardo.pages;

import com.codeborne.selenide.SelenideElement;
import ficha.eduardo.selenium.pages.PaperGamesBattleshipSeleniumPage;
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
import static com.codeborne.selenide.Selenide.open;
import static com.codeborne.selenide.WebDriverConditions.urlContaining;
import static com.codeborne.selenide.WebDriverRunner.getWebDriver;
import static com.codeborne.selenide.WebDriverRunner.url;
import static com.codeborne.selenide.Selenide.webdriver;

/**
 * Page object for the PaperGames Battleship landing page and robot game onboarding flow.
 */
public class PaperGamesBattleshipPage {
	private static final String BATTLESHIP_URL = "https://papergames.io/en/battleship";

	public PaperGamesBattleshipPage openLandingPage() {
		open(BATTLESHIP_URL);
		bodyShouldContainAny("Battleship Online", "Battleship trực tuyến");
		waitForClientApp();
		prepareVisiblePage();
		return this;
	}

	public PaperGamesBattleshipPage assertLandingPageIsLoaded() {
		bodyShouldContainAny("Battleship Online", "Battleship trực tuyến");
		bodyShouldContainAny("First to sink all opponent ships wins", "Người đầu tiên đánh chìm hết tàu đối thủ");
		return this;
	}

	public PaperGamesBattleshipPage assertMainEntryOptionsAreAvailable() {
		buttonContaining("Play with a friend", "Chơi với một người bạn").shouldBe(visible, enabled);
		buttonContaining("Play vs robot", "Chơi với robot").shouldBe(visible, enabled);
		buttonContaining("Play online", "Chơi trực tuyến").shouldBe(visible, enabled);
		elementContaining("Create tournament", "Tạo giải đấu").shouldBe(visible);
		return this;
	}

	public PaperGamesBattleshipPage assertRulesAndWeaponsAreDocumented() {
		bodyShouldContainAny("Rules of Battleship game online", "Luật chơi");
		bodyShouldContainAny("Each player has a 10x10 grid", "mạng lưới 10 × 10");
		bodyShouldContainAny("Weapons", "vũ khí");
		bodyShouldContainAny("Nuclear missile", "bom nguyên tử");
		return this;
	}

	public PaperGamesBattleshipPage startRobotGame() {
		seleniumPage().startRobotGame();
		return this;
	}

	public PaperGamesBattleshipPage assertNicknamePromptIsVisible() {
		nicknameInput().shouldBe(visible, enabled);
		buttonContaining("Continue", "Tiếp tục").shouldBe(visible);
		return this;
	}

	public PaperGamesBattleshipPage chooseNickname(String nickname) {
		seleniumPage().chooseNickname(nickname);
		return this;
	}

	public PaperGamesBattleshipPage assertRobotGameStarted(String nickname) {
		webdriver().shouldHave(urlContaining("/r/"));
		bodyShouldContainAny(nickname);
		bodyShouldContainAny("Paper Man");
		return this;
	}

	public String currentUrl() {
		return url();
	}

	private SelenideElement nicknameInput() {
		return $("input[formcontrolname='username'], input[placeholder='Nickname']");
	}

	private SelenideElement buttonContaining(String... labels) {
		return $x("//*[" + containsAnyNormalizedText(labels) + " and self::button]");
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
		hideExternalOverlaysIfVisible();
	}

	private void hideExternalOverlaysIfVisible() {
		executeJavaScript(
				"document.querySelectorAll(\"[class^='fc-'], [class*=' fc-'], iframe[id^='googlefc']\")"
						+ ".forEach(function(element) { element.remove(); });");
	}

	private PaperGamesBattleshipSeleniumPage seleniumPage() {
		return new PaperGamesBattleshipSeleniumPage(getWebDriver());
	}

	private String containsAnyNormalizedText(String... labels) {
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
