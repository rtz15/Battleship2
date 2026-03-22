package battleship;

import java.util.List;

public record MoveSummary(
		int number,
		int validShots,
		int repeatedShots,
		int outsideShots,
		int missedShots,
		int hits,
		int sunkShips,
		List<ShotSummary> shotSummaries
) {
	public MoveSummary {
		shotSummaries = List.copyOf(shotSummaries);
	}
}
