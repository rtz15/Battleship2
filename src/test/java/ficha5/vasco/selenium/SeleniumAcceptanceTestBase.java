package ficha5.vasco.selenium;

import ficha.vasco.selenium.pages.PaperGamesBattleshipSharedLinkSeleniumPage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.PageLoadStrategy;

import java.time.Duration;

/**
 * Browser setup shared by Vasco's Selenium WebDriver acceptance tests.
 */
abstract class SeleniumAcceptanceTestBase {
	protected WebDriver driver;
	protected PaperGamesBattleshipSharedLinkSeleniumPage battleshipPage;

	@BeforeEach
	void setUpBrowser() {
		ChromeOptions options = new ChromeOptions();
		options.setPageLoadStrategy(PageLoadStrategy.EAGER);
		options.addArguments("--headless=new");
		options.addArguments("--window-size=1440,1000");
		options.addArguments("--disable-gpu");
		options.addArguments("--disable-dev-shm-usage");
		options.addArguments("--disable-extensions");
		options.addArguments("--disable-background-networking");
		options.addArguments("--disable-notifications");
		options.addArguments("--no-sandbox");
		options.addArguments("--lang=en-US");
		driver = new ChromeDriver(options);
		driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(60));
		battleshipPage = new PaperGamesBattleshipSharedLinkSeleniumPage(driver);
	}

	@AfterEach
	void tearDownBrowser() {
		if (driver != null) {
			driver.quit();
		}
	}
}
