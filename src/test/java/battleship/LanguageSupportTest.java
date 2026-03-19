package battleship;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class LanguageSupportTest {

	@Test
	void defaultsToPortugueseWhenNoLanguageIsProvided() {
		assertEquals(AppLanguage.PORTUGUESE, LanguageSupport.resolve(new String[0], null));
	}

	@Test
	void usesEnvironmentLanguageWhenNoCliFlagIsPresent() {
		assertEquals(AppLanguage.ENGLISH, LanguageSupport.resolve(new String[0], "en"));
	}

	@Test
	void cliFlagOverridesEnvironmentLanguage() {
		assertEquals(AppLanguage.ENGLISH, LanguageSupport.resolve(new String[] { "--lang", "en" }, "pt"));
	}

	@Test
	void supportsInlineCliFlagSyntax() {
		assertEquals(AppLanguage.ENGLISH, LanguageSupport.resolve(new String[] { "--lang=en" }, null));
	}
}
