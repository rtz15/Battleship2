package ficha5.tiago.selenium;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * User story 1: as a player, I want to consult rankings or leaderboards to compare results with other players.
 */
class SeleniumUserStory1Test extends SeleniumAcceptanceTestBase {

	@Test
	@DisplayName("Selenium UserStoryTest1 - landing page exposes rankings and leaderboard sections")
	void landingPageExposesRankingsAndLeaderboardSections() {
		battleshipPage.openLandingPage();

		assertAll("rankings and leaderboard",
				() -> assertTrue(battleshipPage.hasRankingsAndLeaderboardVisible(),
						"Error: expected the landing page to show the leaderboard and ranking areas, but it did not.")
		);
	}
}
