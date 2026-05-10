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
import java.util.List;

/**
 * Page object for the PaperGames Battleship page using direct Selenium WebDriver calls.
 */
public class PaperGamesBattleshipSeleniumPage {
	private static final String BATTLESHIP_URL = "https://papergames.io/en/battleship";
	private static final By BODY = By.tagName("body");
	private static final By NICKNAME_INPUT = By.cssSelector("input[placeholder='Nickname']");

	private final WebDriver driver;
	private final WebDriverWait wait;

	public PaperGamesBattleshipSeleniumPage(WebDriver driver) {
		this.driver = driver;
		this.wait = new WebDriverWait(driver, Duration.ofSeconds(20));
	}

	public PaperGamesBattleshipSeleniumPage openLandingPage() {
		driver.get(BATTLESHIP_URL);
		waitForBodyText("Battleship Online");
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
			clickButtonAndWaitForNicknamePrompt("Play vs robot");
			if (nicknamePromptOpened(Duration.ofSeconds(12))) {
				return this;
			}
			prepareVisiblePage();
		}
		wait.until(ExpectedConditions.visibilityOfElementLocated(NICKNAME_INPUT));
		return this;
	}

	public PaperGamesBattleshipSeleniumPage chooseNickname(String nickname) {
		for (int attempt = 0; attempt < 3; attempt++) {
			prepareVisiblePage();
			WebElement input = wait.until(ExpectedConditions.visibilityOfElementLocated(NICKNAME_INPUT));
			fillNickname(input, nickname);

			WebElement continueButton = wait.until(ExpectedConditions.elementToBeClickable(buttonContaining("Continue")));
			clickElement(continueButton);

			if (roomOpened()) {
				return this;
			}

			prepareVisiblePage();
			input = wait.until(ExpectedConditions.visibilityOfElementLocated(NICKNAME_INPUT));
			fillNickname(input, nickname);
			continueButton = wait.until(ExpectedConditions.elementToBeClickable(buttonContaining("Continue")));
			clickElement(continueButton);
			if (roomOpened()) {
				return this;
			}

			startRobotGame();
		}

		wait.until(ExpectedConditions.urlContains("/r/"));
		waitForBodyText("Paper Man");
		return this;
	}

	public boolean hasBodyText(String expectedText) {
		try {
			waitForBodyText(expectedText);
			return true;
		} catch (TimeoutException ignored) {
			return false;
		}
	}

	public boolean isMainButtonAvailable(String label) {
		try {
			WebElement button = wait.until(ExpectedConditions.elementToBeClickable(buttonContaining(label)));
			return button.isDisplayed() && button.isEnabled();
		} catch (TimeoutException ignored) {
			return false;
		}
	}

	public boolean isButtonVisible(String label) {
		try {
			WebElement button = wait.until(ExpectedConditions.visibilityOfElementLocated(buttonContaining(label)));
			return button.isDisplayed();
		} catch (TimeoutException ignored) {
			return false;
		}
	}

	public boolean isTextVisible(String text) {
		try {
			WebElement element = wait.until(ExpectedConditions.visibilityOfElementLocated(elementContaining(text)));
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

	private void clickButton(String label) {
		WebElement button = wait.until(ExpectedConditions.visibilityOfElementLocated(buttonContaining(label)));
		clickElement(button);
	}

	private void clickButtonAndWaitForNicknamePrompt(String label) {
		WebElement button = wait.until(ExpectedConditions.visibilityOfElementLocated(buttonContaining(label)));
		clickElement(button);
		if (nicknamePromptOpened(Duration.ofSeconds(3))) {
			return;
		}

		button = wait.until(ExpectedConditions.visibilityOfElementLocated(buttonContaining(label)));
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

	private boolean roomOpened() {
		try {
			new WebDriverWait(driver, Duration.ofSeconds(8))
					.until(ExpectedConditions.urlContains("/r/"));
			waitForBodyText("Paper Man");
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
		rejectConsentDialogIfVisible();
		hideExternalOverlaysIfVisible();
	}

	private void hideExternalOverlaysIfVisible() {
		((JavascriptExecutor) driver).executeScript(
				"document.querySelectorAll(\".fc-ab-root, .fc-dialog, .fc-dialog-overlay, .fc-consent-root, "
						+ ".fc-whitelist-root, button.fc-faq-header, .fc-faq-header, "
						+ ".fc-dialog-restricted-content, [aria-label='Learn more']\")"
						+ ".forEach(function(element) { element.style.display='none'; element.style.pointerEvents='none'; });");
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

	private void waitForBodyText(String expectedText) {
		wait.until(webDriver -> webDriver.findElement(BODY).getText().contains(expectedText));
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

	private By buttonContaining(String label) {
		return By.xpath("//button[contains(normalize-space(.), " + xpathLiteral(label) + ")]");
	}

	private By elementContaining(String text) {
		return By.xpath("//*[contains(normalize-space(.), " + xpathLiteral(text) + ")]");
	}

	private void rejectConsentDialogIfVisible() {
		List<WebElement> rejectButtons = driver.findElements(buttonContaining("Do not consent"));
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
