package ficha5.tiago.selenium;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * User story 3: as a player, I want to see the initial game state after starting a robot match.
 */
class SeleniumUserStory3Test extends SeleniumAcceptanceTestBase {

	@Test
	@DisplayName("Selenium UserStoryTest3 - robot match shows the initial battle state")
	void robotMatchShowsInitialBattleState() {
		String nickname = "Tiago" + (System.currentTimeMillis() % 100_000);

		battleshipPage
				.openLandingPage()
				.startRobotGame()
				.chooseNickname(nickname);

		assertAll("initial battle state",
				() -> assertTrue(battleshipPage.hasInitialBattleStateVisible(nickname),
						"Error: expected the started robot match to show the player, opponent and battle controls, but it did not.")
		);
	}
}
