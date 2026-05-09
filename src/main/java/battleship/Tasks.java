package battleship;

import java.util.List;
import java.util.Scanner;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

/**
 * The type Tasks.
 */
public class Tasks {
	/**
	 * The constant LOGGER.
	 */
	private static final Logger LOGGER = LogManager.getLogger(Tasks.class);

	/**
	 * Strings to be used by the user.
	 */
	private static final String AJUDA = "ajuda";
	private static final String GERAFROTA = "gerafrota";
	private static final String LEFROTA = "lefrota";
	private static final String DESISTIR = "desisto";
	private static final String RAJADA = "rajada";
	private static final String TIROS = "tiros";
	private static final String MAPA = "mapa";
	private static final String STATUS = "estado";
	private static final String SIMULA = "simula";
	private static final String HISTORICO = "historico";
	private static final String HISTORY_HEADER = "======================= HISTORICO DE JOGOS =======================";
	private static final String HISTORY_FOOTER = "===============================================================";
	private static final String[][] HELP_LINES = {
			{GERAFROTA, "menu.description.gerafrota"},
			{LEFROTA, "menu.description.lefrota"},
			{STATUS, "menu.description.estado"},
			{MAPA, "menu.description.mapa"},
			{RAJADA, "menu.description.rajada"},
			{SIMULA, "menu.description.simula"},
			{TIROS, "menu.description.tiros"},
			{HISTORICO, "menu.description.historico"},
			{DESISTIR, "menu.description.desisto"}
	};

	/**
	 * This task also tests the fighting element of a round of three shots.
	 */
	public static void menu() {
		MenuState state = new MenuState();
		menuHelp();

		System.out.print("> ");
		Scanner in = new Scanner(System.in);
		String command = in.next();
		while (!command.equals(DESISTIR)) {
			handleCommand(command, in, state);
			System.out.print("> ");
			command = in.next();
		}
		System.out.println(Messages.get("menu.goodbye"));
	}

	/**
	 * This function provides help information about the menu commands.
	 */
	public static void menuHelp() {
		System.out.println(Messages.get("menu.help.header"));
		System.out.println(Messages.get("menu.help.instructions"));
		for (String[] helpLine : HELP_LINES) {
			printHelpLine(helpLine[0], helpLine[1]);
		}
		System.out.println(Messages.get("menu.help.footer"));
	}

	private static void printHelpLine(String command, String descriptionKey) {
		System.out.println("- " + command + ": " + Messages.get(descriptionKey));
	}

	/**
	 * This operation allows the build up of a fleet, given user data.
	 *
	 * @param in The scanner to read from
	 * @return The fleet that has been built
	 */
	public static Fleet buildFleet(Scanner in) {
		assert in != null;

		Fleet fleet = new Fleet();
		int addedShips = 0;
		while (addedShips < Fleet.FLEET_SIZE) {
			IShip ship = readShip(in);
			if (addShipIfPossible(fleet, ship)) {
				addedShips++;
			}
		}
		LOGGER.info("{} navios adicionados com sucesso!", addedShips);
		return fleet;
	}

	/**
	 * This operation reads data about a ship, builds it, and returns it.
	 *
	 * @param in The scanner to read from
	 * @return The created ship based on the data that has been read
	 */
	public static Ship readShip(Scanner in) {
		assert in != null;

		String shipKind = in.next();
		Position pos = readPosition(in);
		char c = in.next().charAt(0);
		Compass bearing = Compass.charToCompass(c);
		return Ship.buildShip(shipKind, bearing, pos);
	}

	/**
	 * This operation allows reading a position in the map.
	 *
	 * @param in The scanner to read from
	 * @return The position that has been read
	 */
	public static Position readPosition(Scanner in) {
		assert in != null;

		int row = in.nextInt();
		int column = in.nextInt();
		return new Position(row, column);
	}

	/**
	 * This operation allows reading a classic position in the map.
	 *
	 * @param in The scanner to read from
	 * @return The classic position that has been read
	 */
	public static IPosition readClassicPosition(@NotNull Scanner in) {
		if (!in.hasNext()) {
			throw new IllegalArgumentException(Messages.get("position.none"));
		}

		String part1 = in.next();
		String part2 = null;

		if (in.hasNextInt()) {
			part2 = in.next();
		}

		String input = (part2 != null) ? part1 + part2 : part1;
		input = input.toUpperCase();

		if (input.matches("[A-Z]\\d+")) {
			char column = input.charAt(0);
			int row = Integer.parseInt(input.substring(1));
			return new Position(column, row);
		}
		if (part2 != null && part1.matches("[A-Z]") && part2.matches("\\d+")) {
			char column = part1.charAt(0);
			int row = Integer.parseInt(part2);
			return new Position(column, row);
		}

		throw new IllegalArgumentException(Messages.get("position.invalid"));
	}

	private static void handleCommand(String command, Scanner in, MenuState state) {
		switch (command) {
			case GERAFROTA:
				loadFleet(Fleet.createRandom(), state);
				break;
			case LEFROTA:
				loadFleet(buildFleet(in), state);
				break;
			case STATUS:
				printFleetStatus(state);
				break;
			case MAPA:
				printFleetMap(state);
				break;
			case RAJADA:
				processBurst(in, state);
				break;
			case SIMULA:
				simulateGame(state);
				break;
			case TIROS:
				printShots(state);
				break;
			case HISTORICO:
				printHistory(new GameHistory());
				break;
			case AJUDA:
				menuHelp();
				break;
			default:
				System.out.println(Messages.get("menu.unknown"));
		}
	}

	private static void loadFleet(IFleet fleet, MenuState state) {
		state.myFleet = fleet;
		state.game = new Game(fleet);
		state.game.printMyBoard(false, true);
	}

	private static void printFleetStatus(MenuState state) {
		if (state.myFleet != null) {
			state.myFleet.printStatus();
		}
	}

	private static void printFleetMap(MenuState state) {
		if (state.game != null) {
			state.game.printMyBoard(false, true);
		}
	}

	private static void processBurst(Scanner in, MenuState state) {
		if (state.game != null) {
			state.game.readEnemyFire(in);
			state.myFleet.printStatus();
			state.game.printMyBoard(true, false);
			exitIfGameOver(state.game);
		}
	}

	private static void simulateGame(MenuState state) {
		if (state.game != null) {
			while (state.game.getRemainingShips() > 0) {
				state.game.randomEnemyFire();
				state.myFleet.printStatus();
				state.game.printMyBoard(true, false);
				pauseSimulation();
			}
			exitIfGameOver(state.game);
		}
	}

	private static void printShots(MenuState state) {
		if (state.game != null) {
			state.game.printMyBoard(true, true);
		}
	}

	private static void printHistory(GameHistory gameHistory) {
		List<String> history = gameHistory.getHistory();
		System.out.println(HISTORY_HEADER);
		for (String entry : history) {
			System.out.println(entry);
		}
		System.out.println(HISTORY_FOOTER);
	}

	private static boolean addShipIfPossible(Fleet fleet, IShip ship) {
		if (ship == null) {
			LOGGER.info("Navio desconhecido!");
			return false;
		}

		boolean success = fleet.addShip(ship);
		if (!success) {
			LOGGER.info("Falha na criacao de {} {} {}", ship.getCategory(), ship.getBearing(), ship.getPosition());
		}
		return success;
	}

	private static void pauseSimulation() {
		try {
			Thread.sleep(3000);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
	}

	private static void exitIfGameOver(IGame game) {
		if (game.getRemainingShips() == 0) {
			game.over();
			System.exit(0);
		}
	}

	private static final class MenuState {
		private IFleet myFleet;
		private IGame game;
	}
}
