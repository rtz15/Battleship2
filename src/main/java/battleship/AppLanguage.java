package battleship;

import java.util.Locale;

import com.ibm.icu.util.ULocale;

enum AppLanguage {
	PORTUGUESE("pt", ULocale.forLanguageTag("pt-PT")),
	ENGLISH("en", ULocale.ENGLISH);

	private static final AppLanguage DEFAULT_LANGUAGE = PORTUGUESE;

	private final String code;
	private final ULocale locale;

	AppLanguage(String code, ULocale locale) {
		this.code = code;
		this.locale = locale;
	}

	public Locale toLocale() {
		return locale.toLocale();
	}

	public ULocale toULocale() {
		return locale;
	}

	public static AppLanguage fromCode(String code) {
		String normalizedCode = normalize(code);
		if (normalizedCode == null)
			return DEFAULT_LANGUAGE;

		for (AppLanguage language : values()) {
			if (language.matches(normalizedCode))
				return language;
		}

		return DEFAULT_LANGUAGE;
	}

	private static String normalize(String code) {
		if (code == null || code.isBlank())
			return null;
		return code.trim().toLowerCase(Locale.ROOT);
	}

	private boolean matches(String normalizedCode) {
		return normalizedCode.startsWith(code);
	}
}
