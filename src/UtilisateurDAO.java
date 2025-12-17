import java.sql.*;

public class UtilisateurDAO {

    // Méthode de Login
    public static Utilisateur login(String nom, String mdpEnClair) {
        // On hache le mot de passe pour comparer avec la BDD
        String mdpHash = SecurityUtils.hashPassword(mdpEnClair);

        try (Connection c = BDManager.getConnection()) {
            PreparedStatement ps = c.prepareStatement("SELECT * FROM utilisateurs WHERE nom = ? AND mdp = ?");
            ps.setString(1, nom);
            ps.setString(2, mdpHash);

            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return new Utilisateur(rs.getInt("id"), rs.getString("nom"), rs.getString("role"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    // Méthode pour Créer un utilisateur
    public static void creerUtilisateur(String nom, String mdpClair, String role) {
        // On hache le mot de passe avant l'insertion
        String hash = SecurityUtils.hashPassword(mdpClair);

        try (Connection c = BDManager.getConnection();
             PreparedStatement ps = c.prepareStatement("INSERT INTO utilisateurs(nom, mdp, role) VALUES(?,?,?)")) {
            ps.setString(1, nom);
            ps.setString(2, hash);
            ps.setString(3, role);
            ps.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}