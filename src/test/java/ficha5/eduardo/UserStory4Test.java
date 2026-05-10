package ficha5.eduardo;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * User story 4: as a player, I want to start a robot match after entering my nickname.
 */
class UserStory4Test extends AcceptanceTestBase {

	@Test
	@DisplayName("UserStoryTest4 - nickname submission starts a robot match")
	void nicknameSubmissionStartsRobotMatch() {
		String nickname = "Eduardo" + (System.currentTimeMillis() % 100_000);

		battleshipPage
				.openLandingPage()
				.startRobotGame()
				.chooseNickname(nickname)
				.assertRobotGameStarted(nickname);
	}
}
