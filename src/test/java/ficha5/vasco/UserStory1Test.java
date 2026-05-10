package ficha5.vasco;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * User story 1: as a player, I want to find the option to play with a friend using a shared room.
 */
class UserStory1Test extends AcceptanceTestBase {

	@Test
	@DisplayName("UserStoryTest1 - landing page exposes the shared link game option")
	void landingPageExposesSharedLinkGameOption() {
		battleshipPage
				.openLandingPage()
				.assertSharedLinkOptionIsAvailable();
	}
}
