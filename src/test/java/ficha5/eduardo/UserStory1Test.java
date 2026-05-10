package ficha5.eduardo;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * User story 1: as a visitor, I want to see the Battleship entry options before choosing a game mode.
 */
class UserStory1Test extends AcceptanceTestBase {

	@Test
	@DisplayName("UserStoryTest1 - landing page exposes the available Battleship entry options")
	void landingPageExposesGameEntryOptions() {
		battleshipPage
				.openLandingPage()
				.assertLandingPageIsLoaded()
				.assertMainEntryOptionsAreAvailable();
	}
}
