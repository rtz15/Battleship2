package battleship;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class GameHistory {

    private static final String DB_URL = "jdbc:h2:./battleship_history";

    public GameHistory() {
        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement()) {
            String sql = "CREATE TABLE IF NOT EXISTS game_summary (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY," +
                    "finished_at TIMESTAMP," +
                    "total_moves INT," +
                    "hits INT," +
                    "sunk_ships INT," +
                    "remaining_ships INT," +
                    "result VARCHAR(255)" +
                    ")";
            stmt.executeUpdate(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void saveGame(Timestamp finishedAt, int totalMoves, int hits, int sunkShips, int remainingShips, String result) {
        String sql = "INSERT INTO game_summary (finished_at, total_moves, hits, sunk_ships, remaining_ships, result) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setTimestamp(1, finishedAt);
            pstmt.setInt(2, totalMoves);
            pstmt.setInt(3, hits);
            pstmt.setInt(4, sunkShips);
            pstmt.setInt(5, remainingShips);
            pstmt.setString(6, result);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<String> getHistory() {
        List<String> history = new ArrayList<>();
        String sql = "SELECT * FROM game_summary ORDER BY finished_at DESC";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                String entry = String.format("Game finished at: %s, Moves: %d, Hits: %d, Sunk Ships: %d, Remaining Ships: %d, Result: %s",
                        rs.getTimestamp("finished_at"),
                        rs.getInt("total_moves"),
                        rs.getInt("hits"),
                        rs.getInt("sunk_ships"),
                        rs.getInt("remaining_ships"),
                        rs.getString("result"));
                history.add(entry);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return history;
    }
}
