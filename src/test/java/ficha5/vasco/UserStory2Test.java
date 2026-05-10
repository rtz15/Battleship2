package ficha5.vasco;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * User story 2: as a player, I want to enter a nickname before creating a shared room.
 */
class UserStory2Test extends AcceptanceTestBase {

	@Test
	@DisplayName("UserStoryTest2 - shared link flow asks for a nickname")
	void sharedLinkFlowAsksForNickname() {
		battleshipPage
				.openLandingPage()
				.startSharedLinkGame()
				.assertNicknamePromptIsVisible();
	}
}
