package ficha5.eduardo.selenium;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * User story 2: as a visitor, I want to read the visible game rules before starting a match.
 */
class SeleniumUserStory2Test extends SeleniumAcceptanceTestBase {

	@Test
	@DisplayName("Selenium UserStoryTest2 - landing page documents rules and weapons")
	void landingPageDocumentsRulesAndWeapons() {
		battleshipPage.openLandingPage();

		assertAll("rules and weapons",
				() -> assertTrue(battleshipPage.hasAnyBodyText("Rules of Battleship game online", "Luật chơi"),
						"Error: expected the rules section to be visible, but it was not."),
				() -> assertTrue(battleshipPage.hasAnyBodyText("Each player has a 10x10 grid", "mạng lưới 10 × 10"),
						"Error: expected the grid rule to be visible, but it was not."),
				() -> assertTrue(battleshipPage.hasAnyBodyText("Weapons", "vũ khí"),
						"Error: expected the weapons section to be visible, but it was not."),
				() -> assertTrue(battleshipPage.hasAnyBodyText("Nuclear missile", "bom nguyên tử"),
						"Error: expected the nuclear missile description to be visible, but it was not.")
		);
	}
}
