package ficha5.eduardo;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * User story 2: as a visitor, I want to read the visible game rules before starting a match.
 */
class UserStoryTest2 extends AcceptanceTestBase {

	@Test
	@DisplayName("UserStoryTest2 - landing page documents rules and weapons")
	void landingPageDocumentsRulesAndWeapons() {
		battleshipPage
				.openLandingPage()
				.assertLandingPageIsLoaded()
				.assertRulesAndWeaponsAreDocumented();
	}
}
