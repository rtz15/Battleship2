package battleship;

final class LanguageSupport {
	private static final String LANGUAGE_FLAG = "--lang";
	private static final String LANGUAGE_ENV = "BATTLESHIP_LANG";

	private LanguageSupport() {
	}

	public static AppLanguage resolve(String[] args) {
		return resolve(args, System.getenv(LANGUAGE_ENV));
	}

	static AppLanguage resolve(String[] args, String envLanguage) {
		String cliLanguage = extractCliLanguage(args);
		return AppLanguage.fromCode(cliLanguage != null ? cliLanguage : envLanguage);
	}

	private static String extractCliLanguage(String[] args) {
		if (args == null)
			return null;

		for (int i = 0; i < args.length; i++) {
			String arg = args[i];
			if (LANGUAGE_FLAG.equals(arg) && i + 1 < args.length)
				return args[i + 1];
			if (arg.startsWith(LANGUAGE_FLAG + "="))
				return arg.substring((LANGUAGE_FLAG + "=").length());
		}

		return null;
	}
}
