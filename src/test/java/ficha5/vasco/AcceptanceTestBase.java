package ficha5.vasco;

import com.codeborne.selenide.Configuration;
import com.codeborne.selenide.Selenide;
import com.codeborne.selenide.logevents.SelenideLogger;
import ficha5.vasco.pages.PaperGamesBattleshipSharedLinkPage;
import io.qameta.allure.selenide.AllureSelenide;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import static com.codeborne.selenide.Selenide.page;

/**
 * Shared browser configuration for Vasco's Ficha 5 acceptance tests.
 */
abstract class AcceptanceTestBase {
	protected PaperGamesBattleshipSharedLinkPage battleshipPage;

	@BeforeEach
	void setUpBrowser() {
		Configuration.browser = "chrome";
		Configuration.browserSize = "1440x1000";
		Configuration.headless = true;
		Configuration.timeout = 20_000;
		Configuration.pageLoadTimeout = 60_000;
		Configuration.pageLoadStrategy = "eager";
		Configuration.reportsFolder = "target/selenide-reports";
		SelenideLogger.addListener("AllureSelenide", new AllureSelenide()
				.screenshots(true)
				.savePageSource(true));
		battleshipPage = page(PaperGamesBattleshipSharedLinkPage.class);
	}

	@AfterEach
	void tearDownBrowser() {
		SelenideLogger.removeListener("AllureSelenide");
		Selenide.closeWebDriver();
	}
}
