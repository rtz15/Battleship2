package ficha5.vasco.pages;

import com.codeborne.selenide.SelenideElement;
import ficha.vasco.selenium.pages.PaperGamesBattleshipSharedLinkSeleniumPage;
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
import static com.codeborne.selenide.Selenide.webdriver;

/**
 * Page object for Vasco's Ficha 5 shared-link acceptance suite.
 */
public class PaperGamesBattleshipSharedLinkPage {
	private static final String BATTLESHIP_URL = "https://papergames.io/en/battleship";
	private static final String[] SHARED_GAME_LABELS = {
			"Play with a friend",
			"Play with friend",
			"Invite a friend",
			"Private game",
			"Create private room",
			"Jogar com um amigo",
			"Chơi với một người bạn"
	};
	private static final String[] SHARE_MARKERS = {
			"Share",
			"Invite",
			"Copy",
			"link",
			"Link",
			"friend",
			"Friend",
			"Waiting",
			"Wait",
			"room",
			"Room"
	};

	public PaperGamesBattleshipSharedLinkPage openLandingPage() {
		open(BATTLESHIP_URL);
		bodyShouldContainAny("Battleship Online", "Battleship trực tuyến");
		prepareVisiblePage();
		return this;
	}

	public PaperGamesBattleshipSharedLinkPage assertSharedLinkOptionIsAvailable() {
		buttonContaining(SHARED_GAME_LABELS).shouldBe(visible, enabled);
		return this;
	}

	public PaperGamesBattleshipSharedLinkPage startSharedLinkGame() {
		seleniumPage().startSharedLinkGame();
		return this;
	}

	public PaperGamesBattleshipSharedLinkPage assertNicknamePromptIsVisible() {
		nicknameInput().shouldBe(visible, enabled);
		buttonContaining("Continue", "Tiếp tục").shouldBe(visible);
		return this;
	}

	public PaperGamesBattleshipSharedLinkPage chooseNickname(String nickname) {
		seleniumPage().chooseNickname(nickname);
		return this;
	}

	public PaperGamesBattleshipSharedLinkPage assertSharedRoomUrlIsCreated() {
		webdriver().shouldHave(urlContaining("/r/"));
		return this;
	}

	public PaperGamesBattleshipSharedLinkPage assertShareOrWaitingStateIsVisible() {
		webdriver().shouldHave(urlContaining("/r/"));
		if (hasInviteControlVisible()) {
			return this;
		}
		bodyShouldContainAny(SHARE_MARKERS);
		return this;
	}

	private SelenideElement nicknameInput() {
		return $("input[formcontrolname='username'], input[placeholder='Nickname']");
	}

	private boolean hasInviteControlVisible() {
		try {
			inviteControl().shouldBe(visible);
			return true;
		} catch (AssertionError ignored) {
			return false;
		}
	}

	private SelenideElement buttonContaining(String... labels) {
		return $x("//*[" + containsAnyNormalizedText(labels) + " and self::button]");
	}

	private SelenideElement inviteControl() {
		return $x("//*[self::a or self::button or self::input or self::textarea]["
				+ "contains(@href, '/r/') or contains(@value, '/r/') or contains(@placeholder, 'link') or "
				+ containsAnyNormalizedText(SHARE_MARKERS) + "]");
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

	private PaperGamesBattleshipSharedLinkSeleniumPage seleniumPage() {
		return new PaperGamesBattleshipSharedLinkSeleniumPage(getWebDriver());
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
