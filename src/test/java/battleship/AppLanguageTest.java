package battleship;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

class AppLanguageTest {

	@Test
	@DisplayName("fromCode defaults to Portuguese for null, blank and unknown values")
	void fromCodeDefaultsToPortugueseForNullBlankAndUnknownValues() {
		assertAll(
				() -> assertEquals(AppLanguage.PORTUGUESE, AppLanguage.fromCode(null), "Error: expected null language codes to default to Portuguese."),
				() -> assertEquals(AppLanguage.PORTUGUESE, AppLanguage.fromCode("   "), "Error: expected blank language codes to default to Portuguese."),
				() -> assertEquals(AppLanguage.PORTUGUESE, AppLanguage.fromCode("es"), "Error: expected unknown language codes to fall back to Portuguese.")
		);
	}

	@Test
	@DisplayName("fromCode recognizes English prefixes regardless of case and surrounding whitespace")
	void fromCodeRecognizesEnglishPrefixes() {
		assertAll(
				() -> assertEquals(AppLanguage.ENGLISH, AppLanguage.fromCode("en"), "Error: expected the basic English code to resolve to ENGLISH."),
				() -> assertEquals(AppLanguage.ENGLISH, AppLanguage.fromCode("EN"), "Error: expected upper-case English codes to resolve to ENGLISH."),
				() -> assertEquals(AppLanguage.ENGLISH, AppLanguage.fromCode("  en-US  "), "Error: expected trimmed English locale tags to resolve to ENGLISH.")
		);
	}

	@Test
	@DisplayName("fromCode recognizes Portuguese prefixes regardless of case")
	void fromCodeRecognizesPortuguesePrefixes() {
		assertAll(
				() -> assertEquals(AppLanguage.PORTUGUESE, AppLanguage.fromCode("pt"), "Error: expected the basic Portuguese code to resolve to PORTUGUESE."),
				() -> assertEquals(AppLanguage.PORTUGUESE, AppLanguage.fromCode("PT"), "Error: expected upper-case Portuguese codes to resolve to PORTUGUESE."),
				() -> assertEquals(AppLanguage.PORTUGUESE, AppLanguage.fromCode("pt-BR"), "Error: expected locale-style Portuguese tags to resolve to PORTUGUESE.")
		);
	}

	@Test
	@DisplayName("Locale accessors expose the configured Java and ICU locales")
	void localeAccessorsExposeExpectedLocales() {
		assertAll(
				() -> assertEquals("pt", AppLanguage.PORTUGUESE.toLocale().getLanguage(), "Error: expected the Portuguese Java locale to keep the pt language code."),
				() -> assertEquals("PT", AppLanguage.PORTUGUESE.toLocale().getCountry(), "Error: expected the Portuguese Java locale to keep the PT country code."),
				() -> assertEquals("en", AppLanguage.ENGLISH.toLocale().getLanguage(), "Error: expected the English Java locale to keep the en language code."),
				() -> assertEquals("pt_PT", AppLanguage.PORTUGUESE.toULocale().toString(), "Error: expected the Portuguese ICU locale to keep the pt_PT identifier."),
				() -> assertEquals("en", AppLanguage.ENGLISH.toULocale().toString(), "Error: expected the English ICU locale to keep the en identifier.")
		);
	}
}
