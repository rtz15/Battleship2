package ficha5.vasco.selenium;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * User story 3: as a player, I want the submitted nickname to create a room with a shareable URL.
 */
class SeleniumUserStory3Test extends SeleniumAcceptanceTestBase {

	@Test
	@DisplayName("Selenium UserStoryTest3 - nickname creates a shared room URL")
	void nicknameCreatesSharedRoomUrl() {
		String nickname = "Vasco" + (System.currentTimeMillis() % 100_000);

		battleshipPage
				.openLandingPage()
				.startSharedLinkGame()
				.chooseNickname(nickname);

		assertAll("shared room url",
				() -> assertTrue(battleshipPage.hasRoomUrl(),
						"Error: expected the shared game flow to create a /r/ room URL, but it did not.")
		);
	}
}
