package ficha5.eduardo.pages;

import com.codeborne.selenide.SelenideElement;

import java.time.Duration;

import static com.codeborne.selenide.Condition.disappear;
import static com.codeborne.selenide.Condition.enabled;
import static com.codeborne.selenide.Condition.text;
import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selectors.byText;
import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.$$;
import static com.codeborne.selenide.Selenide.open;
import static com.codeborne.selenide.WebDriverConditions.urlContaining;
import static com.codeborne.selenide.WebDriverRunner.url;
import static com.codeborne.selenide.Selenide.webdriver;

/**
 * Page object for the PaperGames Battleship landing page and robot game onboarding flow.
 */
public class PaperGamesBattleshipPage {
	private static final String BATTLESHIP_URL = "https://papergames.io/en/battleship";

	public PaperGamesBattleshipPage openLandingPage() {
		open(BATTLESHIP_URL);
		rejectConsentDialogIfVisible();
		return this;
	}

	public PaperGamesBattleshipPage assertLandingPageIsLoaded() {
		$("body").shouldHave(
				text("Battleship Online"),
				text("First to sink all opponent ships wins")
		);
		return this;
	}

	public PaperGamesBattleshipPage assertMainEntryOptionsAreAvailable() {
		buttonContaining("Play with a friend").shouldBe(visible, enabled);
		buttonContaining("Play vs robot").shouldBe(visible, enabled);
		buttonContaining("Play online").shouldBe(visible, enabled);
		$(byText("Create tournament")).shouldBe(visible);
		return this;
	}

	public PaperGamesBattleshipPage assertRulesAndWeaponsAreDocumented() {
		$("body").shouldHave(
				text("Rules of Battleship game online"),
				text("Each player has a 10x10 grid"),
				text("Weapons"),
				text("Nuclear missile")
		);
		return this;
	}

	public PaperGamesBattleshipPage startRobotGame() {
		rejectConsentDialogIfVisible();
		for (int attempt = 0; attempt < 2; attempt++) {
			buttonContaining("Play vs robot").shouldBe(visible, enabled).click();
			try {
				nicknameInput().shouldBe(visible, Duration.ofSeconds(8));
				return this;
			} catch (AssertionError ignored) {
				rejectConsentDialogIfVisible();
			}
		}
		nicknameInput().shouldBe(visible);
		return this;
	}

	public PaperGamesBattleshipPage assertNicknamePromptIsVisible() {
		$("body").shouldHave(
				text("Who are you?"),
				text("Please choose a respectful username")
		);
		nicknameInput().shouldBe(visible, enabled);
		buttonContaining("Continue").shouldBe(visible);
		return this;
	}

	public PaperGamesBattleshipPage chooseNickname(String nickname) {
		nicknameInput().setValue(nickname);
		rejectConsentDialogIfVisible();
		buttonContaining("Continue").click();
		rejectConsentDialogIfVisible();
		return this;
	}

	public PaperGamesBattleshipPage assertRobotGameStarted(String nickname) {
		webdriver().shouldHave(urlContaining("/r/"));
		$("body").shouldHave(
				text(nickname),
				text("Paper Man"),
				text("Your boats"),
				text("Attack your opponent!"),
				text("Abort game")
		);
		return this;
	}

	public String currentUrl() {
		return url();
	}

	private SelenideElement nicknameInput() {
		return $("input[placeholder='Nickname']");
	}

	private SelenideElement buttonContaining(String label) {
		return $$("button").findBy(text(label));
	}

	private void rejectConsentDialogIfVisible() {
		SelenideElement rejectButton = $$("button").findBy(text("Do not consent"));
		try {
			rejectButton.shouldBe(visible, Duration.ofSeconds(5)).click();
			rejectButton.should(disappear, Duration.ofSeconds(5));
		} catch (AssertionError ignored) {
			// The consent dialog is asynchronous and is not always shown.
		}
	}
}
