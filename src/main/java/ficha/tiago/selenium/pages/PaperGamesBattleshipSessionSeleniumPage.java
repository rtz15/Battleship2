package ficha.tiago.selenium.pages;

import ficha.eduardo.selenium.pages.PaperGamesBattleshipSeleniumPage;
import org.openqa.selenium.By;
import org.openqa.selenium.ElementClickInterceptedException;
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
import java.util.stream.Collectors;

/**
 * Page object for Tiago's acceptance suite covering rankings, tournaments and in-game session controls.
 */
public class PaperGamesBattleshipSessionSeleniumPage {
	private static final By BODY = By.tagName("body");

	private final WebDriver driver;
	private final WebDriverWait wait;
	private final PaperGamesBattleshipSeleniumPage onboardingPage;

	public PaperGamesBattleshipSessionSeleniumPage(WebDriver driver) {
		this.driver = driver;
		this.wait = new WebDriverWait(driver, Duration.ofSeconds(20));
		this.onboardingPage = new PaperGamesBattleshipSeleniumPage(driver);
	}

	public PaperGamesBattleshipSessionSeleniumPage openLandingPage() {
		onboardingPage.openLandingPage();
		return this;
	}

	public PaperGamesBattleshipSessionSeleniumPage startRobotGame() {
		onboardingPage.startRobotGame();
		return this;
	}

	public PaperGamesBattleshipSessionSeleniumPage chooseNickname(String nickname) {
		onboardingPage.chooseNickname(nickname);
		return this;
	}

	public PaperGamesBattleshipSessionSeleniumPage openAbortConfirmation() {
		clickButton(firstButtonContaining("Abort game"));
		wait.until(ExpectedConditions.visibilityOfElementLocated(elementContaining("Are you sure you want to continue?")));
		return this;
	}

	public PaperGamesBattleshipSessionSeleniumPage confirmAbortGame() {
		clickButton(lastButtonContaining("Abort game"));
		wait.until(ExpectedConditions.urlContains("/en/battleship"));
		waitForAnyBodyText("Battleship Online", "Daily leaderboard");
		return this;
	}

	public boolean hasRankingsAndLeaderboardVisible() {
		return hasAnyBodyText("Daily leaderboard")
				&& isTextVisible("RANKA")
				&& hasAnyBodyText("General ranking since 30 days.");
	}

	public boolean hasTournamentLinksVisible() {
		return isLinkVisible("Create tournament", "/en/t/create-tournament")
				&& isLinkVisible("My tournaments", "/en/t/my-tournaments");
	}

	public boolean hasInitialBattleStateVisible(String nickname) {
		return currentUrl().contains("/r/")
				&& hasAnyBodyText(nickname)
				&& hasAnyBodyText("Paper Man")
				&& isTextVisible("Your boats")
				&& isTextVisible("Your opponent's boats")
				&& isTextVisible("Attack your opponent!");
	}

	public boolean hasAbortConfirmationVisible() {
		return hasAnyBodyText("Are you sure you want to continue?")
				&& isButtonVisible("Cancel")
				&& isButtonVisible(lastButtonContaining("Abort game"));
	}

	public boolean isLandingRestoredAfterAbort() {
		return currentUrl().contains("/en/battleship")
				&& hasAnyBodyText("Battleship Online")
				&& hasRankingsAndLeaderboardVisible();
	}

	public String currentUrl() {
		return driver.getCurrentUrl();
	}

	private boolean hasAnyBodyText(String... expectedTexts) {
		try {
			waitForAnyBodyText(expectedTexts);
			return true;
		} catch (TimeoutException ignored) {
			return false;
		}
	}

	private boolean isTextVisible(String... texts) {
		try {
			WebElement element = wait.until(ExpectedConditions.visibilityOfElementLocated(elementContaining(texts)));
			return element.isDisplayed();
		} catch (TimeoutException ignored) {
			return false;
		}
	}

	private boolean isLinkVisible(String label, String hrefFragment) {
		try {
			WebElement link = wait.until(ExpectedConditions.visibilityOfElementLocated(linkContaining(label, hrefFragment)));
			return link.isDisplayed();
		} catch (TimeoutException ignored) {
			return false;
		}
	}

	private boolean isButtonVisible(String... labels) {
		return isButtonVisible(firstButtonContaining(labels));
	}

	private boolean isButtonVisible(By locator) {
		try {
			WebElement button = wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
			return button.isDisplayed();
		} catch (TimeoutException ignored) {
			return false;
		}
	}

	private void clickButton(By locator) {
		WebElement button = wait.until(ExpectedConditions.elementToBeClickable(locator));
		scrollIntoView(button);
		try {
			new Actions(driver)
					.moveToElement(button)
					.pause(Duration.ofMillis(150))
					.click()
					.perform();
		} catch (ElementClickInterceptedException | StaleElementReferenceException intercepted) {
			((JavascriptExecutor) driver).executeScript("arguments[0].click();", button);
		}
	}

	private void scrollIntoView(WebElement element) {
		((JavascriptExecutor) driver).executeScript(
				"arguments[0].scrollIntoView({block: 'center', inline: 'center'});", element);
	}

	private void waitForAnyBodyText(String... expectedTexts) {
		wait.until(webDriver -> {
			String bodyText = webDriver.findElement(BODY).getText();
			return Arrays.stream(expectedTexts).anyMatch(bodyText::contains);
		});
	}

	private By elementContaining(String... texts) {
		return By.xpath("//*[" + containsAnyNormalizedText(texts) + "]");
	}

	private By firstButtonContaining(String... labels) {
		return By.xpath("//button[" + containsAnyNormalizedText(labels) + "]");
	}

	private By lastButtonContaining(String... labels) {
		return By.xpath("(//button[" + containsAnyNormalizedText(labels) + "])[last()]");
	}

	private By linkContaining(String label, String hrefFragment) {
		return By.xpath("//a[contains(normalize-space(.), " + xpathLiteral(label) + ") and contains(@href, "
				+ xpathLiteral(hrefFragment) + ")]");
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
