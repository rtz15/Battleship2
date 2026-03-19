package battleship;

import java.util.Locale;

import com.ibm.icu.util.ULocale;

enum AppLanguage {
	PORTUGUESE("pt", ULocale.forLanguageTag("pt-PT")),
	ENGLISH("en", ULocale.ENGLISH);

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
		if (code == null || code.isBlank())
			return PORTUGUESE;

		String normalized = code.trim().toLowerCase(Locale.ROOT);
		if (normalized.startsWith(ENGLISH.code))
			return ENGLISH;
		if (normalized.startsWith(PORTUGUESE.code))
			return PORTUGUESE;
		return PORTUGUESE;
	}
}
