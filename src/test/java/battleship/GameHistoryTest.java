package battleship;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test class for GameHistory.
 * Author: vasco 111331
 * Date: 2026-05-06
 * Cyclomatic Complexity:
 * - constructor: 2
 * - saveGame(): 2
 * - getHistory(): 3
 */
class GameHistoryTest {

	private static final String DB_FILE = "./battleship_history.mv.db";
	private static final String DB_TRACE_FILE = "./battleship_history.trace.db";
	private static final String DB_URL = "jdbc:h2:./battleship_history";

	private final PrintStream originalErr = System.err;
	private ByteArrayOutputStream errContent;

	@BeforeEach
	void setUp() {
		errContent = new ByteArrayOutputStream();
		System.setErr(new PrintStream(errContent, true, StandardCharsets.UTF_8));
		cleanup();
	}

	@AfterEach
	void tearDown() {
		System.setErr(originalErr);
		cleanup();
	}

	@Test
	@DisplayName("constructor creates an empty readable history table")
	void constructorCreatesReadableEmptyHistory() {
		GameHistory gameHistory = new GameHistory();

		List<String> history = gameHistory.getHistory();

		assertTrue(history.isEmpty(), "Error: expected a new history database to contain no game entries.");
	}

	@Test
	@DisplayName("saveGame persists every observable field in the formatted history entry")
	void saveAndGetHistoryIncludesAllPersistedFields() {
		GameHistory gameHistory = new GameHistory();
		Timestamp finishedAt = Timestamp.valueOf("2026-05-06 10:15:30");

		gameHistory.saveGame(finishedAt, 10, 5, 2, 3, "WIN");
		List<String> history = gameHistory.getHistory();

		assertAll(
				() -> assertEquals(1, history.size(), "Error: expected exactly one game history entry."),
				() -> assertTrue(history.get(0).contains("2026-05-06 10:15:30"),
						"Error: expected the persisted timestamp to be visible in the history entry."),
				() -> assertTrue(history.get(0).contains("Moves: 10"),
						"Error: expected total moves to be visible in the history entry."),
				() -> assertTrue(history.get(0).contains("Hits: 5"),
						"Error: expected hits to be visible in the history entry."),
				() -> assertTrue(history.get(0).contains("Sunk Ships: 2"),
						"Error: expected sunk ships to be visible in the history entry."),
				() -> assertTrue(history.get(0).contains("Remaining Ships: 3"),
						"Error: expected remaining ships to be visible in the history entry."),
				() -> assertTrue(history.get(0).contains("Result: WIN"),
						"Error: expected result to be visible in the history entry.")
		);
	}

	@Test
	@DisplayName("getHistory returns multiple games ordered by finished_at descending")
	void getHistoryOrdersMultipleGamesByNewestFirst() {
		GameHistory gameHistory = new GameHistory();
		Timestamp oldest = Timestamp.valueOf("2026-05-06 09:00:00");
		Timestamp middle = Timestamp.valueOf("2026-05-06 10:00:00");
		Timestamp newest = Timestamp.valueOf("2026-05-06 11:00:00");

		gameHistory.saveGame(middle, 6, 3, 1, 4, "MIDDLE");
		gameHistory.saveGame(newest, 9, 7, 2, 0, "NEWEST");
		gameHistory.saveGame(oldest, 3, 1, 0, 5, "OLDEST");

		List<String> history = gameHistory.getHistory();

		assertAll(
				() -> assertEquals(3, history.size(), "Error: expected all three saved games to be returned."),
				() -> assertTrue(history.get(0).contains("Result: NEWEST"),
						"Error: expected the newest finished_at row to be returned first."),
				() -> assertTrue(history.get(1).contains("Result: MIDDLE"),
						"Error: expected the middle finished_at row to be returned second."),
				() -> assertTrue(history.get(2).contains("Result: OLDEST"),
						"Error: expected the oldest finished_at row to be returned last.")
		);
	}

	@Test
	@DisplayName("history persists across GameHistory instances")
	void historyPersistsAcrossInstances() {
		GameHistory writer = new GameHistory();
		writer.saveGame(Timestamp.valueOf("2026-05-06 14:00:00"), 4, 2, 1, 6, "LOSS");

		GameHistory reader = new GameHistory();
		List<String> history = reader.getHistory();

		assertAll(
				() -> assertEquals(1, history.size(), "Error: expected a second GameHistory instance to read persisted rows."),
				() -> assertTrue(history.get(0).contains("Result: LOSS"),
						"Error: expected the persisted result to survive across instances.")
		);
	}

	@Test
	@DisplayName("saveGame accepts nullable timestamp and result values because the schema permits them")
	void saveGamePersistsNullableValues() {
		GameHistory gameHistory = new GameHistory();

		assertDoesNotThrow(() -> gameHistory.saveGame(null, 0, 0, 0, 11, null),
				"Error: expected saveGame() to accept null values for nullable database columns.");

		List<String> history = gameHistory.getHistory();

		assertAll(
				() -> assertEquals(1, history.size(), "Error: expected a row with nullable values to be persisted."),
				() -> assertTrue(history.get(0).contains("Game finished at: null"),
						"Error: expected a null timestamp to be rendered in the history entry."),
				() -> assertTrue(history.get(0).contains("Result: null"),
						"Error: expected a null result to be rendered in the history entry.")
		);
	}

	@Test
	@DisplayName("saveGame prints the SQL error when the backing table is missing")
	void saveGamePrintsSQLExceptionWhenTableIsMissing() throws Exception {
		GameHistory gameHistory = new GameHistory();
		dropHistoryTable();

		gameHistory.saveGame(Timestamp.valueOf("2026-05-06 15:00:00"), 1, 1, 1, 10, "WIN");

		assertTrue(stderr().contains("JdbcSQLSyntaxErrorException"),
				"Error: expected saveGame() to print the observable SQL exception to stderr.");
	}

	@Test
	@DisplayName("getHistory returns an empty list and prints the SQL error when the table is missing")
	void getHistoryReturnsEmptyAndPrintsSQLExceptionWhenTableIsMissing() throws Exception {
		GameHistory gameHistory = new GameHistory();
		dropHistoryTable();

		List<String> history = gameHistory.getHistory();

		assertAll(
				() -> assertTrue(history.isEmpty(),
						"Error: expected getHistory() to return an empty list after a SQL failure."),
				() -> assertTrue(stderr().contains("JdbcSQLSyntaxErrorException"),
						"Error: expected getHistory() to print the observable SQL exception to stderr.")
		);
	}

	private void dropHistoryTable() throws SQLException {
		try (Connection conn = DriverManager.getConnection(DB_URL);
			 Statement stmt = conn.createStatement()) {
			stmt.executeUpdate("DROP TABLE game_summary");
		}
	}

	private String stderr() {
		return errContent.toString(StandardCharsets.UTF_8);
	}

	private void cleanup() {
		try (Connection conn = DriverManager.getConnection(DB_URL);
			 Statement stmt = conn.createStatement()) {
			stmt.executeUpdate("DROP TABLE IF EXISTS game_summary");
		} catch (SQLException ignored) {
			// Cleanup must be tolerant because some tests intentionally break the table.
		}

		new File(DB_FILE).delete();
		new File(DB_TRACE_FILE).delete();
	}
}
