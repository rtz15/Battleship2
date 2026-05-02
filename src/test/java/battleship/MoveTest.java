package battleship;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test class for class Move.
 * Author: Eduardo Sousa
 * Date: 2026-04-26 17:24
 * Cyclomatic Complexity:
 * - constructor: 1
 * - toString(): 1
 * - getNumber(): 1
 * - getShots(): 1
 * - getShotResults(): 1
 * - processEnemyFire(): 37
 */
class MoveTest {

	private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

	private Move move;

	@BeforeEach
	void setUp() {
		move = new Move(
				1,
				defaultShots(),
				List.of(
						shotResult(true, false, null, false),
						shotResult(true, false, null, false),
						shotResult(true, false, null, false)
				)
		);
	}

	@AfterEach
	void tearDown() {
		move = null;
	}

	@Test
	@DisplayName("Move constructor and accessors preserve assigned state")
	void constructorAndAccessorsPreserveAssignedState() {
		assertAll(
				() -> assertEquals(1, move.getNumber(), "Error: expected move number 1 but got a different value."),
				() -> assertEquals(3, move.getShots().size(), "Error: expected 3 shots but got a different amount."),
				() -> assertEquals(3, move.getShotResults().size(), "Error: expected 3 shot results but got a different amount."),
				() -> assertEquals("Move{number=1, shots=3, results=3}", move.toString(), "Error: expected Move toString to summarize number, shots and results."),
				() -> assertEquals(new Position(0, 0), move.getShots().get(0), "Error: expected the first shot to remain A1."),
				() -> assertFalse(move.getShotResults().get(0).repeated(), "Error: expected the first baseline shot result to be non-repeated.")
		);
	}

	@Test
	@DisplayName("processEnemyFire summarizes valid missed shots without verbose output")
	void processEnemyFireSummarizesOnlyMissedValidShots() throws Exception {
		String json = move.processEnemyFire(false);
		JsonNode tree = OBJECT_MAPPER.readTree(json);

		assertAll(
				() -> assertEquals(3, tree.get("validShots").asInt(), "Error: expected 3 valid shots but got a different count."),
				() -> assertEquals(0, tree.get("repeatedShots").asInt(), "Error: expected 0 repeated shots but got a different count."),
				() -> assertEquals(0, tree.get("outsideShots").asInt(), "Error: expected 0 outside shots but got a different count."),
				() -> assertEquals(3, tree.get("missedShots").asInt(), "Error: expected 3 missed shots but got a different count."),
				() -> assertEquals(0, tree.get("sunkBoats").size(), "Error: expected no sunk boats but got unexpected entries."),
				() -> assertEquals(0, tree.get("hitsOnBoats").size(), "Error: expected no hits on boats but got unexpected entries.")
		);
	}

	@Test
	@DisplayName("processEnemyFire reports repeated and outside shots when no valid shot lands")
	void processEnemyFireReportsRepeatedAndOutsideShotsWhenNoValidShotLands() throws Exception {
		move = new Move(
				2,
				defaultShots(),
				List.of(
						shotResult(true, true, null, false),
						shotResult(false, false, null, false),
						shotResult(false, false, null, false)
				)
		);

		ProcessedMove processed = processWithCapturedOutput(move, true);
		JsonNode tree = OBJECT_MAPPER.readTree(processed.json());

		assertAll(
				() -> assertTrue(processed.consoleOutput().contains("Jogada nº2 -> 1 tiro repetido, 2 tiros exteriores"), "Error: expected repeated and outside shots to be described in verbose output."),
				() -> assertEquals(0, tree.get("validShots").asInt(), "Error: expected 0 valid shots but got a different count."),
				() -> assertEquals(1, tree.get("repeatedShots").asInt(), "Error: expected 1 repeated shot but got a different count."),
				() -> assertEquals(2, tree.get("outsideShots").asInt(), "Error: expected 2 outside shots but got a different count."),
				() -> assertEquals(0, tree.get("missedShots").asInt(), "Error: expected 0 missed shots but got a different count.")
		);
	}

	@Test
	@DisplayName("processEnemyFire reports sunk boats, partial hits and repeated shots without stray separators")
	void processEnemyFireReportsSunkBoatsPartialHitsAndRepeatedShots() throws Exception {
		IShip barge = new Barge(Compass.NORTH, new Position(0, 0));
		IShip frigate = new Frigate(Compass.EAST, new Position(3, 3));
		move = new Move(
				3,
				defaultShots(),
				List.of(
						shotResult(true, false, barge, true),
						shotResult(true, false, frigate, false),
						shotResult(true, true, null, false)
				)
		);

		ProcessedMove processed = processWithCapturedOutput(move, true);
		JsonNode tree = OBJECT_MAPPER.readTree(processed.json());

		assertAll(
				() -> assertTrue(processed.consoleOutput().contains("Jogada nº3 -> 2 tiros válidos:"), "Error: expected verbose output to mention the valid shot count for move 3."),
				() -> assertTrue(processed.consoleOutput().contains("Barca ao fundo"), "Error: expected verbose output to mention the sunk Barca."),
				() -> assertTrue(processed.consoleOutput().contains("Fragata"), "Error: expected verbose output to mention the partial hit on Fragata."),
				() -> assertTrue(processed.consoleOutput().contains("1 tiro repetido"), "Error: expected verbose output to mention the repeated shot."),
				() -> assertEquals(2, tree.get("validShots").asInt(), "Error: expected 2 valid shots but got a different count."),
				() -> assertEquals(1, tree.get("repeatedShots").asInt(), "Error: expected 1 repeated shot but got a different count."),
				() -> assertEquals(0, tree.get("outsideShots").asInt(), "Error: expected 0 outside shots but got a different count."),
				() -> assertEquals(0, tree.get("missedShots").asInt(), "Error: expected 0 missed shots but got a different count."),
				() -> assertEquals(1, tree.get("sunkBoats").size(), "Error: expected one sunk boat entry but got a different amount."),
				() -> assertEquals("Barca", tree.get("sunkBoats").get(0).get("type").asText(), "Error: expected the sunk boat type to be Barca."),
				() -> assertEquals(1, tree.get("sunkBoats").get(0).get("count").asInt(), "Error: expected one sunk Barca but got a different count."),
				() -> assertEquals(1, tree.get("hitsOnBoats").size(), "Error: expected one partial-hit boat entry but got a different amount."),
				() -> assertEquals("Fragata", tree.get("hitsOnBoats").get(0).get("type").asText(), "Error: expected the partial-hit boat type to be Fragata."),
				() -> assertEquals(1, tree.get("hitsOnBoats").get(0).get("hits").asInt(), "Error: expected one hit on Fragata but got a different count.")
		);
	}

	@Test
	@DisplayName("processEnemyFire groups multiple sunk ships of the same type")
	void processEnemyFireGroupsMultipleSunkShipsOfTheSameType() throws Exception {
		move = new Move(
				4,
				defaultShots(),
				List.of(
						shotResult(true, false, new Barge(Compass.NORTH, new Position(0, 0)), true),
						shotResult(true, false, new Barge(Compass.NORTH, new Position(1, 0)), true),
						shotResult(true, false, null, false)
				)
		);

		ProcessedMove processed = processWithCapturedOutput(move, true);
		JsonNode tree = OBJECT_MAPPER.readTree(processed.json());

		assertAll(
				() -> assertTrue(processed.consoleOutput().contains("Jogada nº4 -> 3 tiros válidos: 2 Barcas ao fundo + 1 tiro na água"), "Error: expected verbose output to aggregate two sunk Barcas and one missed shot."),
				() -> assertEquals(3, tree.get("validShots").asInt(), "Error: expected 3 valid shots but got a different count."),
				() -> assertEquals(1, tree.get("missedShots").asInt(), "Error: expected 1 missed shot but got a different count."),
				() -> assertEquals(1, tree.get("sunkBoats").size(), "Error: expected a single aggregated sunk boat entry but got a different amount."),
				() -> assertEquals("Barca", tree.get("sunkBoats").get(0).get("type").asText(), "Error: expected the grouped sunk boat type to be Barca."),
				() -> assertEquals(2, tree.get("sunkBoats").get(0).get("count").asInt(), "Error: expected two sunk Barcas but got a different count."),
				() -> assertEquals(0, tree.get("hitsOnBoats").size(), "Error: expected no partial-hit boats because all hits sank their target.")
		);
	}

	@Test
	@DisplayName("processEnemyFire reports only outside shots when every shot is invalid")
	void processEnemyFireReportsOnlyOutsideShotsWhenEveryShotIsInvalid() throws Exception {
		move = new Move(
				5,
				defaultShots(),
				List.of(
						shotResult(false, false, null, false),
						shotResult(false, false, null, false),
						shotResult(false, false, null, false)
				)
		);

		ProcessedMove processed = processWithCapturedOutput(move, true);
		JsonNode tree = OBJECT_MAPPER.readTree(processed.json());

		assertAll(
				() -> assertTrue(processed.consoleOutput().contains("Jogada nº5 -> 3 tiros exteriores"), "Error: expected verbose output to mention only outside shots."),
				() -> assertEquals(0, tree.get("validShots").asInt(), "Error: expected 0 valid shots but got a different count."),
				() -> assertEquals(0, tree.get("repeatedShots").asInt(), "Error: expected 0 repeated shots but got a different count."),
				() -> assertEquals(3, tree.get("outsideShots").asInt(), "Error: expected 3 outside shots but got a different count."),
				() -> assertEquals(0, tree.get("missedShots").asInt(), "Error: expected 0 missed shots but got a different count.")
		);
	}

	@Test
	@DisplayName("processEnemyFire uses singular wording for one valid, one repeated and one outside shot")
	void processEnemyFireUsesSingularWordingForOneValidRepeatedAndOutsideShot() throws Exception {
		move = new Move(
				6,
				defaultShots(),
				List.of(
						shotResult(true, false, null, false),
						shotResult(true, true, null, false),
						shotResult(false, false, null, false)
				)
		);

		ProcessedMove processed = processWithCapturedOutput(move, true);
		JsonNode tree = OBJECT_MAPPER.readTree(processed.json());

		assertAll(
				() -> assertTrue(processed.consoleOutput().contains("Jogada nº6 -> 1 tiro válido: 1 tiro na água, 1 tiro repetido, 1 tiro exterior"), "Error: expected singular wording for one valid, one repeated and one outside shot."),
				() -> assertEquals(1, tree.get("validShots").asInt(), "Error: expected 1 valid shot but got a different count."),
				() -> assertEquals(1, tree.get("repeatedShots").asInt(), "Error: expected 1 repeated shot but got a different count."),
				() -> assertEquals(1, tree.get("outsideShots").asInt(), "Error: expected 1 outside shot but got a different count."),
				() -> assertEquals(1, tree.get("missedShots").asInt(), "Error: expected 1 missed shot but got a different count.")
		);
	}

	@Test
	@DisplayName("processEnemyFire aggregates plural hits on the same unsunk boat")
	void processEnemyFireAggregatesPluralHitsOnTheSameUnsunkBoat() throws Exception {
		IShip frigate = new Frigate(Compass.EAST, new Position(4, 4));
		move = new Move(
				7,
				defaultShots(),
				List.of(
						shotResult(true, false, frigate, false),
						shotResult(true, false, frigate, false),
						shotResult(false, false, null, false)
				)
		);

		ProcessedMove processed = processWithCapturedOutput(move, true);
		JsonNode tree = OBJECT_MAPPER.readTree(processed.json());

		assertAll(
				() -> assertTrue(processed.consoleOutput().contains("Jogada nº7 -> 2 tiros válidos:"), "Error: expected verbose output to mention the valid shot count for move 7."),
				() -> assertTrue(processed.consoleOutput().contains("2 tiros num(a) Fragata"), "Error: expected verbose output to aggregate two hits on Fragata."),
				() -> assertTrue(processed.consoleOutput().contains("1 tiro exterior"), "Error: expected verbose output to mention the outside shot."),
				() -> assertEquals(2, tree.get("validShots").asInt(), "Error: expected 2 valid shots but got a different count."),
				() -> assertEquals(0, tree.get("repeatedShots").asInt(), "Error: expected 0 repeated shots but got a different count."),
				() -> assertEquals(1, tree.get("outsideShots").asInt(), "Error: expected 1 outside shot but got a different count."),
				() -> assertEquals(0, tree.get("missedShots").asInt(), "Error: expected 0 missed shots but got a different count."),
				() -> assertEquals(0, tree.get("sunkBoats").size(), "Error: expected no sunk boats but got unexpected entries."),
				() -> assertEquals(1, tree.get("hitsOnBoats").size(), "Error: expected a single aggregated hit entry for Fragata."),
				() -> assertEquals("Fragata", tree.get("hitsOnBoats").get(0).get("type").asText(), "Error: expected the aggregated hit entry to refer to Fragata."),
				() -> assertEquals(2, tree.get("hitsOnBoats").get(0).get("hits").asInt(), "Error: expected two hits on Fragata but got a different count.")
		);
	}

	private static List<IPosition> defaultShots() {
		return List.of(new Position(0, 0), new Position(0, 1), new Position(0, 2));
	}

	private static IGame.ShotResult shotResult(boolean valid, boolean repeated, IShip ship, boolean sunk) {
		return new IGame.ShotResult(valid, repeated, ship, sunk);
	}

	private static ProcessedMove processWithCapturedOutput(Move moveUnderTest, boolean verbose) {
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		PrintStream originalOut = System.out;
		try {
			System.setOut(new PrintStream(output, true, StandardCharsets.UTF_8));
			String json = moveUnderTest.processEnemyFire(verbose);
			return new ProcessedMove(json, output.toString(StandardCharsets.UTF_8));
		} finally {
			System.setOut(originalOut);
		}
	}

	private record ProcessedMove(String json, String consoleOutput) {
	}
}
