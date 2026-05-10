package ficha5.eduardo.selenium;

import ficha.eduardo.selenium.pages.PaperGamesBattleshipSeleniumPage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import java.time.Duration;

/**
 * Browser setup shared by Eduardo's Selenium WebDriver acceptance tests.
 */
abstract class SeleniumAcceptanceTestBase {
	protected WebDriver driver;
	protected PaperGamesBattleshipSeleniumPage battleshipPage;

	@BeforeEach
	void setUpBrowser() {
		ChromeOptions options = new ChromeOptions();
		options.addArguments("--headless=new");
		options.addArguments("--window-size=1440,1000");
		options.addArguments("--disable-gpu");
		options.addArguments("--disable-dev-shm-usage");
		options.addArguments("--disable-notifications");
		options.addArguments("--no-sandbox");
		options.addArguments("--lang=en-US");
		driver = new ChromeDriver(options);
		driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(60));
		battleshipPage = new PaperGamesBattleshipSeleniumPage(driver);
	}

	@AfterEach
	void tearDownBrowser() {
		if (driver != null) {
			driver.quit();
		}
	}
}
