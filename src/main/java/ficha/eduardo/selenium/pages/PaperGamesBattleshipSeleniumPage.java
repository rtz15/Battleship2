package ficha.eduardo.selenium.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.ElementClickInterceptedException;
import org.openqa.selenium.ElementNotInteractableException;
import org.openqa.selenium.JavascriptExecutor;
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
 * Page object for the PaperGames Battleship page using direct Selenium WebDriver calls.
 */
public class PaperGamesBattleshipSeleniumPage {
	private static final String BATTLESHIP_URL = "https://papergames.io/en/battleship";
	private static final By BODY = By.tagName("body");
	private static final By NICKNAME_INPUT = By.cssSelector("input[formcontrolname='username'], input[placeholder='Nickname']");

	private final WebDriver driver;
	private final WebDriverWait wait;

	public PaperGamesBattleshipSeleniumPage(WebDriver driver) {
		this.driver = driver;
		this.wait = new WebDriverWait(driver, Duration.ofSeconds(20));
	}

	public PaperGamesBattleshipSeleniumPage openLandingPage() {
		driver.get(BATTLESHIP_URL);
		waitForAnyBodyText("Battleship Online", "Battleship trá»±c tuyáº¿n");
		waitForClientApp();
		prepareVisiblePage();
		return this;
	}

	public PaperGamesBattleshipSeleniumPage startRobotGame() {
		for (int attempt = 0; attempt < 4; attempt++) {
			waitForClientApp();
			prepareVisiblePage();
			if (nicknamePromptOpened(Duration.ofSeconds(2))) {
				return this;
			}
			clickButtonAndWaitForNicknamePrompt("Play vs robot", "ChÆ¡i vá»›i robot");
			if (nicknamePromptOpened(Duration.ofSeconds(12))) {
				return this;
			}
			prepareVisiblePage();
		}
		wait.until(ExpectedConditions.visibilityOfElementLocated(NICKNAME_INPUT));
		return this;
	}

	public PaperGamesBattleshipSeleniumPage chooseNickname(String nickname) {
		for (int attempt = 0; attempt < 5; attempt++) {
			waitForNicknamePrompt();
			submitNickname(nickname);
			if (roomOpened(Duration.ofSeconds(20))) {
				return this;
			}

			rejectConsentDialogIfVisible();
			if (nicknamePromptOpened(Duration.ofSeconds(3))) {
				submitNickname(nickname);
				if (roomOpened(Duration.ofSeconds(20))) {
					return this;
				}
			}

			recoverRobotGameFlow();
		}

		wait.until(ExpectedConditions.urlContains("/r/"));
		waitForAnyBodyText("Paper Man", "Your boats", "Attack your opponent!", "Abort game", "Resign");
		return this;
	}

	public boolean hasBodyText(String expectedText) {
		return hasAnyBodyText(expectedText);
	}

	public boolean hasAnyBodyText(String... expectedTexts) {
		try {
			waitForAnyBodyText(expectedTexts);
			return true;
		} catch (TimeoutException ignored) {
			return false;
		}
	}

	public boolean isMainButtonAvailable(String... labels) {
		try {
			WebElement button = wait.until(ExpectedConditions.elementToBeClickable(buttonContaining(labels)));
			return button.isDisplayed() && button.isEnabled();
		} catch (TimeoutException ignored) {
			return false;
		}
	}

	public boolean isButtonVisible(String... labels) {
		try {
			WebElement button = wait.until(ExpectedConditions.visibilityOfElementLocated(buttonContaining(labels)));
			return button.isDisplayed();
		} catch (TimeoutException ignored) {
			return false;
		}
	}

	public boolean isTextVisible(String... texts) {
		try {
			WebElement element = wait.until(ExpectedConditions.visibilityOfElementLocated(elementContaining(texts)));
			return element.isDisplayed();
		} catch (TimeoutException ignored) {
			return false;
		}
	}

	public boolean isNicknameInputEnabled() {
		try {
			WebElement input = wait.until(ExpectedConditions.visibilityOfElementLocated(NICKNAME_INPUT));
			return input.isEnabled();
		} catch (TimeoutException ignored) {
			return false;
		}
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

		WebElement continueButton = wait.until(ExpectedConditions.elementToBeClickable(buttonContaining("Continue", "Tiáº¿p tá»¥c")));
		clickElement(continueButton);
	}

	private boolean roomOpened(Duration timeout) {
		try {
			new WebDriverWait(driver, timeout)
					.until(ExpectedConditions.urlContains("/r/"));
			new WebDriverWait(driver, timeout)
					.until(webDriver -> {
						String bodyText = webDriver.findElement(BODY).getText();
						return bodyText.contains("Paper Man")
								|| bodyText.contains("Your boats")
								|| bodyText.contains("Attack your opponent!")
								|| bodyText.contains("Abort game")
								|| bodyText.contains("Resign");
					});
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
		startRobotGame();
		wait.until(ExpectedConditions.visibilityOfElementLocated(NICKNAME_INPUT));
	}

	private void recoverRobotGameFlow() {
		prepareVisiblePage();
		if (driver.getCurrentUrl().contains("/r/")) {
			return;
		}
		if (nicknamePromptOpened(Duration.ofSeconds(2))) {
			return;
		}
		openLandingPage();
		startRobotGame();
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

	private void prepareVisiblePage() {
		hideExternalOverlaysIfVisible();
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
		wait.until(webDriver -> {
			String bodyText = webDriver.findElement(BODY).getText();
			return Arrays.stream(expectedTexts).anyMatch(bodyText::contains);
		});
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

	private By elementContaining(String... texts) {
		return By.xpath("//*[" + containsAnyNormalizedText(texts) + "]");
	}

	private void rejectConsentDialogIfVisible() {
		List<WebElement> rejectButtons = driver.findElements(buttonContaining("Do not consent", "KhÃ´ng Ä‘á»“ng Ã½"));
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
