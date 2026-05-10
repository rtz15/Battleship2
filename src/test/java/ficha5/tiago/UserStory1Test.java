package ficha5.tiago;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * User story 1: as a player, I want to consult rankings or leaderboards to compare results with other players.
 */
class UserStory1Test extends AcceptanceTestBase {

	@Test
	@DisplayName("UserStoryTest1 - landing page exposes rankings and leaderboard sections")
	void landingPageExposesRankingsAndLeaderboardSections() {
		battleshipPage
				.openLandingPage()
				.assertRankingsAndLeaderboardAreVisible();
	}
}
