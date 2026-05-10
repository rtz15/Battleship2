package ficha5.tiago.selenium;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * User story 4: as a player, I want to abandon a started match when I do not want to continue.
 */
class SeleniumUserStory4Test extends SeleniumAcceptanceTestBase {

	@Test
	@DisplayName("Selenium UserStoryTest4 - started robot match can be aborted")
	void startedRobotMatchCanBeAborted() {
		String nickname = "Tiago" + (System.currentTimeMillis() % 100_000);

		battleshipPage
				.openLandingPage()
				.startRobotGame()
				.chooseNickname(nickname)
				.openAbortConfirmation();

		assertAll("abort confirmation",
				() -> assertTrue(battleshipPage.hasAbortConfirmationVisible(),
						"Error: expected the abort confirmation dialog to be visible, but it was not.")
		);

		battleshipPage.confirmAbortGame();

		assertAll("restored landing page",
				() -> assertTrue(battleshipPage.isLandingRestoredAfterAbort(),
						"Error: expected the landing page to be restored after aborting the match, but it was not.")
		);
	}
}
