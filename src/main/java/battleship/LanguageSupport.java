package battleship;

final class LanguageSupport {
	private static final String LANGUAGE_FLAG = "--lang";
	private static final String INLINE_LANGUAGE_FLAG = LANGUAGE_FLAG + "=";
	private static final String LANGUAGE_ENV = "BATTLESHIP_LANG";

	private LanguageSupport() {
	}

	public static AppLanguage resolve(String[] args) {
		return resolve(args, System.getenv(LANGUAGE_ENV));
	}

	static AppLanguage resolve(String[] args, String envLanguage) {
		return AppLanguage.fromCode(resolveLanguageCode(args, envLanguage));
	}

	private static String resolveLanguageCode(String[] args, String envLanguage) {
		String cliLanguage = extractCliLanguage(args);
		return cliLanguage != null ? cliLanguage : envLanguage;
	}

	private static String extractCliLanguage(String[] args) {
		if (args == null)
			return null;

		for (int i = 0; i < args.length; i++) {
			String arg = args[i];
			String separatedValue = extractSeparatedFlagValue(args, i);
			if (separatedValue != null)
				return separatedValue;

			String inlineValue = extractInlineFlagValue(arg);
			if (inlineValue != null)
				return inlineValue;
		}

		return null;
	}

	private static String extractSeparatedFlagValue(String[] args, int index) {
		if (LANGUAGE_FLAG.equals(args[index]) && index + 1 < args.length)
			return args[index + 1];
		return null;
	}

	private static String extractInlineFlagValue(String arg) {
		if (arg.startsWith(INLINE_LANGUAGE_FLAG))
			return arg.substring(INLINE_LANGUAGE_FLAG.length());
		return null;
	}
}
