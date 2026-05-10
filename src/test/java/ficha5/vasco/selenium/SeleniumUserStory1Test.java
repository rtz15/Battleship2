package ficha5.vasco.selenium;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * User story 1: as a player, I want to find the option to play with a friend using a shared room.
 */
class SeleniumUserStory1Test extends SeleniumAcceptanceTestBase {

	@Test
	@DisplayName("Selenium UserStoryTest1 - landing page exposes the shared link game option")
	void landingPageExposesSharedLinkGameOption() {
		battleshipPage.openLandingPage();

		assertAll("shared link game option",
				() -> assertTrue(battleshipPage.hasSharedPlayOptionVisible(),
						"Error: expected the landing page to show a play-with-friend option, but it did not.")
		);
	}
}
