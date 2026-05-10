package ficha5.eduardo.selenium;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * User story 3: as a player, I want to choose a nickname before entering a robot match.
 */
class SeleniumUserStory3Test extends SeleniumAcceptanceTestBase {

	@Test
	@DisplayName("Selenium UserStoryTest3 - robot game asks for a nickname")
	void robotGameAsksForNickname() {
		battleshipPage
				.openLandingPage()
				.startRobotGame();

		assertAll("nickname prompt",
				() -> assertTrue(battleshipPage.isNicknameInputEnabled(),
						"Error: expected the nickname input to be enabled, but it was not."),
				() -> assertTrue(battleshipPage.isButtonVisible("Continue", "Tiếp tục"),
						"Error: expected the Continue button to be visible, but it was not.")
		);
	}
}
