public class Utilisateur {
    private int id;
    private String nom;
    private String role;

    public Utilisateur(int id, String nom, String role) {
        this.id = id;
        this.nom = nom;
        this.role = role;
    }

    public int getId() { return id; }
    public String getNom() { return nom; }
    public String getRole() { return role; }
}
