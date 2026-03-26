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

	/**
	 * This task also tests the fighting element of a round of three shots.
	 */
	public static void menu() {
		IFleet myFleet = null;
		IGame game = null;
		menuHelp();

		System.out.print("> ");
		Scanner in = new Scanner(System.in);
		String command = in.next();
		while (!command.equals(DESISTIR)) {
			switch (command) {
				case GERAFROTA:
					myFleet = Fleet.createRandom();
					game = new Game(myFleet);
					game.printMyBoard(false, true);
					break;
				case LEFROTA:
					myFleet = buildFleet(in);
					game = new Game(myFleet);
					game.printMyBoard(false, true);
					break;
				case STATUS:
					if (myFleet != null)
						myFleet.printStatus();
					break;
				case MAPA:
					if (myFleet != null)
						game.printMyBoard(false, true);
					break;
				case RAJADA:
					if (game != null) {
						game.readEnemyFire(in);
						myFleet.printStatus();
						game.printMyBoard(true, false);

						if (game.getRemainingShips() == 0) {
							game.over();
							System.exit(0);
						}
					}
					break;
				case SIMULA:
					if (game != null) {
						while (game.getRemainingShips() > 0) {
							game.randomEnemyFire();
							myFleet.printStatus();
							game.printMyBoard(true, false);
							try {
								Thread.sleep(3000);
							} catch (InterruptedException e) {
								Thread.currentThread().interrupt();
							}
						}

						if (game.getRemainingShips() == 0) {
							game.over();
							System.exit(0);
						}
					}
					break;
				case TIROS:
					if (game != null)
						game.printMyBoard(true, true);
					break;
				case HISTORICO:
					GameHistory gameHistory = new GameHistory();
					List<String> history = gameHistory.getHistory();
					System.out.println("======================= HISTORICO DE JOGOS =======================");
					for (String entry : history) {
						System.out.println(entry);
					}
					System.out.println("===============================================================");
					break;
				case AJUDA:
					menuHelp();
					break;
				default:
					System.out.println(Messages.get("menu.unknown"));
			}
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
		printHelpLine(GERAFROTA, "menu.description.gerafrota");
		printHelpLine(LEFROTA, "menu.description.lefrota");
		printHelpLine(STATUS, "menu.description.estado");
		printHelpLine(MAPA, "menu.description.mapa");
		printHelpLine(RAJADA, "menu.description.rajada");
		printHelpLine(SIMULA, "menu.description.simula");
		printHelpLine(TIROS, "menu.description.tiros");
		printHelpLine(HISTORICO, "menu.description.historico");
		printHelpLine(DESISTIR, "menu.description.desisto");
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
		int i = 0;
		while (i < Fleet.FLEET_SIZE) {
			IShip s = readShip(in);
			if (s != null) {
				boolean success = fleet.addShip(s);
				if (success)
					i++;
				else
					LOGGER.info("Falha na criacao de {} {} {}", s.getCategory(), s.getBearing(), s.getPosition());
			} else {
				LOGGER.info("Navio desconhecido!");
			}
		}
		LOGGER.info("{} navios adicionados com sucesso!", i);
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
}