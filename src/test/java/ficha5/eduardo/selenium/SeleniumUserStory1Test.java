package ficha5.eduardo.selenium;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * User story 1: as a visitor, I want to see the Battleship entry options before choosing a game mode.
 */
class SeleniumUserStory1Test extends SeleniumAcceptanceTestBase {

	@Test
	@DisplayName("Selenium UserStoryTest1 - landing page exposes the available Battleship entry options")
	void landingPageExposesGameEntryOptions() {
		battleshipPage.openLandingPage();

		assertAll("main entry options",
				() -> assertTrue(battleshipPage.hasAnyBodyText("Battleship Online", "Battleship trực tuyến"),
						"Error: expected the landing page to show the Battleship title, but it did not."),
				() -> assertTrue(battleshipPage.isMainButtonAvailable("Play with a friend", "Chơi với một người bạn"),
						"Error: expected the Play with a friend option to be available, but it was not."),
				() -> assertTrue(battleshipPage.isMainButtonAvailable("Play vs robot", "Chơi với robot"),
						"Error: expected the Play vs robot option to be available, but it was not."),
				() -> assertTrue(battleshipPage.isMainButtonAvailable("Play online", "Chơi trực tuyến"),
						"Error: expected the Play online option to be available, but it was not."),
				() -> assertTrue(battleshipPage.isTextVisible("Create tournament", "Tạo giải đấu"),
						"Error: expected the Create tournament option to be visible, but it was not.")
		);
	}
}
