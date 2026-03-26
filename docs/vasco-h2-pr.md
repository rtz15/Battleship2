**Pull Request: Feature - Game History with H2 Database**

This pull request introduces a new feature that automatically saves a summary of each completed game and allows players to view their game history.

**Changes Implemented:**

1.  **H2 Database Dependency:** Added the `com.h2database:h2` dependency to `pom.xml` to enable embedded database functionality. The first commit in this branch isolates this change.
2.  **GameHistory Class:** Created a new `GameHistory` class to manage all database interactions, including:
    *   Initializing the database and creating the `game_summary` table if it doesn't exist.
    *   Saving a completed game's summary (`saveGame` method).
    *   Retrieving all game summaries (`getHistory` method).
3.  **Game Integration:** Modified the `Game` class to call `gameHistory.saveGame()` within the `over()` method, ensuring that a summary is saved automatically when a game concludes.
4.  **"historico" Command:** Added a new `historico` command to `Tasks.java`. When used, this command fetches and displays the game history from the database.
5.  **Unit Testing:** Included a `GameHistoryTest` to verify that saving and retrieving game summaries works correctly.
6.  **Documentation:** Updated the `README.md` with instructions on how to use the new `historico` command.

**How to Test:**

1.  Compile and run the application: `mvn clean package && java -jar target/BattleshipGamePlayer-2.0.jar`.
2.  Start a new game using the `gerafrota` command.
3.  Play the game to completion using the `simula` command.
4.  Once the game is over, the summary will be saved automatically.
5.  Use the new `historico` command to view the saved game summary.
6.  Run the tests to ensure the database logic is correct: `mvn test`.

Closes: https://github.com/rtz15/Battleship2/issues/<ISSUE_NUMBER>
