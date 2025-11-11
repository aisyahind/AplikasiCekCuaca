import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DatabaseManager {

    private static final String DB_URL = "jdbc:sqlite:favorit.db";

    public static Connection connect() throws SQLException {
        return DriverManager.getConnection(DB_URL);
    }

    public static void createTable() {
        String sql = """
                     CREATE TABLE IF NOT EXISTS favorit (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        kota TEXT UNIQUE NOT NULL
                     );
                     """;

        try (Connection conn = connect();
             Statement stmt = conn.createStatement()) {

            stmt.execute(sql);
            System.out.println("Tabel favorit siap.");

        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }

    public static void tambahFavorit(String kota) {
        String sql = "INSERT OR IGNORE INTO favorit(kota) VALUES (?)";

        try (Connection conn = connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, kota);
            ps.executeUpdate();

        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }

    public static void hapusFavorit(String kota) {
        String sql = "DELETE FROM favorit WHERE kota = ?";

        try (Connection conn = connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, kota);
            ps.executeUpdate();

        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }

    public static List<String> getFavorit() {
        List<String> list = new ArrayList<>();
        String sql = "SELECT kota FROM favorit ORDER BY kota ASC";

        try (Connection conn = connect();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(rs.getString("kota"));
            }

        } catch (Exception e) {
            System.err.println(e.getMessage());
        }

        return list;
    }
}