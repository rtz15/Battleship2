package battleship;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Test class for class GameSummary.
 * Author: Eduardo Sousa
 * Date: 2026-04-26 17:24
 * Cyclomatic Complexity:
 * - constructor: 1
 */
class GameSummaryTest {

	private List<String> fleetStatus;
	private List<MoveSummary> moveSummaries;
	private GameSummary gameSummary;

	@BeforeEach
	void setUp() {
		fleetStatus = new ArrayList<>(List.of("Barca @ A1 - Afundado"));
		moveSummaries = new ArrayList<>(List.of(
				new MoveSummary(1, 2, 0, 1, 1, 1, 0, List.of(new ShotSummary("A1", "Agua")))
		));
		gameSummary = new GameSummary(
				"Resumo da simulacao Battleship",
				"Todos os navios do jogador foram afundados",
				"Mensagem final",
				4,
				12,
				5,
				1,
				2,
				6,
				3,
				0,
				fleetStatus,
				moveSummaries
		);
	}

	@AfterEach
	void tearDown() {
		gameSummary = null;
		fleetStatus = null;
		moveSummaries = null;
	}

	@Test
	@DisplayName("GameSummary exposes constructor arguments through record accessors")
	void gameSummaryExposesConstructorArgumentsThroughAccessors() {
		assertAll(
				() -> assertEquals("Resumo da simulacao Battleship", gameSummary.title(), "Error: expected the summary title to match the assigned value."),
				() -> assertEquals("Todos os navios do jogador foram afundados", gameSummary.finalResult(), "Error: expected the final result to match the assigned value."),
				() -> assertEquals("Mensagem final", gameSummary.finalMessage(), "Error: expected the final message to match the assigned value."),
				() -> assertEquals(4, gameSummary.totalMoves(), "Error: expected the total move count to match the assigned value."),
				() -> assertEquals(12, gameSummary.totalShots(), "Error: expected the total shot count to match the assigned value."),
				() -> assertEquals(5, gameSummary.totalHits(), "Error: expected the total hit count to match the assigned value."),
				() -> assertEquals(1, gameSummary.totalRepeatedShots(), "Error: expected the repeated shot count to match the assigned value."),
				() -> assertEquals(2, gameSummary.totalOutsideShots(), "Error: expected the outside shot count to match the assigned value."),
				() -> assertEquals(6, gameSummary.totalMissedShots(), "Error: expected the missed shot count to match the assigned value."),
				() -> assertEquals(3, gameSummary.totalSunkShips(), "Error: expected the sunk ship count to match the assigned value."),
				() -> assertEquals(0, gameSummary.remainingShips(), "Error: expected the remaining ship count to match the assigned value."),
				() -> assertEquals("Barca @ A1 - Afundado", gameSummary.fleetStatus().get(0), "Error: expected the fleet status entry to match the assigned value."),
				() -> assertEquals(1, gameSummary.moveSummaries().get(0).number(), "Error: expected the nested move summary to preserve its move number.")
		);
	}

	@Test
	@DisplayName("GameSummary makes defensive immutable copies of its collection arguments")
	void gameSummaryMakesDefensiveImmutableCopiesOfItsCollectionArguments() {
		fleetStatus.add("Fragata @ B2 - A flutuar");
		moveSummaries.add(new MoveSummary(2, 1, 1, 1, 0, 1, 1, List.of()));

		assertAll(
				() -> assertEquals(1, gameSummary.fleetStatus().size(), "Error: expected fleetStatus to remain detached from later source list mutations."),
				() -> assertEquals(1, gameSummary.moveSummaries().size(), "Error: expected moveSummaries to remain detached from later source list mutations."),
				() -> assertThrows(UnsupportedOperationException.class, () -> gameSummary.fleetStatus().add("Nova entrada"), "Error: expected fleetStatus to be immutable."),
				() -> assertThrows(UnsupportedOperationException.class, () -> gameSummary.moveSummaries().clear(), "Error: expected moveSummaries to be immutable.")
		);
	}
}
