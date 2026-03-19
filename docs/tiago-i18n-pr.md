**Pull Request: Feature - Internationalized CLI Messages**

This pull request introduces internationalization for the main user-facing CLI messages, keeping Portuguese as the default language and adding English as an optional mode.

**Changes Implemented:**

1. **ICU4J Dependency:** Added the `com.ibm.icu:icu4j` dependency to `pom.xml`. The first commit in this branch should isolate this change.
2. **Language Resolution:** Added `AppLanguage` and `LanguageSupport` to resolve the active language from `--lang en` or `BATTLESHIP_LANG=en`.
3. **Translated Message Catalog:** Added a `Messages` helper with Portuguese and English resource bundles for the visible CLI text.
4. **Main and Menu Integration:** Updated `Main`, `Tasks`, and the board/game-over output in `Game` to use translated messages.
5. **Unit Testing:** Included deterministic tests for language selection and translated output.
6. **Documentation:** Updated the `README.md` with instructions on how to run the game in English and how to validate the feature locally.

**How to Test:**

1. Compile and package the application: `mvn clean package`.
2. Run the default version: `java -jar target/BattleshipGamePlayer-2.0.jar`.
3. Confirm the menu/help and goodbye text appear in Portuguese.
4. Run the English version: `java -jar target/BattleshipGamePlayer-2.0.jar --lang en`.
5. Confirm the menu/help and goodbye text appear in English.
6. Run the tests to validate the feature: `mvn test`.

Closes: https://github.com/rtz15/Battleship2/issues/<ISSUE_NUMBER>
