**User Story**

As a player, I want the game to automatically save a summary of each finished game so that I can review my performance over time.

**Acceptance Criteria**

1.  When a game ends (all ships are sunk), a summary of the game is saved to a persistent database.
2.  The game summary must include:
    *   Timestamp of when the game finished.
    *   Total number of moves (rajadas).
    *   Total number of hits.
    *   Total number of sunk ships.
    *   Number of ships remaining (should be 0 for a win).
    *   Final result (e.g., "WIN").
3.  A new command, `historico`, is available in the main menu.
4.  When the `historico` command is executed, it prints a list of all saved game summaries to the console, ordered from most recent to oldest.
5.  The persistence mechanism uses an embedded H2 database, configured via the `pom.xml`.
6.  The application must continue to run correctly even if the database file does not exist (it should be created automatically).

**Label**

`type: ENHANCEMENT`
