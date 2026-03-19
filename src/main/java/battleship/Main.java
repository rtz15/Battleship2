package battleship;

/**
 * The type Main.
 *
 * @author britoeabreu
 * @author adrianolopes
 * @author miguelgoulao
 */
public class Main {
	/**
	 * Main.
	 *
	 * @param args the args
	 */
	public static void main(String[] args) {
		Messages.configure(LanguageSupport.resolve(args));
		System.out.println(Messages.get("app.title"));

		Tasks.menu();
	}
}
