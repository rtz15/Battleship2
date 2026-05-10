package ficha.vasco.selenium.pages;

import ficha.eduardo.selenium.pages.PaperGamesBattleshipSeleniumPage;
import org.openqa.selenium.By;
import org.openqa.selenium.ElementNotInteractableException;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * Page object for Vasco's shared-link acceptance suite.
 */
public class PaperGamesBattleshipSharedLinkSeleniumPage {
	private static final By BODY = By.tagName("body");
	private static final By NICKNAME_INPUT = By.cssSelector("input[formcontrolname='username'], input[placeholder='Nickname']");
	private static final String ROOM_URL_FRAGMENT = "/r/";
	private static final String JAVASCRIPT_CLICK = "arguments[0].click();";
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

	private final WebDriver driver;
	private final WebDriverWait wait;
	private final PaperGamesBattleshipSeleniumPage onboardingPage;

	public PaperGamesBattleshipSharedLinkSeleniumPage(WebDriver driver) {
		this.driver = driver;
		this.wait = new WebDriverWait(driver, Duration.ofSeconds(20));
		this.onboardingPage = new PaperGamesBattleshipSeleniumPage(driver);
	}

	public PaperGamesBattleshipSharedLinkSeleniumPage openLandingPage() {
		onboardingPage.openLandingPage();
		return this;
	}

	public PaperGamesBattleshipSharedLinkSeleniumPage startSharedLinkGame() {
		for (int attempt = 0; attempt < 4; attempt++) {
			prepareVisiblePage();
			if (nicknamePromptOpened(Duration.ofSeconds(2))) {
				return this;
			}
			clickButtonAndWaitForNicknamePrompt(SHARED_GAME_LABELS);
			if (nicknamePromptOpened(Duration.ofSeconds(12))) {
				return this;
			}
		}
		wait.until(ExpectedConditions.visibilityOfElementLocated(NICKNAME_INPUT));
		return this;
	}

	public PaperGamesBattleshipSharedLinkSeleniumPage chooseNickname(String nickname) {
		for (int attempt = 0; attempt < 5; attempt++) {
			waitForNicknamePrompt();
			submitNickname(nickname);
			if (sharedRoomReady(Duration.ofSeconds(25))) {
				return this;
			}
			recoverSharedGameFlow();
		}

		wait.until(ExpectedConditions.urlContains(ROOM_URL_FRAGMENT));
		return this;
	}

	public boolean hasSharedPlayOptionVisible() {
		return onboardingPage.isButtonVisible(SHARED_GAME_LABELS);
	}

	public boolean isNicknamePromptVisible() {
		return nicknamePromptOpened(Duration.ofSeconds(10));
	}

	public boolean hasRoomUrl() {
		try {
			new WebDriverWait(driver, Duration.ofSeconds(20))
					.until(ExpectedConditions.urlContains(ROOM_URL_FRAGMENT));
			return true;
		} catch (TimeoutException ignored) {
			return false;
		}
	}

	public boolean hasShareIndicatorVisible() {
		return hasRoomUrl() || hasInviteControlVisible();
	}

	public boolean hasWaitingOrShareStateVisible() {
		return hasRoomUrl() && (hasInviteControlVisible() || onboardingPage.hasAnyBodyText(SHARE_MARKERS));
	}

	public String currentUrl() {
		return onboardingPage.currentUrl();
	}

	private void clickButtonAndWaitForNicknamePrompt(String... labels) {
		WebElement button = wait.until(ExpectedConditions.elementToBeClickable(buttonContaining(labels)));
		clickWithFallback(button);
		if (nicknamePromptOpened(Duration.ofSeconds(3))) {
			return;
		}

		clickWithJavaScript(wait.until(ExpectedConditions.visibilityOfElementLocated(buttonContaining(labels))));
	}

	private void clickWithFallback(WebElement element) {
		scrollToCenter(element);
		try {
			element.click();
		} catch (ElementNotInteractableException intercepted) {
			prepareVisiblePage();
			clickWithJavaScript(element);
		}
	}

	private void submitNickname(String nickname) {
		prepareVisiblePage();
		WebElement input = wait.until(ExpectedConditions.visibilityOfElementLocated(NICKNAME_INPUT));
		fillNickname(input, nickname);
		rejectConsentDialogIfVisible();

		WebElement continueButton = wait.until(ExpectedConditions.elementToBeClickable(buttonContaining("Continue", "Tiếp tục")));
		clickWithFallback(continueButton);
	}

	private boolean sharedRoomReady(Duration timeout) {
		try {
			new WebDriverWait(driver, timeout)
					.until(webDriver -> webDriver.getCurrentUrl().contains(ROOM_URL_FRAGMENT)
							|| bodyContainsAny(SHARE_MARKERS));
			rejectConsentDialogIfVisible();
			return true;
		} catch (TimeoutException ignored) {
			return false;
		}
	}

	private boolean nicknamePromptOpened(Duration timeout) {
		try {
			new WebDriverWait(driver, timeout)
					.until(ExpectedConditions.visibilityOfElementLocated(NICKNAME_INPUT));
			return true;
		} catch (TimeoutException ignored) {
			return false;
		}
	}

	private void waitForNicknamePrompt() {
		prepareVisiblePage();
		if (nicknamePromptOpened(Duration.ofSeconds(5))) {
			return;
		}
		startSharedLinkGame();
		wait.until(ExpectedConditions.visibilityOfElementLocated(NICKNAME_INPUT));
	}

	private void recoverSharedGameFlow() {
		prepareVisiblePage();
		if (driver.getCurrentUrl().contains(ROOM_URL_FRAGMENT)) {
			return;
		}
		if (nicknamePromptOpened(Duration.ofSeconds(2))) {
			return;
		}
		openLandingPage();
		startSharedLinkGame();
	}

	private void fillNickname(WebElement input, String nickname) {
		scrollToCenter(input);
		setValueWithJavaScript(input, nickname);
	}

	private boolean hasInviteControlVisible() {
		try {
			WebElement element = wait.until(ExpectedConditions.visibilityOfElementLocated(inviteControl()));
			return element.isDisplayed();
		} catch (TimeoutException ignored) {
			return false;
		}
	}

	private boolean bodyContainsAny(String... expectedTexts) {
		String bodyText = driver.findElement(BODY).getText();
		return Arrays.stream(expectedTexts).anyMatch(bodyText::contains);
	}

	private void prepareVisiblePage() {
		hideExternalOverlaysIfVisible();
	}

	private void hideExternalOverlaysIfVisible() {
		((JavascriptExecutor) driver).executeScript(
				"document.querySelectorAll(\"[class^='fc-'], [class*=' fc-'], iframe[id^='googlefc']\")"
						+ ".forEach(function(element) { element.remove(); });");
	}

	private void scrollToCenter(WebElement element) {
		((JavascriptExecutor) driver).executeScript(
				"arguments[0].scrollIntoView({block: 'center', inline: 'center'});", element);
	}

	private void setValueWithJavaScript(WebElement input, String nickname) {
		((JavascriptExecutor) driver).executeScript(
				"arguments[0].value = '';"
						+ "arguments[0].dispatchEvent(new Event('input', { bubbles: true }));"
						+ "arguments[0].value = arguments[1];"
						+ "arguments[0].dispatchEvent(new Event('input', { bubbles: true }));"
						+ "arguments[0].dispatchEvent(new Event('change', { bubbles: true }));",
				input,
				nickname
		);
	}

	private void clickWithJavaScript(WebElement element) {
		((JavascriptExecutor) driver).executeScript(JAVASCRIPT_CLICK, element);
	}

	private By buttonContaining(String... labels) {
		return By.xpath("//button[" + containsAnyNormalizedText(labels) + "]");
	}

	private By inviteControl() {
		return By.xpath("//*[self::a or self::button or self::input or self::textarea][" +
				"contains(@href, " + xpathLiteral(ROOM_URL_FRAGMENT) + ") or contains(@value, "
				+ xpathLiteral(ROOM_URL_FRAGMENT) + ") or contains(@placeholder, 'link') or "
				+ containsAnyNormalizedText(SHARE_MARKERS) + "]");
	}

	private void rejectConsentDialogIfVisible() {
		for (WebElement button : driver.findElements(buttonContaining("Do not consent", "Không đồng ý"))) {
			if (button.isDisplayed() && button.isEnabled()) {
				clickWithFallback(button);
				return;
			}
		}
	}

	private String containsAnyNormalizedText(String... labels) {
		return Arrays.stream(labels).map(this::normalizedTextContains).collect(Collectors.joining(" or "));
	}

	private String normalizedTextContains(String label) {
		return "contains(normalize-space(.), " + xpathLiteral(label) + ")";
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
