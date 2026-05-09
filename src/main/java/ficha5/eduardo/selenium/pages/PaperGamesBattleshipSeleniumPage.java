package ficha5.eduardo.selenium.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.ElementClickInterceptedException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
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
		rejectConsentDialogIfVisible();
		return this;
	}

	public PaperGamesBattleshipSeleniumPage startRobotGame() {
		for (int attempt = 0; attempt < 2; attempt++) {
			clickButton("Play vs robot");
			try {
				wait.until(ExpectedConditions.visibilityOfElementLocated(NICKNAME_INPUT));
				return this;
			} catch (TimeoutException ignored) {
				rejectConsentDialogIfVisible();
			}
		}
		wait.until(ExpectedConditions.visibilityOfElementLocated(NICKNAME_INPUT));
		return this;
	}

	public PaperGamesBattleshipSeleniumPage chooseNickname(String nickname) {
		WebElement input = wait.until(ExpectedConditions.elementToBeClickable(NICKNAME_INPUT));
		input.clear();
		input.sendKeys(nickname);
		clickButton("Continue");
		wait.until(ExpectedConditions.urlContains("/r/"));
		waitForBodyText("Paper Man");
		rejectConsentDialogIfVisible();
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
		WebElement button = wait.until(ExpectedConditions.elementToBeClickable(buttonContaining(label)));
		try {
			button.click();
		} catch (ElementClickInterceptedException intercepted) {
			rejectConsentDialogIfVisible();
			wait.until(ExpectedConditions.elementToBeClickable(buttonContaining(label))).click();
		}
	}

	private void waitForBodyText(String expectedText) {
		wait.until(driver -> driver.findElement(BODY).getText().contains(expectedText));
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
				rejectButton.click();
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
