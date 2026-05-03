package battleship;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GameTest {

	private static final String DB_FILE = "./battleship_history.mv.db";

	private final ObjectMapper objectMapper = new ObjectMapper();
	private Game game;

	@BeforeEach
	void setUp() {
		Messages.configure(AppLanguage.PORTUGUESE);
		game = new Game(new Fleet());
	}

	@AfterEach
	void tearDown() throws IOException {
		game = null;
		Messages.configure(AppLanguage.PORTUGUESE);
		Files.deleteIfExists(PdfExporter.DEFAULT_OUTPUT_PATH);
		cleanupHistoryDatabase();
	}

	@Test
	@DisplayName("Game constructor initializes counters and move lists")
	void constructorInitializesState() {
		assertAll(
				() -> assertNotNull(game, "Error: expected the game instance to exist after construction."),
				() -> assertNotNull(game.getAlienMoves(), "Error: expected alien moves to be initialized."),
				() -> assertTrue(game.getAlienMoves().isEmpty(), "Error: expected alien moves to start empty."),
				() -> assertEquals(0, game.getInvalidShots(), "Error: expected invalid shots to start at zero."),
				() -> assertEquals(0, game.getRepeatedShots(), "Error: expected repeated shots to start at zero."),
				() -> assertEquals(0, game.getHits(), "Error: expected hits to start at zero."),
				() -> assertEquals(0, game.getSunkShips(), "Error: expected sunk ships to start at zero.")
		);
	}

	@Test
	@DisplayName("fireSingleShot increments invalid shot counter for outside positions")
	void fireSingleShotCountsInvalidShots() {
		Position invalidPosition = new Position(-1, 5);

		game.fireSingleShot(invalidPosition, false);

		assertEquals(1, game.getInvalidShots(), "Error: expected the invalid shot counter to increase for an outside shot.");
	}

	@Test
	@DisplayName("fireSingleShot increments repeated shot counter for repeated attempts")
	void fireSingleShotCountsRepeatedShots() {
		Position position = new Position(2, 3);

		game.fireSingleShot(position, false);
		game.fireSingleShot(position, true);

		assertEquals(1, game.getRepeatedShots(), "Error: expected the repeated shot counter to increase for a repeated shot.");
	}

	@Test
	@DisplayName("repeatedShot returns true once the same position was already fired")
	void repeatedShotReturnsTrueForPreviouslyFiredPosition() {
		List<IPosition> positions = List.of(new Position(2, 3), new Position(2, 4), new Position(2, 5));

		game.fireShots(positions);

		assertTrue(game.repeatedShot(new Position(2, 3)), "Error: expected a previously fired position to be reported as repeated.");
	}

	@Test
	@DisplayName("repeatedShot returns false before any shot was fired at the position")
	void repeatedShotReturnsFalseWhenPositionWasNotFired() {
		assertFalse(game.repeatedShot(new Position(2, 3)), "Error: expected an untouched position not to be reported as repeated.");
	}

	@Test
	@DisplayName("fireShots records the move in alienMoves")
	void fireShotsRecordsMove() {
		List<IPosition> positions = List.of(new Position(2, 3), new Position(2, 4), new Position(2, 5));

		game.fireShots(positions);

		assertEquals(1, game.getAlienMoves().size(), "Error: expected one alien move to be recorded after firing a burst.");
	}

	@Test
	@DisplayName("fireShots prints the aggregated move JSON to the console")
	void fireShotsPrintsJsonResponseToConsole() {
		String consoleOutput = captureStdout(() -> game.fireShots(List.of(
				new Position(2, 3),
				new Position(2, 4),
				new Position(2, 5)
		)));

		assertAll(
				() -> assertTrue(consoleOutput.contains("\"validShots\" : 3"), "Error: expected the console JSON to report three valid shots."),
				() -> assertTrue(consoleOutput.contains("\"missedShots\" : 3"), "Error: expected the console JSON to report three missed shots."),
				() -> assertTrue(consoleOutput.contains("\"outsideShots\" : 0"), "Error: expected the console JSON to report zero outside shots.")
		);
	}

	@Test
	@DisplayName("getRemainingShips reflects floating ships in the player's fleet")
	void getRemainingShipsReflectsCurrentFleetState() {
		IFleet fleet = game.getMyFleet();
		Ship ship1 = new Barge(Compass.NORTH, new Position(1, 1));
		Ship ship2 = new Frigate(Compass.EAST, new Position(5, 5));

		fleet.addShip(ship1);
		fleet.addShip(ship2);
		ship2.sink();

		assertEquals(1, game.getRemainingShips(), "Error: expected exactly one ship to remain floating after sinking one of two ships.");
	}

	@Test
	@DisplayName("jsonShots serializes classic coordinates in input order")
	void jsonShotsSerializesClassicCoordinatesInOrder() throws Exception {
		String json = Game.jsonShots(List.of(
				new Position('C', 10),
				new Position('A', 1),
				new Position('J', 7)
		));

		List<Map<String, Object>> serializedShots = objectMapper.readValue(json, new TypeReference<>() {
		});

		assertAll(
				() -> assertEquals(3, serializedShots.size(), "Error: expected the JSON to contain exactly three serialized shots."),
				() -> assertEquals("C", serializedShots.get(0).get("row"), "Error: expected the first serialized row to keep the classic coordinate."),
				() -> assertEquals(10, serializedShots.get(0).get("column"), "Error: expected the first serialized column to keep the classic coordinate."),
				() -> assertEquals("A", serializedShots.get(1).get("row"), "Error: expected the second serialized row to preserve input order."),
				() -> assertEquals(1, serializedShots.get(1).get("column"), "Error: expected the second serialized column to preserve input order."),
				() -> assertEquals("J", serializedShots.get(2).get("row"), "Error: expected the third serialized row to preserve input order."),
				() -> assertEquals(7, serializedShots.get(2).get("column"), "Error: expected the third serialized column to preserve input order.")
		);
	}

	@Test
	@DisplayName("printBoard renders shots, sunk adjacency and the legend when requested")
	void printBoardRendersShotsAndLegend() {
		Fleet fleet = new Fleet();
		Barge sunkShip = new Barge(Compass.NORTH, new Position(0, 0));
		sunkShip.sink();
		assertTrue(fleet.addShip(sunkShip), "Error: expected the sunk ship fixture to be added to the fleet.");

		Move move = new Move(
				1,
				List.of(new Position(0, 0), new Position(1, 1), new Position(-1, 0)),
				List.of()
		);

		String boardOutput = captureStdout(() -> Game.printBoard(fleet, List.of(move), true, true));

		assertAll(
				() -> assertTrue(boardOutput.contains("*"), "Error: expected the board to mark ship hits with '*'."),
				() -> assertTrue(boardOutput.contains("o"), "Error: expected the board to mark water shots with 'o'."),
				() -> assertTrue(boardOutput.contains("-"), "Error: expected the board to render sunk-ship adjacent markers."),
				() -> assertTrue(boardOutput.contains(Messages.get("board.legend.title")), "Error: expected the legend title to be printed when requested."),
				() -> assertTrue(boardOutput.contains(" A |"), "Error: expected the board to render row labels.")
		);
	}

	@Test
	@DisplayName("printBoard hides the legend when showLegend is false")
	void printBoardHidesLegendWhenNotRequested() {
		Fleet fleet = new Fleet();
		assertTrue(fleet.addShip(new Barge(Compass.NORTH, new Position(0, 0))), "Error: expected the ship fixture to be added to the fleet.");

		String boardOutput = captureStdout(() -> Game.printBoard(fleet, List.of(), false, false));

		assertFalse(boardOutput.contains(Messages.get("board.legend.title")), "Error: expected the legend title not to be printed when showLegend is false.");
	}

	@Test
	@DisplayName("readEnemyFire accepts separated row and column tokens")
	void readEnemyFireAcceptsSeparatedTokens() throws Exception {
		String json = game.readEnemyFire(new Scanner("A 1 B 2 C 3"));

		List<Map<String, Object>> serializedShots = objectMapper.readValue(json, new TypeReference<>() {
		});

		assertAll(
				() -> assertEquals(1, game.getAlienMoves().size(), "Error: expected readEnemyFire to record one move."),
				() -> assertEquals("A", serializedShots.get(0).get("row"), "Error: expected the first separated token pair to resolve to A1."),
				() -> assertEquals(1, serializedShots.get(0).get("column"), "Error: expected the first separated token pair to resolve to A1."),
				() -> assertEquals("B", serializedShots.get(1).get("row"), "Error: expected the second separated token pair to resolve to B2."),
				() -> assertEquals(2, serializedShots.get(1).get("column"), "Error: expected the second separated token pair to resolve to B2."),
				() -> assertEquals("C", serializedShots.get(2).get("row"), "Error: expected the third separated token pair to resolve to C3."),
				() -> assertEquals(3, serializedShots.get(2).get("column"), "Error: expected the third separated token pair to resolve to C3.")
		);
	}

	@Test
	@DisplayName("readEnemyFire accepts compact classic coordinates")
	void readEnemyFireAcceptsCompactClassicCoordinates() throws Exception {
		String json = game.readEnemyFire(new Scanner("A1 B2 C3"));

		List<Map<String, Object>> serializedShots = objectMapper.readValue(json, new TypeReference<>() {
		});

		assertAll(
				() -> assertEquals(1, game.getAlienMoves().size(), "Error: expected readEnemyFire to record one move for compact classic coordinates."),
				() -> assertEquals("A", serializedShots.get(0).get("row"), "Error: expected the first compact token to resolve to A1."),
				() -> assertEquals("B", serializedShots.get(1).get("row"), "Error: expected the second compact token to resolve to B2."),
				() -> assertEquals("C", serializedShots.get(2).get("row"), "Error: expected the third compact token to resolve to C3.")
		);
	}

	@Test
	@DisplayName("readEnemyFire rejects incomplete separated coordinates")
	void readEnemyFireRejectsIncompleteSeparatedCoordinate() {
		IllegalArgumentException exception = assertThrows(
				IllegalArgumentException.class,
				() -> game.readEnemyFire(new Scanner("A B2 C3")),
				"Error: expected an incomplete separated coordinate to be rejected."
		);

		assertTrue(exception.getMessage().contains("Posi"), "Error: expected the error message to explain that the coordinate is incomplete.");
	}

	@Test
	@DisplayName("readEnemyFire rejects bursts with fewer than three positions")
	void readEnemyFireRejectsWrongShotCount() {
		IllegalArgumentException exception = assertThrows(
				IllegalArgumentException.class,
				() -> game.readEnemyFire(new Scanner("A1 B2")),
				"Error: expected readEnemyFire to reject bursts with fewer than three positions."
		);

		assertTrue(exception.getMessage().contains("3"), "Error: expected the error message to mention the required burst size.");
	}

	@Test
	@DisplayName("randomEnemyFire returns three unique shots on a fresh board")
	void randomEnemyFireReturnsThreeUniqueShotsOnFreshBoard() throws Exception {
		String json = game.randomEnemyFire();

		List<Map<String, Object>> serializedShots = objectMapper.readValue(json, new TypeReference<>() {
		});
		long distinctCoordinates = serializedShots.stream()
				.map(shot -> shot.get("row") + ":" + shot.get("column"))
				.distinct()
				.count();

		assertAll(
				() -> assertEquals(1, game.getAlienMoves().size(), "Error: expected a random enemy burst to be recorded as one move."),
				() -> assertEquals(3, serializedShots.size(), "Error: expected randomEnemyFire to return three serialized shots."),
				() -> assertEquals(3, distinctCoordinates, "Error: expected a fresh random burst to contain three unique positions.")
		);
	}

	@Test
	@DisplayName("createSummary aggregates move totals, outcomes and ordered fleet status")
	void createSummaryAggregatesMoveTotalsAndFleetStatus() {
		Fleet fleet = new Fleet();
		assertTrue(fleet.addShip(new Barge(Compass.NORTH, new Position(0, 0))), "Error: expected the barge fixture to be added to the fleet.");
		assertTrue(fleet.addShip(new Frigate(Compass.EAST, new Position(2, 2))), "Error: expected the frigate fixture to be added to the fleet.");

		Game summaryGame = new Game(fleet);
		summaryGame.fireShots(List.of(
				new Position(0, 0),
				new Position(0, 1),
				new Position(0, 2)
		));
		summaryGame.fireShots(List.of(
				new Position(2, 2),
				new Position(0, 0),
				new Position(0, 10)
		));

		GameSummary summary = summaryGame.createSummary();
		MoveSummary firstMove = summary.moveSummaries().get(0);
		MoveSummary secondMove = summary.moveSummaries().get(1);

		assertAll(
				() -> assertEquals("Resumo da simulacao Battleship", summary.title(), "Error: expected the summary title to match the simulator export title."),
				() -> assertEquals("A simulacao terminou com navios ainda a flutuar", summary.finalResult(), "Error: expected the final result to report remaining floating ships."),
				() -> assertEquals(2, summary.totalMoves(), "Error: expected two moves in the generated summary."),
				() -> assertEquals(6, summary.totalShots(), "Error: expected the total shot count to sum all burst positions."),
				() -> assertEquals(2, summary.totalHits(), "Error: expected the total hit count to include the sunk barge hit and the frigate hit."),
				() -> assertEquals(1, summary.totalRepeatedShots(), "Error: expected one repeated shot across the two bursts."),
				() -> assertEquals(1, summary.totalOutsideShots(), "Error: expected one outside shot across the two bursts."),
				() -> assertEquals(2, summary.totalMissedShots(), "Error: expected two missed water shots across the two bursts."),
				() -> assertEquals(1, summary.totalSunkShips(), "Error: expected one sunk ship across the two bursts."),
				() -> assertEquals(1, summary.remainingShips(), "Error: expected one ship to remain floating after the summary scenario."),
				() -> assertEquals(List.of("Barca @ A1 - Afundado", "Fragata @ C3 - A flutuar"), summary.fleetStatus(), "Error: expected fleet status to be sorted by category and origin position."),
				() -> assertEquals(2, summary.moveSummaries().size(), "Error: expected two move summaries to be generated."),
				() -> assertEquals(3, firstMove.validShots(), "Error: expected the first move summary to report all three in-bounds shots as valid."),
				() -> assertEquals(2, firstMove.missedShots(), "Error: expected the first move summary to report two water shots."),
				() -> assertEquals(1, firstMove.sunkShips(), "Error: expected the first move summary to report the sunk barge."),
				() -> assertEquals("Afundou Barca", firstMove.shotSummaries().get(0).outcome(), "Error: expected the first shot summary to describe the sunk barge."),
				() -> assertEquals(1, secondMove.validShots(), "Error: expected the second move summary to report one valid hit."),
				() -> assertEquals(1, secondMove.repeatedShots(), "Error: expected the second move summary to report one repeated shot."),
				() -> assertEquals(1, secondMove.outsideShots(), "Error: expected the second move summary to report one outside shot."),
				() -> assertEquals(1, secondMove.hits(), "Error: expected the second move summary to report one unsunk hit."),
				() -> assertEquals("Acertou Fragata", secondMove.shotSummaries().get(0).outcome(), "Error: expected the second move summary to describe the frigate hit."),
				() -> assertEquals("Repetido", secondMove.shotSummaries().get(1).outcome(), "Error: expected the repeated shot outcome to be preserved in the summary."),
				() -> assertEquals("Exterior", secondMove.shotSummaries().get(2).outcome(), "Error: expected the outside shot outcome to be preserved in the summary.")
		);
	}

	@Test
	@DisplayName("exportSummary writes the simulator summary to the provided output path")
	void exportSummaryWritesToProvidedPath(@TempDir Path tempDir) throws Exception {
		Fleet fleet = new Fleet();
		assertTrue(fleet.addShip(new Barge(Compass.NORTH, new Position(0, 0))), "Error: expected the barge fixture to be added to the fleet.");
		Game exportGame = new Game(fleet);
		exportGame.fireShots(List.of(
				new Position(0, 0),
				new Position(0, 1),
				new Position(0, 2)
		));

		Path outputPath = tempDir.resolve("reports").resolve("tiago").resolve("summary.pdf");
		Path exportedPdf = exportGame.exportSummary(outputPath);

		assertAll(
				() -> assertEquals(outputPath.toAbsolutePath(), exportedPdf, "Error: expected Game.exportSummary(Path) to return the absolute provided path."),
				() -> assertTrue(Files.exists(exportedPdf), "Error: expected Game.exportSummary(Path) to create the requested PDF file.")
		);
	}

	@Test
	@DisplayName("over prints the localized ending, creates the PDF and persists the game result")
	void overPrintsEndingCreatesPdfAndPersistsHistory() {
		Fleet fleet = new Fleet();
		assertTrue(fleet.addShip(new Barge(Compass.NORTH, new Position(0, 0))), "Error: expected the barge fixture to be added to the fleet.");
		Game overGame = new Game(fleet);
		overGame.fireShots(List.of(
				new Position(0, 0),
				new Position(0, 1),
				new Position(0, 2)
		));

		String output = captureStdout(overGame::over);
		List<String> history = new GameHistory().getHistory();

		assertAll(
				() -> assertTrue(output.contains(Messages.get("game.over.message")), "Error: expected over() to print the localized game-over message."),
				() -> assertTrue(output.contains("Resumo PDF gerado em:"), "Error: expected over() to confirm PDF generation."),
				() -> assertTrue(Files.exists(PdfExporter.DEFAULT_OUTPUT_PATH), "Error: expected over() to generate the default summary PDF."),
				() -> assertEquals(1, history.size(), "Error: expected over() to persist exactly one game summary entry."),
				() -> assertTrue(history.get(0).contains("Result: WIN"), "Error: expected the persisted history entry to record the WIN result.")
		);
	}

	private void cleanupHistoryDatabase() {
		try (Connection conn = DriverManager.getConnection("jdbc:h2:./battleship_history");
			 Statement stmt = conn.createStatement()) {
			stmt.executeUpdate("DROP TABLE IF EXISTS game_summary");
		} catch (SQLException ignored) {
			// The tests tolerate a missing or already-closed database file.
		}

		new File(DB_FILE).delete();
	}

	private String captureStdout(Runnable action) {
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
