package battleship;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.InputMismatchException;
import java.util.NoSuchElementException;
import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test class for Tasks.
 * Author: vasco 111331
 * Date: 2026-05-06
 * Cyclomatic Complexity:
 * - menu(): 21
 * - menuHelp(): 1
 * - buildFleet(): 4
 * - readShip(): 1
 * - readPosition(): 1
 * - readClassicPosition(): 6
 */
class TasksTest {

	private static final String DB_FILE = "./battleship_history.mv.db";
	private static final String DB_TRACE_FILE = "./battleship_history.trace.db";

	private final InputStream originalIn = System.in;
	private final PrintStream originalOut = System.out;
	private final PrintStream originalErr = System.err;

	private ByteArrayOutputStream outContent;
	private ByteArrayOutputStream errContent;

	@BeforeEach
	void setUp() {
		Messages.configure(AppLanguage.PORTUGUESE);
		outContent = new ByteArrayOutputStream();
		errContent = new ByteArrayOutputStream();
		System.setOut(new PrintStream(outContent, true, StandardCharsets.UTF_8));
		System.setErr(new PrintStream(errContent, true, StandardCharsets.UTF_8));
		cleanupHistoryDatabase();
	}

	@AfterEach
	void tearDown() {
		System.setIn(originalIn);
		System.setOut(originalOut);
		System.setErr(originalErr);
		Messages.configure(AppLanguage.PORTUGUESE);
		cleanupHistoryDatabase();
	}

	@Test
	@DisplayName("menu prints help and exits immediately when the player gives up")
	void menuExitsImmediatelyWithDesisto() {
		provideInput("desisto%n");

		Tasks.menu();

		String output = stdout();
		assertAll(
				() -> assertTrue(output.contains(Messages.get("menu.help.header")),
						"Error: expected menu() to print the help block before reading commands."),
				() -> assertTrue(output.contains(Messages.get("menu.goodbye")),
						"Error: expected menu() to print the localized goodbye message.")
		);
	}

	@Test
	@DisplayName("menu handles ajuda, unknown command and desisto in order")
	void menuRepeatsHelpAndReportsUnknownCommands() {
		provideInput("ajuda%ncomando_falso%ndesisto%n");

		Tasks.menu();

		String output = stdout();
		assertAll(
				() -> assertTrue(countOccurrences(output, Messages.get("menu.help.header")) >= 2,
						"Error: expected ajuda to print the menu help a second time."),
				() -> assertTrue(output.contains(Messages.get("menu.unknown")),
						"Error: expected an unknown command to print the localized unknown-command message.")
		);
	}

	@Test
	@DisplayName("menu ignores fleet-dependent commands before a fleet exists")
	void menuIgnoresFleetCommandsBeforeFleetExists() {
		provideInput("estado%nmapa%ntiros%nrajada%nsimula%ndesisto%n");

		Tasks.menu();

		String output = stdout();
		assertAll(
				() -> assertTrue(output.contains(Messages.get("menu.goodbye")),
						"Error: expected menu() to keep accepting commands until desisto."),
				() -> assertFalse(output.contains("Estado da Frota:"),
						"Error: expected estado to do nothing before a fleet is created."),
				() -> assertFalse(output.contains(Messages.get("board.legend.title")),
						"Error: expected mapa/tiros to do nothing before a game is created.")
		);
	}

	@Test
	@DisplayName("menu creates a random fleet and then prints status, map and shots")
	void menuHandlesGeneratedFleetCommands() {
		provideInput("gerafrota%nestado%nmapa%ntiros%ndesisto%n");

		Tasks.menu();

		String output = stdout();
		assertAll(
				() -> assertTrue(output.contains("Estado da Frota:"),
						"Error: expected estado to print fleet status after gerafrota."),
				() -> assertTrue(output.contains(Messages.get("board.legend.title")),
						"Error: expected generated fleet board output to include the legend."),
				() -> assertTrue(output.contains(Messages.get("menu.goodbye")),
						"Error: expected menu() to exit after desisto.")
		);
	}

	@Test
	@DisplayName("menu loads a custom fleet through lefrota and processes a rajada")
	void menuHandlesCustomFleetAndRajada() {
		provideInput("lefrota%n" + completeFleetInput() + "rajada A1 C1 E1%ndesisto%n");

		Tasks.menu();

		String output = stdout();
		assertAll(
				() -> assertTrue(output.contains("\"validShots\" : 3"),
						"Error: expected rajada to print the JSON response for three valid shots."),
				() -> assertTrue(output.contains("Estado da Frota:"),
						"Error: expected rajada to print fleet status after firing."),
				() -> assertTrue(output.contains(Messages.get("menu.goodbye")),
						"Error: expected menu() to continue after a non-ending rajada and then exit.")
		);
	}

	@Test
	@DisplayName("menu prints persisted history entries")
	void menuPrintsGameHistory() {
		GameHistory gameHistory = new GameHistory();
		gameHistory.saveGame(java.sql.Timestamp.valueOf("2026-05-06 12:30:00"), 7, 4, 1, 3, "LOSS");
		provideInput("historico%ndesisto%n");

		Tasks.menu();

		String output = stdout();
		assertAll(
				() -> assertTrue(output.contains("HISTORICO DE JOGOS"),
						"Error: expected historico to print the history header."),
				() -> assertTrue(output.contains("Result: LOSS"),
						"Error: expected historico to print persisted result entries.")
		);
	}

	@Test
	@DisplayName("menu throws when no command token exists")
	void menuThrowsWhenInputEndsBeforeFirstCommand() {
		provideInput("");

		assertThrows(NoSuchElementException.class, Tasks::menu,
				"Error: expected menu() to surface Scanner.next() failure when no command is available.");
	}

	@Test
	@DisplayName("menuHelp prints every public menu command")
	void menuHelpPrintsEveryCommand() {
		Tasks.menuHelp();

		String output = stdout();
		assertAll(
				() -> assertTrue(output.contains("- gerafrota:"), "Error: expected help to document gerafrota."),
				() -> assertTrue(output.contains("- lefrota:"), "Error: expected help to document lefrota."),
				() -> assertTrue(output.contains("- estado:"), "Error: expected help to document estado."),
				() -> assertTrue(output.contains("- mapa:"), "Error: expected help to document mapa."),
				() -> assertTrue(output.contains("- rajada:"), "Error: expected help to document rajada."),
				() -> assertTrue(output.contains("- simula:"), "Error: expected help to document simula."),
				() -> assertTrue(output.contains("- tiros:"), "Error: expected help to document tiros."),
				() -> assertTrue(output.contains("- historico:"), "Error: expected help to document historico."),
				() -> assertTrue(output.contains("- desisto:"), "Error: expected help to document desisto.")
		);
	}

	@Test
	@DisplayName("Tasks can be instantiated by legacy callers")
	void constructorCreatesTasksInstance() {
		assertNotNull(new Tasks(), "Error: expected the public default constructor to remain usable.");
	}

	@Test
	@DisplayName("buildFleet skips unknown and rejected ships until eleven ships are accepted")
	void buildFleetSkipsUnknownAndRejectedShips() {
		String input = ""
				+ "canoa 0 0 n%n"
				+ "barca -1 0 n%n"
				+ "barca 0 0 n%n"
				+ "barca 0 0 n%n"
				+ remainingFleetInputAfterA1();

		Fleet fleet = Tasks.buildFleet(new Scanner(input.formatted()));

		assertAll(
				() -> assertEquals(Fleet.FLEET_SIZE, fleet.getShips().size(),
						"Error: expected buildFleet() to stop only after eleven accepted ships."),
				() -> assertNotNull(fleet.shipAt(new Position(0, 0)),
						"Error: expected the first valid barca to be present in the fleet."),
				() -> assertNull(fleet.shipAt(new Position(-1, 0)),
						"Error: expected an outside ship candidate not to be added.")
		);
	}

	@Test
	@DisplayName("buildFleet rejects a null scanner through assertions")
	void buildFleetRejectsNullScanner() {
		assertThrows(AssertionError.class, () -> Tasks.buildFleet(null),
				"Error: expected buildFleet() to assert that the scanner is not null.");
	}

	@Test
	@DisplayName("readShip creates each supported Portuguese ship kind")
	void readShipCreatesSupportedShipKinds() {
		assertAll(
				() -> assertInstanceOf(Barge.class, Tasks.readShip(new Scanner("barca 0 0 n")),
						"Error: expected barca to create a Barge."),
				() -> assertInstanceOf(Caravel.class, Tasks.readShip(new Scanner("caravela 0 0 e")),
						"Error: expected caravela to create a Caravel."),
				() -> assertInstanceOf(Carrack.class, Tasks.readShip(new Scanner("nau 0 0 s")),
						"Error: expected nau to create a Carrack."),
				() -> assertInstanceOf(Frigate.class, Tasks.readShip(new Scanner("fragata 0 0 o")),
						"Error: expected fragata to create a Frigate."),
				() -> assertInstanceOf(Galleon.class, Tasks.readShip(new Scanner("galeao 2 2 n")),
						"Error: expected galeao to create a Galleon.")
		);
	}

	@Test
	@DisplayName("readShip returns null for an unknown ship kind")
	void readShipReturnsNullForUnknownKind() {
		Ship ship = Tasks.readShip(new Scanner("submarino 3 4 n"));

		assertNull(ship, "Error: expected an unsupported ship kind to return null.");
	}

	@Test
	@DisplayName("readShip surfaces invalid bearings for known ship kinds")
	void readShipThrowsForInvalidBearing() {
		assertThrows(AssertionError.class, () -> Tasks.readShip(new Scanner("barca 3 4 x")),
				"Error: expected a known ship with an invalid bearing to fail while assertions are enabled.");
	}

	@Test
	@DisplayName("readShip rejects a null scanner through assertions")
	void readShipRejectsNullScanner() {
		assertThrows(AssertionError.class, () -> Tasks.readShip(null),
				"Error: expected readShip() to assert that the scanner is not null.");
	}

	@Test
	@DisplayName("readPosition parses numeric row and column")
	void readPositionParsesTwoIntegers() {
		Position position = Tasks.readPosition(new Scanner("3 4"));

		assertAll(
				() -> assertEquals(3, position.getRow(), "Error: expected row to be parsed from the first integer."),
				() -> assertEquals(4, position.getColumn(), "Error: expected column to be parsed from the second integer.")
		);
	}

	@Test
	@DisplayName("readPosition returns outside positions without validating board limits")
	void readPositionReturnsOutsideCoordinatesAsEntered() {
		Position position = Tasks.readPosition(new Scanner("-1 10"));

		assertAll(
				() -> assertEquals(-1, position.getRow(), "Error: expected readPosition() to keep the outside row."),
				() -> assertEquals(10, position.getColumn(), "Error: expected readPosition() to keep the outside column."),
				() -> assertFalse(position.isInside(), "Error: expected the parsed outside position to report isInside=false.")
		);
	}

	@Test
	@DisplayName("readPosition throws when tokens are not integers")
	void readPositionThrowsForNonNumericInput() {
		assertThrows(InputMismatchException.class, () -> Tasks.readPosition(new Scanner("A 1")),
				"Error: expected readPosition() to require integer tokens.");
	}

	@Test
	@DisplayName("readPosition throws when the second coordinate is missing")
	void readPositionThrowsWhenCoordinateIsMissing() {
		assertThrows(NoSuchElementException.class, () -> Tasks.readPosition(new Scanner("2")),
				"Error: expected readPosition() to require both row and column.");
	}

	@Test
	@DisplayName("readPosition rejects a null scanner through assertions")
	void readPositionRejectsNullScanner() {
		assertThrows(AssertionError.class, () -> Tasks.readPosition(null),
				"Error: expected readPosition() to assert that the scanner is not null.");
	}

	@Test
	@DisplayName("readClassicPosition accepts compact coordinates")
	void readClassicPositionAcceptsCompactCoordinates() {
		IPosition position = Tasks.readClassicPosition(new Scanner("B2"));

		assertAll(
				() -> assertEquals(1, position.getRow(), "Error: expected B2 to resolve to row index 1."),
				() -> assertEquals(1, position.getColumn(), "Error: expected B2 to resolve to column index 1.")
		);
	}

	@Test
	@DisplayName("readClassicPosition accepts separated coordinates")
	void readClassicPositionAcceptsSeparatedCoordinates() {
		IPosition position = Tasks.readClassicPosition(new Scanner("c 10"));

		assertAll(
				() -> assertEquals(2, position.getRow(), "Error: expected c 10 to resolve to row index 2."),
				() -> assertEquals(9, position.getColumn(), "Error: expected c 10 to resolve to column index 9.")
		);
	}

	@Test
	@DisplayName("readClassicPosition returns outside classic coordinates as parsed")
	void readClassicPositionReturnsOutsideCoordinatesAsParsed() {
		IPosition position = Tasks.readClassicPosition(new Scanner("Z99"));

		assertAll(
				() -> assertEquals(25, position.getRow(), "Error: expected Z99 to preserve the parsed row index."),
				() -> assertEquals(98, position.getColumn(), "Error: expected Z99 to preserve the parsed column index."),
				() -> assertFalse(position.isInside(), "Error: expected Z99 to be outside the board.")
		);
	}

	@Test
	@DisplayName("readClassicPosition rejects empty input")
	void readClassicPositionRejectsEmptyInput() {
		IllegalArgumentException exception = assertThrows(
				IllegalArgumentException.class,
				() -> Tasks.readClassicPosition(new Scanner("")),
				"Error: expected empty input to be rejected."
		);

		assertEquals(Messages.get("position.none"), exception.getMessage(),
				"Error: expected the empty-input error message to be localized.");
	}

	@Test
	@DisplayName("readClassicPosition rejects invalid formats")
	void readClassicPositionRejectsInvalidFormats() {
		assertAll(
				() -> assertThrows(IllegalArgumentException.class, () -> Tasks.readClassicPosition(new Scanner("123")),
						"Error: expected a numeric-only classic coordinate to be rejected."),
				() -> assertThrows(IllegalArgumentException.class, () -> Tasks.readClassicPosition(new Scanner("A B")),
						"Error: expected a letter not followed by an integer to be rejected."),
				() -> assertThrows(IllegalArgumentException.class, () -> Tasks.readClassicPosition(new Scanner("AA1")),
						"Error: expected compact multi-letter classic coordinates to be rejected."),
				() -> assertThrows(IllegalArgumentException.class, () -> Tasks.readClassicPosition(new Scanner("AA 1")),
						"Error: expected separated multi-letter classic coordinates to be rejected."),
				() -> assertThrows(IllegalArgumentException.class, () -> Tasks.readClassicPosition(new Scanner("A +1")),
						"Error: expected signed classic columns to be rejected by the documented format.")
		);
	}

	private void provideInput(String input) {
		System.setIn(new ByteArrayInputStream(input.formatted().getBytes(StandardCharsets.UTF_8)));
	}

	private String stdout() {
		return outContent.toString(StandardCharsets.UTF_8);
	}

	private int countOccurrences(String value, String needle) {
		int count = 0;
		int index = value.indexOf(needle);
		while (index >= 0) {
			count++;
			index = value.indexOf(needle, index + needle.length());
		}
		return count;
	}

	private String completeFleetInput() {
		return ("barca 0 0 n%n" + remainingFleetInputAfterA1()).formatted();
	}

	private String remainingFleetInputAfterA1() {
		return """
				barca 0 2 n
				barca 0 4 n
				barca 0 6 n
				barca 0 8 n
				barca 2 0 n
				barca 2 2 n
				barca 2 4 n
				barca 2 6 n
				barca 2 8 n
				barca 4 0 n
				""";
	}

	private void cleanupHistoryDatabase() {
		try (Connection conn = DriverManager.getConnection("jdbc:h2:./battleship_history");
			 Statement stmt = conn.createStatement()) {
			stmt.executeUpdate("DROP TABLE IF EXISTS game_summary");
		} catch (SQLException ignored) {
			// The tests tolerate an absent or already-closed H2 database.
		}

		new File(DB_FILE).delete();
		new File(DB_TRACE_FILE).delete();
	}
}
