package battleship;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class GameHistoryTest {

    private static final String DB_FILE = "./battleship_history.mv.db";

    @BeforeEach
    @AfterEach
    void cleanup() {
        try (Connection conn = DriverManager.getConnection("jdbc:h2:./battleship_history");
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("DROP TABLE IF EXISTS game_summary");
        } catch (SQLException e) {
            // Ignore errors during cleanup
        }
        new File(DB_FILE).delete();
    }

    @Test
    void testSaveAndGetHistory() {
        GameHistory gameHistory = new GameHistory();
        Timestamp now = new Timestamp(System.currentTimeMillis());

        gameHistory.saveGame(now, 10, 5, 2, 3, "WIN");
        List<String> history = gameHistory.getHistory();

        assertFalse(history.isEmpty());
        assertEquals(1, history.size());
        assertTrue(history.get(0).contains("WIN"));
    }
}
