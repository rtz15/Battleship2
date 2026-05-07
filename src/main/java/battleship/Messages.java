package battleship;

import java.util.ResourceBundle;

import com.ibm.icu.text.MessageFormat;

final class Messages {
	private static final String BUNDLE_NAME = "battleship.messages";
	private static final AppLanguage DEFAULT_LANGUAGE = AppLanguage.PORTUGUESE;

	private static AppLanguage currentLanguage = DEFAULT_LANGUAGE;
	private static ResourceBundle bundle = loadBundle(currentLanguage);

	private Messages() {
	}

	public static void configure(AppLanguage language) {
		currentLanguage = resolveLanguage(language);
		bundle = loadBundle(currentLanguage);
	}

	public static String get(String key) {
		return bundle.getString(key);
	}

	public static String format(String key, Object... args) {
		return createFormatter(key).format(args);
	}

	private static AppLanguage resolveLanguage(AppLanguage language) {
		return language == null ? DEFAULT_LANGUAGE : language;
	}

	private static ResourceBundle loadBundle(AppLanguage language) {
		return ResourceBundle.getBundle(BUNDLE_NAME, language.toLocale());
	}

	private static MessageFormat createFormatter(String key) {
		return new MessageFormat(get(key), currentLanguage.toULocale());
	}
}
