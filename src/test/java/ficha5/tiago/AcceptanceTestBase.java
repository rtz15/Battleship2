package ficha5.tiago;

import com.codeborne.selenide.Configuration;
import com.codeborne.selenide.Selenide;
import com.codeborne.selenide.logevents.SelenideLogger;
import ficha5.tiago.pages.PaperGamesBattleshipSessionPage;
import io.qameta.allure.selenide.AllureSelenide;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import static com.codeborne.selenide.Selenide.page;

/**
 * Shared browser configuration for Tiago's Ficha 5 acceptance tests.
 */
abstract class AcceptanceTestBase {
	protected PaperGamesBattleshipSessionPage battleshipPage;

	@BeforeEach
	void setUpBrowser() {
		Configuration.browser = "chrome";
		Configuration.browserSize = "1440x1000";
		Configuration.headless = true;
		Configuration.timeout = 20_000;
		Configuration.pageLoadTimeout = 60_000;
		Configuration.reportsFolder = "target/selenide-reports";
		SelenideLogger.addListener("AllureSelenide", new AllureSelenide()
				.screenshots(true)
				.savePageSource(true));
		battleshipPage = page(PaperGamesBattleshipSessionPage.class);
	}

	@AfterEach
	void tearDownBrowser() {
		SelenideLogger.removeListener("AllureSelenide");
		Selenide.closeWebDriver();
	}
}
