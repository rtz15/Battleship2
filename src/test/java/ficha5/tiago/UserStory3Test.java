package ficha5.tiago;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * User story 3: as a player, I want to see the initial game state after starting a robot match.
 */
class UserStory3Test extends AcceptanceTestBase {

	@Test
	@DisplayName("UserStoryTest3 - robot match shows the initial battle state")
	void robotMatchShowsInitialBattleState() {
		String nickname = "Tiago" + (System.currentTimeMillis() % 100_000);

		battleshipPage
				.openLandingPage()
				.startRobotGame()
				.chooseNickname(nickname)
				.assertInitialBattleState(nickname);
	}
}
