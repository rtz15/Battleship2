package ficha5.vasco;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * User story 3: as a player, I want the submitted nickname to create a room with a shareable URL.
 */
class UserStory3Test extends AcceptanceTestBase {

	@Test
	@DisplayName("UserStoryTest3 - nickname creates a shared room URL")
	void nicknameCreatesSharedRoomUrl() {
		String nickname = "Vasco" + (System.currentTimeMillis() % 100_000);

		battleshipPage
				.openLandingPage()
				.startSharedLinkGame()
				.chooseNickname(nickname)
				.assertSharedRoomUrlIsCreated();
	}
}
