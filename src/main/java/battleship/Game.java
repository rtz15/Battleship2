package battleship;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.io.IOException;
import java.nio.file.Path;
import java.sql.Timestamp;
import java.util.*;

public class Game implements IGame
{
	/**
	 * Board dimension used by the simulator.
	 */
	public static final int BOARD_SIZE = 10;
	/**
	 * Number of shots that compose one burst.
	 */
	public static final int NUMBER_SHOTS = 3;

	private static final String GAME_OVER_MESSAGE = "Maldito sejas, Java Sparrow, eu voltarei, glub glub glub ...";
	private static final ObjectMapper JSON_MAPPER = createIndentedObjectMapper();

	/**
	 * Prints the game board by representing the positions of ships, adjacent tiles,
	 * shots, and other game elements onto the console. The method also optionally
	 * displays shot positions and a legend explaining the symbols used on the board.
	 *
	 * @param fleet       the fleet of ships to be displayed on the board. Ships are marked
	 *                    and their positions are shown according to their placement.
	 * @param moves       the list of moves containing shots. If shot positions are shown,
	 *                    they will be rendered based on their outcome (hit, miss, etc.).
	 * @param show_shots  if true, displays the shots taken during the game and marks
	 *                    their result (hit or miss) on the board.
	 * @param showLegend  if true, displays an explanatory legend of the symbols used
	 *                    to represent various elements such as ships, misses, hits, etc.
	 */
	public static void printBoard(IFleet fleet, List<IMove> moves, boolean show_shots, boolean showLegend) {

		assert fleet != null;
		assert moves != null;

		char[][] map = createEmptyBoard();
		paintFleet(map, fleet);
		if (show_shots) {
			paintShots(map, moves);
		}

		printBoardFrame(map, showLegend);
	}

	/**
	 * Serializes a list of shot positions into a JSON string. Each shot is represented
	 * with its classic row and column values. The method uses the Jackson library for
	 * JSON serialization.
	 *
	 * @param shots a list of shot positions to be serialized. Each position is represented
	 *              by an implementation of the {@code IPosition} interface. The list must
	 *              not be null.
	 * @return a formatted JSON string containing the shot positions. Each shot includes
	 *         its classic row and column.
	 * @throws RuntimeException if an error occurs during JSON serialization.
	 */
	public static String jsonShots(List<IPosition> shots) {
		assert shots != null;

		return toJson(toClassicShotMaps(shots), "Erro ao serializar o JSON");
	}

	private static final char EMPTY_MARKER = '.';
	private static final char SHIP_MARKER = '#';
	private static final char SHOT_SHIP_MARKER = '*';
	private static final char SHOT_WATER_MARKER = 'o';
	private static final char SHIP_ADJACENT_MARKER = '-';

	//------------------------------------------------------------------
	private final IFleet myFleet;
	private final List<IMove> alienMoves;

	private final IFleet alienFleet;
	private final List<IMove> myMoves;

	private Integer countInvalidShots;
	private Integer countRepeatedShots;
	private Integer countHits;
	private Integer countSinks;
	private int moveNumber;
	private final GameHistory gameHistory;

	//------------------------------------------------------------------
	public Game(IFleet myFleet)
	{
		this.moveNumber = 1;

		this.alienMoves = new ArrayList<>();
		this.myMoves = new ArrayList<>();

		this.alienFleet = new Fleet();
		this.myFleet = myFleet;

		this.countInvalidShots = 0;
		this.countRepeatedShots = 0;
		this.countHits = 0;
		this.countSinks = 0;
		this.gameHistory = new GameHistory();
	}

	@Override
	public IFleet getMyFleet()
	{
		return myFleet;
	}

	@Override
	public List<IMove> getAlienMoves()
	{
		return alienMoves;
	}

	@Override
	public IFleet getAlienFleet()
	{
		return alienFleet;
	}

	@Override
	public List<IMove> getMyMoves()
	{
		return myMoves;
	}

	/**
	 * Simulates a random firing action by the enemy, generating a set of unique shot coordinates
	 * and serializing them into a JSON string. The method ensures that the random shots are valid
	 * and do not duplicate existing shots in the game or previous enemy moves. After generating
	 * the shots, it applies the firing logic and serializes the result for further processing.
	 *
	 * @return A JSON string representing the list of randomly generated enemy shots.
	 * @throws RuntimeException if there is an error during the JSON serialization of the shots.
	 */
	public String randomEnemyFire() {

		// Criar uma instância de Random com uma seed baseada no timestamp atual
		Random random = new Random(System.currentTimeMillis());
		List<IPosition> shots = selectRandomShots(collectUsableEnemyTargets(), random);
		printBurst(shots);

		this.fireShots(shots);

		return Game.jsonShots(shots);
	}


	/**
	 * Reads and processes the enemy fire input from the specified scanner.
	 * The method expects input describing positions for enemy shots. It verifies
	 * the format, ensures the correct number of positions are provided, and then fires
	 * on those positions.
	 *
	 * @param in the scanner object to read the enemy fire positions from, input must
	 *           be formatted either as a single token combining the column and row
	 *           (e.g., "A3") or as separate tokens (e.g., "A" followed by "3").
	 * @throws IllegalArgumentException if the provided positions are incomplete,
	 *                                  incorrectly formatted, or do not match the
	 *                                  required number of shots (NUMBER_SHOTS).
	 */
	public String readEnemyFire(Scanner in) {

		assert in != null;

		String input = in.nextLine().trim();

		// Criar lista para armazenar os tiros
		List<IPosition> shots = new ArrayList<>();

		try (Scanner inputScanner = new Scanner(input)) {
			while (shots.size() < NUMBER_SHOTS && inputScanner.hasNext()) {
				String token = inputScanner.next();

				if (token.matches("[A-Za-z]")) {
					shots.add(readSeparatedShot(token, inputScanner));
				} else {
					shots.add(readCompactShot(token));
				}
			}
		}

		if (shots.size() != NUMBER_SHOTS) {
			throw new IllegalArgumentException("Você deve inserir exatamente " + NUMBER_SHOTS + " posições!");
		}

		this.fireShots(shots);

		return Game.jsonShots(shots);
	}

	/**
	 * Fires a set of shots during a player's move. Each shot is resolved and
	 * consolidated into a move, which is processed and added to the list of alien moves.
	 * The method ensures exactly {@code NUMBER_SHOTS} shots are fired, validates
	 * each shot's position, and increments the move counter after completing the operation.
	 *
	 * @param shots a list of positions representing the locations to fire shots at.
	 *              The positions should be unique and valid within the bounds of the game board.
	 *              The size of the list must be equal to {@code NUMBER_SHOTS}.
	 * @throws IllegalArgumentException if the list of shots is null, contains an invalid
	 *                                  number of positions, or includes duplicate positions.
	 */
	public void fireShots(List<IPosition> shots)
	{
		assert shots != null;

		validateBurstSize(shots);
		List<ShotResult> shotResults = resolveShotResults(shots);

		Move move = new Move(moveNumber, shots, shotResults);

//		System.out.println(move);

		String responseJson = move.processEnemyFire(true);
		System.out.println(responseJson);

		alienMoves.add(move);

		moveNumber++;
	}

	/**
	 * Fires a single shot at the specified position, handling scenarios such as invalid positions,
	 * repeated shots, hits, misses, and sinking a ship. The method updates the necessary counters
	 * for invalid shots, repeated shots, hits, and sunk ships.
	 *
	 * @param pos the position to fire the shot at; must be valid and within the game board boundaries.
	 * @param isRepeated true if the shot is marked as a repeat attempt, false otherwise.
	 * @return a ShotResult object containing the result of the shot, including whether the shot was
	 *         valid, repeated, a hit, and whether a ship was sunk.
	 */
	public ShotResult fireSingleShot(IPosition pos, boolean isRepeated) {

		assert pos != null;

		if (!pos.isInside()) {
			countInvalidShots++;
			return new ShotResult(false, false, null, false);
		}

		if (isRepeated || repeatedShot(pos)) {
			countRepeatedShots++;
			return new ShotResult(true, true, null, false);
		}

		IShip ship = myFleet.shipAt(pos);
		if (ship == null)
			return new ShotResult(true, false, null, false);
		else
		{
			ship.shoot(pos);
			countHits++;
			if (!ship.stillFloating()) {
				countSinks++;
			}
			return new ShotResult(true, false, ship, !ship.stillFloating());
		}
	}

	@Override
	public int getRepeatedShots()
	{
		return this.countRepeatedShots;
	}

	@Override
	public int getInvalidShots()
	{
		return this.countInvalidShots;
	}

	@Override
	public int getHits()
	{
		return this.countHits;
	}

	@Override
	public int getSunkShips()
	{
		return this.countSinks;
	}

	@Override
	public int getRemainingShips()
	{
		List<IShip> floatingShips = myFleet.getFloatingShips();
		return floatingShips.size();
	}

	public boolean repeatedShot(IPosition pos)
	{
		assert pos != null;

		for (IMove move : alienMoves)
			if (move.getShots().contains(pos))
				return true;
		return false;
	}

	public void printMyBoard(boolean show_shots, boolean show_legend)
	{
		Game.printBoard(this.myFleet, this.alienMoves, show_shots, show_legend);
	}

	public void printAlienBoard(boolean show_shots, boolean show_legend)
	{
		Game.printBoard(this.alienFleet, this.myMoves, show_shots, show_legend);
	}

	GameSummary createSummary() {
		List<MoveSummary> moveSummaries = this.alienMoves.stream()
				.map(this::buildMoveSummary)
				.toList();

		return new GameSummary(
				"Resumo da simulacao Battleship",
				resolveFinalResult(),
				GAME_OVER_MESSAGE,
				this.alienMoves.size(),
				countTotalShots(),
				this.countHits,
				this.countRepeatedShots,
				this.countInvalidShots,
				countTotalMissedShots(moveSummaries),
				this.countSinks,
				this.getRemainingShips(),
				buildFleetStatus(),
				moveSummaries
		);
	}

	public Path exportSummary() throws IOException {
		return exportSummary(PdfExporter.DEFAULT_OUTPUT_PATH);
	}

	public Path exportSummary(Path outputPath) throws IOException {
		return PdfExporter.export(createSummary(), outputPath);
	}

	private MoveSummary buildMoveSummary(IMove move) {
		List<ShotSummary> shotSummaries = new ArrayList<>();
		MoveCounters counters = summarizeMoveResults(move, shotSummaries);

		return new MoveSummary(
				move.getNumber(),
				counters.validShots(),
				counters.repeatedShots(),
				counters.outsideShots(),
				counters.missedShots(),
				counters.hits(),
				counters.sunkShips(),
				shotSummaries
		);
	}

	private String describeShotResult(ShotResult result) {
		if (!result.valid()) {
			return "Exterior";
		}
		if (result.repeated()) {
			return "Repetido";
		}
		if (result.ship() == null) {
			return "Agua";
		}
		if (result.sunk()) {
			return "Afundou " + result.ship().getCategory();
		}
		return "Acertou " + result.ship().getCategory();
	}

	private static ObjectMapper createIndentedObjectMapper() {
		return new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
	}

	private static List<Map<String, Object>> toClassicShotMaps(List<IPosition> shots) {
		List<Map<String, Object>> simplifiedShots = new ArrayList<>();
		for (IPosition shot : shots) {
			Map<String, Object> simplePos = new LinkedHashMap<>();
			simplePos.put("row", String.valueOf(shot.getClassicRow()));
			simplePos.put("column", shot.getClassicColumn());
			simplifiedShots.add(simplePos);
		}
		return simplifiedShots;
	}

	private static String toJson(Object value, String errorMessage) {
		try {
			return JSON_MAPPER.writeValueAsString(value);
		} catch (JsonProcessingException e) {
			throw new RuntimeException(errorMessage, e);
		}
	}

	private List<IPosition> collectUsableEnemyTargets() {
		Set<IPosition> blockedPositions = collectBlockedEnemyTargets();
		List<IPosition> usablePositions = new ArrayList<>();
		for (int row = 0; row < BOARD_SIZE; row++) {
			for (int col = 0; col < BOARD_SIZE; col++) {
				IPosition candidate = new Position(row, col);
				if (!blockedPositions.contains(candidate)) {
					usablePositions.add(candidate);
				}
			}
		}
		return usablePositions;
	}

	private Set<IPosition> collectBlockedEnemyTargets() {
		Set<IPosition> blockedPositions = new HashSet<>();
		this.myFleet.getSunkShips().forEach(ship -> blockedPositions.addAll(ship.getAdjacentPositions()));
		this.alienMoves.forEach(move -> blockedPositions.addAll(move.getShots()));
		return blockedPositions;
	}

	private static void validateBurstSize(List<IPosition> shots) {
		if (shots.size() != NUMBER_SHOTS) {
			throw new IllegalArgumentException("Must fire exactly " + NUMBER_SHOTS + " shots per move.");
		}
	}

	private List<ShotResult> resolveShotResults(List<IPosition> shots) {
		List<ShotResult> shotResults = new ArrayList<>();
		Set<IPosition> shotsInBurst = new HashSet<>();
		for (IPosition position : shots) {
			shotResults.add(fireSingleShot(position, shotsInBurst.contains(position)));
			shotsInBurst.add(position);
		}
		return shotResults;
	}

	private MoveCounters summarizeMoveResults(IMove move, List<ShotSummary> shotSummaries) {
		MoveCounters counters = MoveCounters.empty();
		List<IPosition> shots = move.getShots();
		List<ShotResult> results = move.getShotResults();

		for (int index = 0; index < shots.size(); index++) {
			IPosition shot = shots.get(index);
			ShotResult result = results.get(index);
			counters = counters.record(result);
			shotSummaries.add(new ShotSummary(shot.toString(), describeShotResult(result)));
		}

		return counters;
	}

	private static List<IPosition> selectRandomShots(List<IPosition> candidateShots, Random random) {
		List<IPosition> shots = new ArrayList<>();
		IPosition fallbackShot = null;

		if (candidateShots.size() >= Game.NUMBER_SHOTS) {
			while (shots.size() < Game.NUMBER_SHOTS) {
				IPosition candidate = candidateShots.get(random.nextInt(candidateShots.size()));
				if (!shots.contains(candidate))
					shots.add(candidate);
			}
			return shots;
		}

		while (shots.size() < candidateShots.size()) {
			IPosition candidate = candidateShots.get(random.nextInt(candidateShots.size()));
			if (!shots.contains(candidate)) {
				shots.add(candidate);
				fallbackShot = candidate;
			}
		}

		while (shots.size() < Game.NUMBER_SHOTS)
			shots.add(fallbackShot);

		return shots;
	}

	private static char[][] createEmptyBoard() {
		char[][] map = new char[BOARD_SIZE][BOARD_SIZE];
		for (int row = 0; row < BOARD_SIZE; row++) {
			Arrays.fill(map[row], EMPTY_MARKER);
		}
		return map;
	}

	private static void paintFleet(char[][] map, IFleet fleet) {
		for (IShip ship : fleet.getShips()) {
			paintShip(map, ship);
			paintSunkAdjacency(map, ship);
		}
	}

	private static void paintShip(char[][] map, IShip ship) {
		for (IPosition shipPosition : ship.getPositions()) {
			map[shipPosition.getRow()][shipPosition.getColumn()] = SHIP_MARKER;
		}
	}

	private static void paintSunkAdjacency(char[][] map, IShip ship) {
		if (!ship.stillFloating()) {
			for (IPosition adjacentPosition : ship.getAdjacentPositions()) {
				map[adjacentPosition.getRow()][adjacentPosition.getColumn()] = SHIP_ADJACENT_MARKER;
			}
		}
	}

	private static void paintShots(char[][] map, List<IMove> moves) {
		for (IMove move : moves) {
			for (IPosition shot : move.getShots()) {
				paintShot(map, shot);
			}
		}
	}

	private static void paintShot(char[][] map, IPosition shot) {
		if (!shot.isInside()) {
			return;
		}

		int row = shot.getRow();
		int col = shot.getColumn();
		if (map[row][col] == SHIP_MARKER) {
			map[row][col] = SHOT_SHIP_MARKER;
		}
		if (map[row][col] == EMPTY_MARKER || map[row][col] == SHIP_ADJACENT_MARKER) {
			map[row][col] = SHOT_WATER_MARKER;
		}
	}

	private static void printBoardFrame(char[][] map, boolean showLegend) {
		System.out.println();
		printColumnHeader();
		printTopBorder();
		printRows(map);
		printBottomBorder();
		if (showLegend) {
			printLegend();
		}
		System.out.println();
	}

	private static void printColumnHeader() {
		System.out.print("    ");
		for (int col = 0; col < BOARD_SIZE; col++) {
			System.out.print(" " + (col + 1));
		}
		System.out.println();
	}

	private static void printTopBorder() {
		System.out.print("   +-");
		for (int col = 0; col < BOARD_SIZE; col++) {
			System.out.print("--");
		}
		System.out.println("+");
	}

	private static void printRows(char[][] map) {
		for (int row = 0; row < BOARD_SIZE; row++) {
			Position pos = new Position(row, 0);
			char rowLabel = pos.getClassicRow();
			System.out.print(" " + rowLabel + " |");
			for (int col = 0; col < BOARD_SIZE; col++) {
				System.out.print(" " + map[row][col]);
			}
			System.out.println(" |");
		}
	}

	private static void printBottomBorder() {
		System.out.print("   +");
		for (int col = 0; col < BOARD_SIZE; col++) {
			System.out.print("--");
		}
		System.out.println("-+");
	}

	private static void printLegend() {
		System.out.println(Messages.get("board.legend.title"));
		System.out.println(Messages.format("board.legend.line1", SHIP_MARKER, SHIP_ADJACENT_MARKER, EMPTY_MARKER));
		System.out.println(Messages.format("board.legend.line2", SHOT_SHIP_MARKER, SHOT_WATER_MARKER));
	}

	private static void printBurst(List<IPosition> shots) {
		System.out.println();
		System.out.print("rajada ");
		for (IPosition shot : shots)
			System.out.print(shot + " ");
		System.out.println();
	}

	private static IPosition readSeparatedShot(String token, Scanner inputScanner) {
		if (!inputScanner.hasNextInt()) {
			throw new IllegalArgumentException("Posição incompleta! A coluna '" + token + "' não é seguida por uma linha.");
		}
		int row = inputScanner.nextInt();
		return new Position(token.toUpperCase().charAt(0), row);
	}

	private static IPosition readCompactShot(String token) {
		try (Scanner singleScanner = new Scanner(token)) {
			return Tasks.readClassicPosition(singleScanner);
		}
	}

	private int countTotalShots() {
		return this.alienMoves.stream()
				.mapToInt(move -> move.getShots().size())
				.sum();
	}

	private static int countTotalMissedShots(List<MoveSummary> moveSummaries) {
		return moveSummaries.stream()
				.mapToInt(MoveSummary::missedShots)
				.sum();
	}

	private List<String> buildFleetStatus() {
		List<String> fleetStatus = new ArrayList<>();
		List<IShip> orderedShips = new ArrayList<>(this.myFleet.getShips());
		orderedShips.sort(Comparator
				.comparing(IShip::getCategory)
				.thenComparing(ship -> ship.getPosition().toString()));

		for (IShip ship : orderedShips) {
			String status = ship.stillFloating() ? "A flutuar" : "Afundado";
			fleetStatus.add(ship.getCategory() + " @ " + ship.getPosition() + " - " + status);
		}

		return fleetStatus;
	}

	private String resolveFinalResult() {
		return this.getRemainingShips() == 0
				? "Todos os navios do jogador foram afundados"
				: "A simulacao terminou com navios ainda a flutuar";
	}

	private record MoveCounters(
			int validShots,
			int repeatedShots,
			int outsideShots,
			int missedShots,
			int hits,
			int sunkShips
	) {
		private static MoveCounters empty() {
			return new MoveCounters(0, 0, 0, 0, 0, 0);
		}

		private MoveCounters record(ShotResult result) {
			if (!result.valid()) {
				return new MoveCounters(validShots, repeatedShots, outsideShots + 1, missedShots, hits, sunkShips);
			}
			if (result.repeated()) {
				return new MoveCounters(validShots, repeatedShots + 1, outsideShots, missedShots, hits, sunkShips);
			}
			if (result.ship() == null) {
				return new MoveCounters(validShots + 1, repeatedShots, outsideShots, missedShots + 1, hits, sunkShips);
			}
			if (result.sunk()) {
				return new MoveCounters(validShots + 1, repeatedShots, outsideShots, missedShots, hits + 1, sunkShips + 1);
			}
			return new MoveCounters(validShots + 1, repeatedShots, outsideShots, missedShots, hits + 1, sunkShips);
		}
	}

	public void over() {
		System.out.println();
		System.out.println(Messages.get("game.over.top"));
		System.out.println(Messages.get("game.over.message"));
		System.out.println(Messages.get("game.over.top"));

		try {
			Path pdfPath = exportSummary();
			System.out.println("Resumo PDF gerado em: " + pdfPath.toAbsolutePath());
		} catch (IOException e) {
			System.err.println("Nao foi possivel gerar o PDF: " + e.getMessage());
		}

		int totalMoves = alienMoves.size();
		String result = (getRemainingShips() == 0) ? "WIN" : "LOSS";
		gameHistory.saveGame(
				new Timestamp(System.currentTimeMillis()),
				totalMoves,
				getHits(),
				getSunkShips(),
				getRemainingShips(),
				result
		);
	}
}
