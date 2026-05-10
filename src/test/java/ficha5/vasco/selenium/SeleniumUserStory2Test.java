package ficha5.vasco.selenium;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * User story 2: as a player, I want to enter a nickname before creating a shared room.
 */
class SeleniumUserStory2Test extends SeleniumAcceptanceTestBase {

	@Test
	@DisplayName("Selenium UserStoryTest2 - shared link flow asks for a nickname")
	void sharedLinkFlowAsksForNickname() {
		battleshipPage
				.openLandingPage()
				.startSharedLinkGame();

		assertAll("nickname prompt",
				() -> assertTrue(battleshipPage.isNicknamePromptVisible(),
						"Error: expected the shared-link flow to ask for a nickname, but it did not.")
		);
	}
}
