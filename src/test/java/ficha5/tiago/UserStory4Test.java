package ficha5.tiago;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * User story 4: as a player, I want to abandon a started match when I do not want to continue.
 */
class UserStory4Test extends AcceptanceTestBase {

	@Test
	@DisplayName("UserStoryTest4 - started robot match can be aborted")
	void startedRobotMatchCanBeAborted() {
		String nickname = "Tiago" + (System.currentTimeMillis() % 100_000);

		battleshipPage
				.openLandingPage()
				.startRobotGame()
				.chooseNickname(nickname)
				.openAbortConfirmation()
				.confirmAbortGame()
				.assertLandingPageRestoredAfterAbort();
	}
}
