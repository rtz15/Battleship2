package ficha5.tiago.selenium;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * User story 2: as a player, I want to access tournament entries to organize or join competitions.
 */
class SeleniumUserStory2Test extends SeleniumAcceptanceTestBase {

	@Test
	@DisplayName("Selenium UserStoryTest2 - landing page exposes tournament entry links")
	void landingPageExposesTournamentEntryLinks() {
		battleshipPage.openLandingPage();

		assertAll("tournament links",
				() -> assertTrue(battleshipPage.hasTournamentLinksVisible(),
						"Error: expected the landing page to expose Create tournament and My tournaments links, but it did not.")
		);
	}
}
