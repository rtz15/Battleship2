package ficha5.vasco.selenium;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * User story 4: as a player, I want the created room to expose a stable invite or waiting state.
 */
class SeleniumUserStory4Test extends SeleniumAcceptanceTestBase {

	@Test
	@DisplayName("Selenium UserStoryTest4 - created room exposes a share or waiting state")
	void createdRoomExposesShareOrWaitingState() {
		String nickname = "Vasco" + (System.currentTimeMillis() % 100_000);

		battleshipPage
				.openLandingPage()
				.startSharedLinkGame()
				.chooseNickname(nickname);

		assertAll("share or waiting state",
				() -> assertTrue(battleshipPage.hasWaitingOrShareStateVisible(),
						"Error: expected the created room to expose invite, share or waiting-state evidence, but it did not.")
		);
	}
}
