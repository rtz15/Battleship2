package battleship;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class GameHistory {

    private static final Logger LOGGER = LogManager.getLogger(GameHistory.class);
    private static final String DB_URL = "jdbc:h2:./battleship_history";
    private static final String CREATE_TABLE_SQL = "CREATE TABLE IF NOT EXISTS game_summary (" +
            "id INT AUTO_INCREMENT PRIMARY KEY," +
            "finished_at TIMESTAMP," +
            "total_moves INT," +
            "hits INT," +
            "sunk_ships INT," +
            "remaining_ships INT," +
            "result VARCHAR(255)" +
            ")";
    private static final String INSERT_GAME_SQL =
            "INSERT INTO game_summary (finished_at, total_moves, hits, sunk_ships, remaining_ships, result) VALUES (?, ?, ?, ?, ?, ?)";
    private static final String SELECT_HISTORY_SQL = "SELECT * FROM game_summary ORDER BY finished_at DESC";

    public GameHistory() {
        try (Connection conn = openConnection();
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(CREATE_TABLE_SQL);
        } catch (SQLException e) {
            logDatabaseError("Unable to initialize game history database.", e);
        }
    }

    public void saveGame(Timestamp finishedAt, int totalMoves, int hits, int sunkShips, int remainingShips, String result) {
        try (Connection conn = openConnection();
             PreparedStatement pstmt = conn.prepareStatement(INSERT_GAME_SQL)) {
            bindGameSummary(pstmt, finishedAt, totalMoves, hits, sunkShips, remainingShips, result);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            logDatabaseError("Unable to save game history entry.", e);
        }
    }

    public List<String> getHistory() {
        List<String> history = new ArrayList<>();
        try (Connection conn = openConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(SELECT_HISTORY_SQL)) {
            while (rs.next()) {
                history.add(formatHistoryEntry(rs));
            }
        } catch (SQLException e) {
            logDatabaseError("Unable to read game history.", e);
        }
        return history;
    }

    private static Connection openConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL);
    }

    private static void bindGameSummary(PreparedStatement pstmt, Timestamp finishedAt, int totalMoves, int hits,
                                        int sunkShips, int remainingShips, String result) throws SQLException {
        pstmt.setTimestamp(1, finishedAt);
        pstmt.setInt(2, totalMoves);
        pstmt.setInt(3, hits);
        pstmt.setInt(4, sunkShips);
        pstmt.setInt(5, remainingShips);
        pstmt.setString(6, result);
    }

    private static String formatHistoryEntry(ResultSet rs) throws SQLException {
        return String.format("Game finished at: %s, Moves: %d, Hits: %d, Sunk Ships: %d, Remaining Ships: %d, Result: %s",
                rs.getTimestamp("finished_at"),
                rs.getInt("total_moves"),
                rs.getInt("hits"),
                rs.getInt("sunk_ships"),
                rs.getInt("remaining_ships"),
                rs.getString("result"));
    }

    private static void logDatabaseError(String message, SQLException exception) {
        LOGGER.error(message, exception);
    }
}
