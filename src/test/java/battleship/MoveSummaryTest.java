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
 * Test class for class MoveSummary.
 * Author: Eduardo Sousa
 * Date: 2026-04-26 17:24
 * Cyclomatic Complexity:
 * - constructor: 1
 */
class MoveSummaryTest {

	private List<ShotSummary> shotSummaries;
	private MoveSummary moveSummary;

	@BeforeEach
	void setUp() {
		shotSummaries = new ArrayList<>(List.of(
				new ShotSummary("A1", "Agua"),
				new ShotSummary("B2", "Afundou Barca")
		));
		moveSummary = new MoveSummary(2, 2, 1, 0, 1, 1, 1, shotSummaries);
	}

	@AfterEach
	void tearDown() {
		moveSummary = null;
		shotSummaries = null;
	}

	@Test
	@DisplayName("MoveSummary exposes constructor arguments through record accessors")
	void moveSummaryExposesConstructorArgumentsThroughAccessors() {
		assertAll(
				() -> assertEquals(2, moveSummary.number(), "Error: expected the move number to match the assigned value."),
				() -> assertEquals(2, moveSummary.validShots(), "Error: expected validShots to match the assigned value."),
				() -> assertEquals(1, moveSummary.repeatedShots(), "Error: expected repeatedShots to match the assigned value."),
				() -> assertEquals(0, moveSummary.outsideShots(), "Error: expected outsideShots to match the assigned value."),
				() -> assertEquals(1, moveSummary.missedShots(), "Error: expected missedShots to match the assigned value."),
				() -> assertEquals(1, moveSummary.hits(), "Error: expected hits to match the assigned value."),
				() -> assertEquals(1, moveSummary.sunkShips(), "Error: expected sunkShips to match the assigned value."),
				() -> assertEquals("A1", moveSummary.shotSummaries().get(0).position(), "Error: expected the first nested shot position to match the assigned value."),
				() -> assertEquals("Afundou Barca", moveSummary.shotSummaries().get(1).outcome(), "Error: expected the second nested shot outcome to match the assigned value.")
		);
	}

	@Test
	@DisplayName("MoveSummary makes a defensive immutable copy of shotSummaries")
	void moveSummaryMakesADefensiveImmutableCopyOfShotSummaries() {
		shotSummaries.add(new ShotSummary("C3", "Repetido"));

		assertAll(
				() -> assertEquals(2, moveSummary.shotSummaries().size(), "Error: expected shotSummaries to remain detached from later source list mutations."),
				() -> assertThrows(UnsupportedOperationException.class, () -> moveSummary.shotSummaries().add(new ShotSummary("D4", "Exterior")), "Error: expected shotSummaries to be immutable.")
		);
	}
}
