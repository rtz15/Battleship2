package ficha5.vasco;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * User story 4: as a player, I want the created room to expose a stable invite or waiting state.
 */
class UserStory4Test extends AcceptanceTestBase {

	@Test
	@DisplayName("UserStoryTest4 - created room exposes a share or waiting state")
	void createdRoomExposesShareOrWaitingState() {
		String nickname = "Vasco" + (System.currentTimeMillis() % 100_000);

		battleshipPage
				.openLandingPage()
				.startSharedLinkGame()
				.chooseNickname(nickname)
				.assertShareOrWaitingStateIsVisible();
	}
}
