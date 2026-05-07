package battleship;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.apache.logging.log4j.core.layout.PatternLayout;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
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
	private static final String LOGGER_NAME = GameHistory.class.getName();

	private TestLogAppender logAppender;

	@BeforeEach
	void setUp() {
		logAppender = attachLogAppender();
		cleanup();
	}

	@AfterEach
	void tearDown() {
		detachLogAppender(logAppender);
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
	@DisplayName("saveGame logs the SQL error when the backing table is missing")
	void saveGameLogsSQLExceptionWhenTableIsMissing() throws Exception {
		GameHistory gameHistory = new GameHistory();
		dropHistoryTable();

		gameHistory.saveGame(Timestamp.valueOf("2026-05-06 15:00:00"), 1, 1, 1, 10, "WIN");

		assertLoggedError("Unable to save game history entry.",
				"Error: expected saveGame() to log the observable SQL failure.");
	}

	@Test
	@DisplayName("getHistory returns an empty list and logs the SQL error when the table is missing")
	void getHistoryReturnsEmptyAndLogsSQLExceptionWhenTableIsMissing() throws Exception {
		GameHistory gameHistory = new GameHistory();
		dropHistoryTable();

		List<String> history = gameHistory.getHistory();

		assertAll(
				() -> assertTrue(history.isEmpty(),
						"Error: expected getHistory() to return an empty list after a SQL failure."),
				() -> assertLoggedError("Unable to read game history.",
						"Error: expected getHistory() to log the observable SQL failure.")
		);
	}

	private void dropHistoryTable() throws SQLException {
		try (Connection conn = DriverManager.getConnection(DB_URL);
			 Statement stmt = conn.createStatement()) {
			stmt.executeUpdate("DROP TABLE game_summary");
		}
	}

	private void assertLoggedError(String expectedMessage, String assertionMessage) {
		boolean found = logAppender.events().stream()
				.anyMatch(event -> event.getLevel().equals(Level.ERROR)
						&& event.getMessage().getFormattedMessage().equals(expectedMessage)
						&& event.getThrown() instanceof SQLException);
		assertTrue(found, assertionMessage);
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

	private TestLogAppender attachLogAppender() {
		LoggerContext context = (LoggerContext) LogManager.getContext(false);
		Configuration configuration = context.getConfiguration();
		TestLogAppender appender = new TestLogAppender("GameHistoryTestAppender");
		appender.start();
		configuration.addAppender(appender);
		LoggerConfig loggerConfig = new LoggerConfig(LOGGER_NAME, Level.ERROR, false);
		loggerConfig.addAppender(appender, Level.ERROR, null);
		configuration.addLogger(LOGGER_NAME, loggerConfig);
		context.updateLoggers();
		return appender;
	}

	private void detachLogAppender(Appender appender) {
		LoggerContext context = (LoggerContext) LogManager.getContext(false);
		Configuration configuration = context.getConfiguration();
		configuration.removeLogger(LOGGER_NAME);
		appender.stop();
		context.updateLoggers();
	}

	private static final class TestLogAppender extends AbstractAppender {
		private final List<LogEvent> events = new ArrayList<>();

		private TestLogAppender(String name) {
			super(name, (Filter) null, PatternLayout.createDefaultLayout(), false, null);
		}

		@Override
		public void append(LogEvent event) {
			events.add(event.toImmutable());
		}

		private List<LogEvent> events() {
			return events;
		}
	}
}
