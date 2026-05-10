package ficha5.eduardo.selenium;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * User story 4: as a player, I want to start a robot match after entering my nickname.
 */
class SeleniumUserStory4Test extends SeleniumAcceptanceTestBase {

	@Test
	@DisplayName("Selenium UserStoryTest4 - nickname submission starts a robot match")
	void nicknameSubmissionStartsRobotMatch() {
		String nickname = "Eduardo" + (System.currentTimeMillis() % 100_000);

		battleshipPage
				.openLandingPage()
				.startRobotGame()
				.chooseNickname(nickname);

		assertAll("started robot match",
				() -> assertTrue(battleshipPage.currentUrl().contains("/r/"),
						"Error: expected the browser URL to contain /r/ after starting the match, but got "
								+ battleshipPage.currentUrl()),
				() -> assertTrue(battleshipPage.hasBodyText(nickname),
						"Error: expected the chosen nickname to be visible in the game, but it was not."),
				() -> assertTrue(battleshipPage.hasBodyText("Paper Man"),
						"Error: expected the robot opponent Paper Man to be visible, but it was not.")
		);
	}
}
