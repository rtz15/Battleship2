package battleship;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MainTest {

	@AfterEach
	void resetLanguage() {
		Messages.configure(AppLanguage.PORTUGUESE);
	}

	@Test
	@DisplayName("main supports the inline language flag syntax end-to-end")
	void mainSupportsInlineLanguageFlagSyntax() {
		String output = captureMain(new String[] { "--lang=en" }, "desisto");

		assertAll(
				() -> assertTrue(output.contains("***  Battleship  ***"), "Error: expected main to print the application title before the menu."),
				() -> assertTrue(output.contains("MENU HELP"), "Error: expected the inline English language flag to switch the menu help to English."),
				() -> assertTrue(output.contains("Fair winds!"), "Error: expected the inline English language flag to switch the goodbye message to English.")
		);
	}

	private String captureMain(String[] args, String input) {
		InputStream originalIn = System.in;
		PrintStream originalOut = System.out;
		ByteArrayOutputStream output = new ByteArrayOutputStream();

		try {
			System.setIn(new ByteArrayInputStream((input + System.lineSeparator()).getBytes(StandardCharsets.UTF_8)));
			System.setOut(new PrintStream(output, true, StandardCharsets.UTF_8));
			Main.main(args);
		} finally {
			System.setIn(originalIn);
			System.setOut(originalOut);
		}

		return output.toString(StandardCharsets.UTF_8);
	}
}
