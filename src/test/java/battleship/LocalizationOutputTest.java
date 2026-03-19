package battleship;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class LocalizationOutputTest {

	@AfterEach
	void resetLanguage() {
		Messages.configure(AppLanguage.PORTUGUESE);
	}

	@Test
	void menuHelpUsesEnglishMessages() {
		Messages.configure(AppLanguage.ENGLISH);

		String output = captureOutput(Tasks::menuHelp);

		assertTrue(output.contains("MENU HELP"));
		assertTrue(output.contains("Type one of the commands below"));
		assertTrue(output.contains("desisto: Exits the game."));
	}

	@Test
	void gameOverUsesEnglishMessage() {
		Messages.configure(AppLanguage.ENGLISH);
		Game game = new Game(new Fleet());

		String output = captureOutput(game::over);

		assertTrue(output.contains("Curse you, Java Sparrow"));
	}

	@Test
	void mainDefaultsToPortugueseAndExitsCleanly() {
		String output = captureMain(new String[0], "desisto");

		assertTrue(output.contains("AJUDA DO MENU"));
		assertTrue(output.contains("Bons ventos!"));
	}

	@Test
	void mainSupportsEnglishViaCliFlag() {
		String output = captureMain(new String[] { "--lang", "en" }, "desisto");

		assertTrue(output.contains("MENU HELP"));
		assertTrue(output.contains("Fair winds!"));
	}

	private String captureMain(String[] args, String input) {
		InputStream originalIn = System.in;
		try {
			System.setIn(new ByteArrayInputStream((input + System.lineSeparator()).getBytes(StandardCharsets.UTF_8)));
			return captureOutput(() -> Main.main(args));
		} finally {
			System.setIn(originalIn);
		}
	}

	private String captureOutput(Runnable action) {
		PrintStream originalOut = System.out;
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		try {
			System.setOut(new PrintStream(output, true, StandardCharsets.UTF_8));
			action.run();
		} finally {
			System.setOut(originalOut);
		}
		return output.toString(StandardCharsets.UTF_8);
	}
}
