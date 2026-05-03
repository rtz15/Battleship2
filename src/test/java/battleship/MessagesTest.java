package battleship;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MessagesTest {

	@AfterEach
	void resetLanguage() {
		Messages.configure(AppLanguage.PORTUGUESE);
	}

	@Test
	@DisplayName("configure(null) resets the bundle to Portuguese")
	void configureNullResetsTheBundleToPortuguese() {
		Messages.configure(AppLanguage.ENGLISH);
		Messages.configure(null);

		assertEquals("Bons ventos!", Messages.get("menu.goodbye"), "Error: expected configure(null) to restore the Portuguese bundle.");
	}

	@Test
	@DisplayName("configure switches bundle lookup to English")
	void configureSwitchesBundleLookupToEnglish() {
		Messages.configure(AppLanguage.ENGLISH);

		assertEquals("Fair winds!", Messages.get("menu.goodbye"), "Error: expected English bundle lookup after configure(ENGLISH).");
	}

	@Test
	@DisplayName("format interpolates board legend placeholders in Portuguese")
	void formatInterpolatesLegendPlaceholdersInPortuguese() {
		Messages.configure(AppLanguage.PORTUGUESE);

		String formatted = Messages.format("board.legend.line1", '#', '-', '.');

		assertAll(
				() -> assertTrue(formatted.contains("#->navio"), "Error: expected the Portuguese legend to contain the ship marker replacement."),
				() -> assertTrue(formatted.contains("-->adjacente a navio"), "Error: expected the Portuguese legend to contain the adjacent marker replacement."),
				() -> assertTrue(formatted.contains(".->agua"), "Error: expected the Portuguese legend to contain the water marker replacement.")
		);
	}

	@Test
	@DisplayName("format interpolates board legend placeholders in English")
	void formatInterpolatesLegendPlaceholdersInEnglish() {
		Messages.configure(AppLanguage.ENGLISH);

		String formatted = Messages.format("board.legend.line2", '*', 'o');

		assertAll(
				() -> assertTrue(formatted.contains("*->direct hit"), "Error: expected the English legend to contain the direct-hit replacement."),
				() -> assertTrue(formatted.contains("o->shot in water"), "Error: expected the English legend to contain the water-shot replacement.")
		);
	}
}
