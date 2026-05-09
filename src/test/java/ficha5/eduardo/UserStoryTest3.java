package ficha5.eduardo;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * User story 3: as a player, I want to choose a nickname before entering a robot match.
 */
class UserStoryTest3 extends AcceptanceTestBase {

	@Test
	@DisplayName("UserStoryTest3 - robot game asks for a nickname")
	void robotGameAsksForNickname() {
		battleshipPage
				.openLandingPage()
				.startRobotGame()
				.assertNicknamePromptIsVisible();
	}
}
