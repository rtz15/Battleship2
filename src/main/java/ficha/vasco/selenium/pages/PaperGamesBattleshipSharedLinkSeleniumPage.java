package ficha.vasco.selenium.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.ElementClickInterceptedException;
import org.openqa.selenium.ElementNotInteractableException;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoSuchSessionException;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Page object for Vasco's shared-link acceptance suite.
 */
public class PaperGamesBattleshipSharedLinkSeleniumPage {
	private static final String BATTLESHIP_URL = "https://papergames.io/en/battleship";
	private static final By BODY = By.tagName("body");
	private static final By NICKNAME_INPUT = By.cssSelector("input[formcontrolname='username'], input[placeholder='Nickname']");
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

	public PaperGamesBattleshipSharedLinkSeleniumPage(WebDriver driver) {
		this.driver = driver;
		this.wait = new WebDriverWait(driver, Duration.ofSeconds(20));
	}

	public PaperGamesBattleshipSharedLinkSeleniumPage openLandingPage() {
		for (int attempt = 0; attempt < 3; attempt++) {
			try {
				driver.get(BATTLESHIP_URL);
				waitForAnyBodyText("Battleship Online", "Battleship trực tuyến");
				prepareVisiblePage();
				return this;
			} catch (TimeoutException timedOut) {
				stopLoadingPage();
				if (hasAnyBodyText("Battleship Online", "Battleship trực tuyến")) {
					prepareVisiblePage();
					return this;
				}
			} catch (NoSuchSessionException sessionClosed) {
				throw sessionClosed;
			}
		}
		waitForAnyBodyText("Battleship Online", "Battleship trực tuyến");
		prepareVisiblePage();
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

		wait.until(ExpectedConditions.urlContains("/r/"));
		return this;
	}

	public boolean hasSharedPlayOptionVisible() {
		return isButtonVisible(SHARED_GAME_LABELS);
	}

	public boolean isNicknamePromptVisible() {
		return nicknamePromptOpened(Duration.ofSeconds(10));
	}

	public boolean hasRoomUrl() {
		try {
			new WebDriverWait(driver, Duration.ofSeconds(20))
					.until(ExpectedConditions.urlContains("/r/"));
			return true;
		} catch (TimeoutException ignored) {
			return false;
		}
	}

	public boolean hasShareIndicatorVisible() {
		return hasRoomUrl() || hasInviteControlVisible();
	}

	public boolean hasWaitingOrShareStateVisible() {
		return hasRoomUrl() && (hasInviteControlVisible() || hasAnyBodyText(SHARE_MARKERS));
	}

	public String currentUrl() {
		return driver.getCurrentUrl();
	}

	private void clickButtonAndWaitForNicknamePrompt(String... labels) {
		WebElement button = wait.until(ExpectedConditions.visibilityOfElementLocated(buttonContaining(labels)));
		clickElement(button);
		if (nicknamePromptOpened(Duration.ofSeconds(3))) {
			return;
		}

		button = wait.until(ExpectedConditions.visibilityOfElementLocated(buttonContaining(labels)));
		scrollIntoView(button);
		((JavascriptExecutor) driver).executeScript("arguments[0].click();", button);
	}

	private void clickElement(WebElement element) {
		scrollIntoView(element);
		try {
			new Actions(driver)
					.moveToElement(element)
					.pause(Duration.ofMillis(150))
					.click()
					.perform();
		} catch (ElementNotInteractableException | StaleElementReferenceException intercepted) {
			prepareVisiblePage();
			((JavascriptExecutor) driver).executeScript("arguments[0].click();", element);
		}
	}

	private void submitNickname(String nickname) {
		prepareVisiblePage();
		WebElement input = wait.until(ExpectedConditions.visibilityOfElementLocated(NICKNAME_INPUT));
		fillNickname(input, nickname);
		rejectConsentDialogIfVisible();

		WebElement continueButton = wait.until(ExpectedConditions.elementToBeClickable(buttonContaining("Continue", "Tiếp tục")));
		clickElement(continueButton);
	}

	private boolean sharedRoomReady(Duration timeout) {
		try {
			new WebDriverWait(driver, timeout)
					.until(webDriver -> webDriver.getCurrentUrl().contains("/r/")
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
		if (driver.getCurrentUrl().contains("/r/")) {
			return;
		}
		if (nicknamePromptOpened(Duration.ofSeconds(2))) {
			return;
		}
		openLandingPage();
		startSharedLinkGame();
	}

	private void fillNickname(WebElement input, String nickname) {
		scrollIntoView(input);
		try {
			input.clear();
			input.sendKeys(nickname);
		} catch (ElementNotInteractableException ignored) {
			setValueWithJavaScript(input, nickname);
			return;
		}

		if (!nickname.equals(input.getAttribute("value"))) {
			setValueWithJavaScript(input, nickname);
		}
	}

	private boolean hasInviteControlVisible() {
		try {
			WebElement element = wait.until(ExpectedConditions.visibilityOfElementLocated(inviteControl()));
			return element.isDisplayed();
		} catch (TimeoutException ignored) {
			return false;
		}
	}

	private boolean hasAnyBodyText(String... expectedTexts) {
		try {
			waitForAnyBodyText(expectedTexts);
			return true;
		} catch (TimeoutException ignored) {
			return false;
		}
	}

	private boolean bodyContainsAny(String... expectedTexts) {
		String bodyText = driver.findElement(BODY).getText();
		return Arrays.stream(expectedTexts).anyMatch(bodyText::contains);
	}

	private boolean isButtonVisible(String... labels) {
		try {
			WebElement button = wait.until(ExpectedConditions.visibilityOfElementLocated(buttonContaining(labels)));
			return button.isDisplayed();
		} catch (TimeoutException ignored) {
			return false;
		}
	}

	private void prepareVisiblePage() {
		waitForClientApp();
		hideExternalOverlaysIfVisible();
	}

	private void stopLoadingPage() {
		try {
			((JavascriptExecutor) driver).executeScript("window.stop();");
		} catch (NoSuchSessionException ignored) {
			throw ignored;
		} catch (RuntimeException ignored) {
			// If the renderer is still recovering, the following wait will decide whether the page is usable.
		}
	}

	private void hideExternalOverlaysIfVisible() {
		((JavascriptExecutor) driver).executeScript(
				"document.querySelectorAll(\"[class^='fc-'], [class*=' fc-'], iframe[id^='googlefc']\")"
						+ ".forEach(function(element) { element.remove(); });");
	}

	private void scrollIntoView(WebElement element) {
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

	private void waitForAnyBodyText(String... expectedTexts) {
		wait.until(webDriver -> bodyContainsAny(expectedTexts));
	}

	private void waitForClientApp() {
		wait.until(webDriver -> "complete".equals(((JavascriptExecutor) webDriver)
				.executeScript("return document.readyState")));
		try {
			new WebDriverWait(driver, Duration.ofSeconds(5))
					.until(webDriver -> Boolean.TRUE.equals(((JavascriptExecutor) webDriver).executeScript(
							"if (!window.getAllAngularTestabilities) { return true; }"
									+ "return window.getAllAngularTestabilities().every(function(testability) {"
									+ "return testability.isStable();"
									+ "});")));
		} catch (TimeoutException ignored) {
			// The live site keeps background polling active, so document readiness is the reliable signal here.
		}
	}

	private By buttonContaining(String... labels) {
		return By.xpath("//button[" + containsAnyNormalizedText(labels) + "]");
	}

	private By inviteControl() {
		return By.xpath("//*[self::a or self::button or self::input or self::textarea][" +
				"contains(@href, '/r/') or contains(@value, '/r/') or contains(@placeholder, 'link') or "
				+ containsAnyNormalizedText(SHARE_MARKERS) + "]");
	}

	private void rejectConsentDialogIfVisible() {
		List<WebElement> rejectButtons = driver.findElements(buttonContaining("Do not consent", "Không đồng ý"));
		for (WebElement rejectButton : rejectButtons) {
			if (rejectButton.isDisplayed() && rejectButton.isEnabled()) {
				scrollIntoView(rejectButton);
				try {
					rejectButton.click();
				} catch (ElementClickInterceptedException intercepted) {
					((JavascriptExecutor) driver).executeScript("arguments[0].click();", rejectButton);
				}
				return;
			}
		}
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
