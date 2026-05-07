package battleship;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.util.*;

/**
 * Default {@link IMove} implementation used to store a numbered burst of shots.
 */
public class Move implements IMove {
	private static final ObjectMapper JSON_MAPPER = createIndentedObjectMapper();

	//-------------------------------------------------------------------
	private final int number;
	private final List<IPosition> shots;
	private final List<IGame.ShotResult> shotResults;

	//-------------------------------------------------------------------
	public Move(int moveNumber, List<IPosition> moveShots, List<IGame.ShotResult> moveResults) {
		this.number = moveNumber;
		this.shots = moveShots;
		this.shotResults = moveResults;
	}

	@Override
	public String toString() {
		return "Move{" +
				"number=" + number +
				", shots=" + shots.size() +
				", results=" + shotResults.size() +
				'}';
	}

	@Override
	public int getNumber() {
		return this.number;
	}

	@Override
	public List<IPosition> getShots() {
		return this.shots;
	}

	@Override
	public List<IGame.ShotResult> getShotResults() {
		return this.shotResults;
	}

	/**
	 * Processes the results of enemy fire on the game board, analyzing the outcomes of shots,
	 * such as valid shots, repeated shots, missed shots, hits on ships, and sunk ships. It can
	 * also display a detailed summary of the shot results if verbose mode is activated.
	 *
	 * @param verbose a boolean indicating whether a detailed summary should be printed to the console
	 *                for the processed enemy fire data.
	 * @return a JSON-formatted string that encapsulates the results, including counts of valid shots,
	 *         repeated shots, missed shots, shots outside the game board, and details of hits and
	 *         sunk ships.
	 */
	@Override
	public String processEnemyFire(boolean verbose) {

		ShotAggregation aggregation = aggregateShotResults();

		if (verbose) {
			System.out.println("Jogada nº" + this.number + " -> " + buildVerboseMessage(
					aggregation.validShots(),
					aggregation.repeatedShots(),
					aggregation.missedShots(),
					aggregation.outsideShots(),
					aggregation.sunkBoatsCount(),
					aggregation.hitsPerBoat()
			));
		}

		return toJson(buildResponse(
				aggregation.validShots(),
				aggregation.repeatedShots(),
				aggregation.missedShots(),
				aggregation.outsideShots(),
				aggregation.sunkBoatsCount(),
				aggregation.hitsPerBoat()
		));
	}

	private ShotAggregation aggregateShotResults() {
		int validShots = 0;
		int repeatedShots = 0;
		int missedShots = 0;
		Map<String, Integer> sunkBoatsCount = new HashMap<>();
		Map<String, Integer> hitsPerBoat = new HashMap<>();

		for (IGame.ShotResult result : this.shotResults) {
			if (!result.valid()) {
				continue;
			}
			if (result.repeated()) {
				repeatedShots++;
				continue;
			}

			validShots++;
			if (result.ship() == null) {
				missedShots++;
				continue;
			}

			registerBoatImpact(result, hitsPerBoat, sunkBoatsCount);
		}

		int outsideShots = Game.NUMBER_SHOTS - validShots - repeatedShots;
		return new ShotAggregation(validShots, repeatedShots, missedShots, outsideShots, sunkBoatsCount, hitsPerBoat);
	}

	private static void registerBoatImpact(IGame.ShotResult result, Map<String, Integer> hitsPerBoat, Map<String, Integer> sunkBoatsCount) {
		String boatName = result.ship().getCategory();
		hitsPerBoat.put(boatName, hitsPerBoat.getOrDefault(boatName, 0) + 1);
		if (result.sunk()) {
			sunkBoatsCount.put(boatName, sunkBoatsCount.getOrDefault(boatName, 0) + 1);
		}
	}

	private String buildVerboseMessage(int validShots, int repeatedShots, int missedShots, int outsideShots,
			Map<String, Integer> sunkBoatsCount, Map<String, Integer> hitsPerBoat) {
		StringBuilder output = new StringBuilder();

		if (validShots == 0 && repeatedShots > 0) {
			output.append(repeatedShots).append(" tiro").append(repeatedShots > 1 ? "s" : "").append(" repetido").append(repeatedShots > 1 ? "s" : "");
		} else {
			appendValidShotSummary(output, validShots);
			appendSunkBoats(output, sunkBoatsCount);
			appendUnsunkHits(output, hitsPerBoat, sunkBoatsCount);
			appendMissedShots(output, missedShots, sunkBoatsCount, hitsPerBoat);
			appendRepeatedShots(output, validShots, repeatedShots);
		}

		appendOutsideShots(output, outsideShots);
		return output.toString();
	}

	private static void appendValidShotSummary(StringBuilder output, int validShots) {
		if (validShots > 0) {
			output.append(validShots).append(" tiro").append(validShots > 1 ? "s" : "").append(" válido").append(validShots > 1 ? "s" : "").append(": ");
		}
	}

	private static void appendSunkBoats(StringBuilder output, Map<String, Integer> sunkBoatsCount) {
		for (Map.Entry<String, Integer> entry : sunkBoatsCount.entrySet()) {
			String boatName = entry.getKey();
			int count = entry.getValue();
			output.append(count).append(" ").append(boatName).append(count > 1 ? "s" : "").append(" ao fundo").append(" + ");
		}
	}

	private static void appendUnsunkHits(StringBuilder output, Map<String, Integer> hitsPerBoat, Map<String, Integer> sunkBoatsCount) {
		for (Map.Entry<String, Integer> entry : hitsPerBoat.entrySet()) {
			String boatName = entry.getKey();
			int hits = entry.getValue();
			if (!sunkBoatsCount.containsKey(boatName)) {
				output.append(hits).append(" tiro").append(hits > 1 ? "s" : "").append(" num(a) ").append(boatName).append(" + ");
			}
		}
	}

	private static void appendMissedShots(StringBuilder output, int missedShots, Map<String, Integer> sunkBoatsCount,
			Map<String, Integer> hitsPerBoat) {
		if (missedShots > 0) {
			output.append(missedShots).append(" tiro").append(missedShots > 1 ? "s" : "").append(" na água");
		} else if (!sunkBoatsCount.isEmpty() || !hitsPerBoat.isEmpty()) {
			output.setLength(output.length() - 2);
		}
	}

	private static void appendRepeatedShots(StringBuilder output, int validShots, int repeatedShots) {
		if (repeatedShots > 0) {
			if (validShots > 0) {
				output.append(", ");
			}
			output.append(repeatedShots).append(" tiro").append(repeatedShots > 1 ? "s" : "").append(" repetido").append(repeatedShots > 1 ? "s" : "");
		}
	}

	private static void appendOutsideShots(StringBuilder output, int outsideShots) {
		if (outsideShots > 0) {
			if (!output.isEmpty()) {
				output.append(", ");
			}
			output.append(outsideShots).append(" tiro").append(outsideShots > 1 ? "s" : "").append(" exterior").append(outsideShots > 1 ? "es" : "");
		}
	}

	private static Map<String, Object> buildResponse(int validShots, int repeatedShots, int missedShots, int outsideShots,
			Map<String, Integer> sunkBoatsCount, Map<String, Integer> hitsPerBoat) {
		Map<String, Object> response = new HashMap<>();
		response.put("validShots", validShots);
		response.put("outsideShots", outsideShots);
		response.put("repeatedShots", repeatedShots);
		response.put("missedShots", missedShots);
		response.put("sunkBoats", buildSunkBoats(sunkBoatsCount));
		response.put("hitsOnBoats", buildBoatHits(hitsPerBoat, sunkBoatsCount));
		return response;
	}

	private static List<Map<String, Object>> buildSunkBoats(Map<String, Integer> sunkBoatsCount) {
		List<Map<String, Object>> sunkBoats = new ArrayList<>();
		for (Map.Entry<String, Integer> entry : sunkBoatsCount.entrySet()) {
			Map<String, Object> boat = new HashMap<>();
			boat.put("type", entry.getKey());
			boat.put("count", entry.getValue());
			sunkBoats.add(boat);
		}
		return sunkBoats;
	}

	private static List<Map<String, Object>> buildBoatHits(Map<String, Integer> hitsPerBoat, Map<String, Integer> sunkBoatsCount) {
		List<Map<String, Object>> boatHits = new ArrayList<>();
		for (Map.Entry<String, Integer> entry : hitsPerBoat.entrySet()) {
			if (!sunkBoatsCount.containsKey(entry.getKey())) {
				Map<String, Object> boat = new HashMap<>();
				boat.put("type", entry.getKey());
				boat.put("hits", entry.getValue());
				boatHits.add(boat);
			}
		}
		return boatHits;
	}

	private static ObjectMapper createIndentedObjectMapper() {
		return new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
	}

	private static String toJson(Map<String, Object> response) {
		try {
			return JSON_MAPPER.writeValueAsString(response);
		} catch (JsonProcessingException e) {
			throw new RuntimeException("Erro ao serializar o JSON dos resultados da jogada", e);
		}
	}

	private record ShotAggregation(
			int validShots,
			int repeatedShots,
			int missedShots,
			int outsideShots,
			Map<String, Integer> sunkBoatsCount,
			Map<String, Integer> hitsPerBoat
	) {
	}
}
