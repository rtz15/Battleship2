package battleship;

import java.util.List;

public record GameSummary(
		String title,
		String finalResult,
		String finalMessage,
		int totalMoves,
		int totalShots,
		int totalHits,
		int totalRepeatedShots,
		int totalOutsideShots,
		int totalMissedShots,
		int totalSunkShips,
		int remainingShips,
		List<String> fleetStatus,
		List<MoveSummary> moveSummaries
) {
	public GameSummary {
		fleetStatus = List.copyOf(fleetStatus);
		moveSummaries = List.copyOf(moveSummaries);
	}
}
