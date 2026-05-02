package battleship;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Test class for class ShotSummary.
 * Author: Eduardo Sousa
 * Date: 2026-04-26 17:24
 * Cyclomatic Complexity:
 * - constructor: 1
 */
class ShotSummaryTest {

	private ShotSummary shotSummary;

	@BeforeEach
	void setUp() {
		shotSummary = new ShotSummary("A1", "Agua");
	}

	@AfterEach
	void tearDown() {
		shotSummary = null;
	}

	@Test
	@DisplayName("ShotSummary keeps the assigned position and outcome")
	void shotSummaryKeepsAssignedPositionAndOutcome() {
		assertAll(
				() -> assertEquals("A1", shotSummary.position(), "Error: expected the recorded shot position to match the assigned value."),
				() -> assertEquals("Agua", shotSummary.outcome(), "Error: expected the recorded shot outcome to match the assigned value."),
				() -> assertEquals(new ShotSummary("A1", "Agua"), shotSummary, "Error: expected equivalent ShotSummary records to compare equal."),
				() -> assertEquals(new ShotSummary("A1", "Agua").hashCode(), shotSummary.hashCode(), "Error: expected equivalent ShotSummary records to have the same hash code."),
				() -> assertEquals("ShotSummary[position=A1, outcome=Agua]", shotSummary.toString(), "Error: expected ShotSummary toString to follow the record format.")
		);
	}
}
