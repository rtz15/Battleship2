package ficha5.tiago;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * User story 2: as a player, I want to access tournament entries to organize or join competitions.
 */
class UserStory2Test extends AcceptanceTestBase {

	@Test
	@DisplayName("UserStoryTest2 - landing page exposes tournament entry links")
	void landingPageExposesTournamentEntryLinks() {
		battleshipPage
				.openLandingPage()
				.assertTournamentLinksAreAvailable();
	}
}
