**User Story**

As a Battleship player, I want the main game messages to support Portuguese and English so that I can use the CLI in the language I prefer.

**Acceptance Criteria**

1. Portuguese remains the default language.
2. English can be selected with `--lang en`.
3. English can also be selected with the `BATTLESHIP_LANG=en` environment variable.
4. At least the startup title, menu/help text, board legend, goodbye message, and game-over message are translated.
5. The main application continues to run normally in both language modes.
6. Deterministic tests validate language selection and translated CLI output.

**Label**

`type: ENHANCEMENT`
