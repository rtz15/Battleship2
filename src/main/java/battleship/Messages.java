package battleship;

import java.util.ResourceBundle;

import com.ibm.icu.text.MessageFormat;

final class Messages {
	private static final String BUNDLE_NAME = "battleship.messages";

	private static AppLanguage currentLanguage = AppLanguage.PORTUGUESE;
	private static ResourceBundle bundle = loadBundle(currentLanguage);

	private Messages() {
	}

	public static void configure(AppLanguage language) {
		currentLanguage = language == null ? AppLanguage.PORTUGUESE : language;
		bundle = loadBundle(currentLanguage);
	}

	public static String get(String key) {
		return bundle.getString(key);
	}

	public static String format(String key, Object... args) {
		MessageFormat formatter = new MessageFormat(get(key), currentLanguage.toULocale());
		return formatter.format(args);
	}

	private static ResourceBundle loadBundle(AppLanguage language) {
		return ResourceBundle.getBundle(BUNDLE_NAME, language.toLocale());
	}
}
