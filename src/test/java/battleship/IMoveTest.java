package battleship;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class IMoveTest {

	@Test
	@DisplayName("readMove parses the declared number of numeric shot coordinates")
	void readMoveParsesDeclaredShotCoordinates() {
		Move move = IMove.readMove(7, new Scanner("3 0 0 1 2 9 9"));

		assertAll(
				() -> assertEquals(7, move.getNumber(), "Error: expected IMove.readMove to keep the supplied move number."),
				() -> assertEquals(3, move.getShots().size(), "Error: expected IMove.readMove to create three parsed shots."),
				() -> assertEquals(new Position(0, 0), move.getShots().get(0), "Error: expected the first parsed shot to be at row 0 column 0."),
				() -> assertEquals(new Position(1, 2), move.getShots().get(1), "Error: expected the second parsed shot to be at row 1 column 2."),
				() -> assertEquals(new Position(9, 9), move.getShots().get(2), "Error: expected the third parsed shot to be at row 9 column 9."),
				() -> assertTrue(move.getShotResults().isEmpty(), "Error: expected IMove.readMove to initialize an empty shot-results list.")
		);
	}

	@Test
	@DisplayName("readMove also supports moves without parsed shots")
	void readMoveSupportsZeroShotMoves() {
		Move move = IMove.readMove(8, new Scanner("0"));

		assertAll(
				() -> assertEquals(8, move.getNumber(), "Error: expected IMove.readMove to keep the supplied move number."),
				() -> assertTrue(move.getShots().isEmpty(), "Error: expected a declared zero-shot move to contain no parsed shots."),
				() -> assertTrue(move.getShotResults().isEmpty(), "Error: expected a declared zero-shot move to contain no parsed results.")
		);
	}
}
