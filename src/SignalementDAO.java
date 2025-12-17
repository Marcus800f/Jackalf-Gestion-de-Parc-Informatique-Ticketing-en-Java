import java.sql.*;


public class SignalementDAO {
    public static void ajouterSignalement(int idUser, String description) {
        try (Connection c = BDManager.getConnection()) {
            PreparedStatement ps = c.prepareStatement(
                    "INSERT INTO signalements(id_user, description, statut) VALUES (?, ?, 'OUVERT')"
            );
            ps.setInt(1, idUser);
            ps.setString(2, description);
            ps.executeUpdate();
        } catch (Exception e) { e.printStackTrace(); }
    }


    public static ResultSet getSignalements() {
        try {
            Connection c = BDManager.getConnection();
            return c.createStatement().executeQuery("SELECT * FROM signalements");
        } catch (Exception e) { e.printStackTrace(); }
        return null;
    }
}