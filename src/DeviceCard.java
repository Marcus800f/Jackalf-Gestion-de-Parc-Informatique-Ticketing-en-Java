import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;

public class DeviceCard {
    private int id;
    private String nom, ip, etat, type, owner;

    public DeviceCard(int id, String nom, String ip, String etat, String type, String owner){
        this.id=id; this.nom=nom; this.ip=ip; this.etat=etat; this.type=type; this.owner=owner;
    }

    public VBox getCard(){
        VBox card = new VBox(5);
        card.getStyleClass().add("card");
        card.setPrefWidth(140);

        //  GESTION DES IMAGES

        // On met tout en minuscule pour comparer facilement
        String typeClean = (type != null) ? type.toLowerCase().trim() : "workstation";
        String imageName;


        if (typeClean.contains("rout")) {
            imageName = "routeur.png";
        } else if (typeClean.contains("switch")) {
            imageName = "switch.jpg";
        } else {
            imageName = "workstation.png";
        }

        // Construction du chemin
        String path = "file:src/style/image/" + imageName;

        ImageView img = new ImageView();
        try {
            img.setImage(new Image(path));
        } catch (Exception e) {
            //débogage
            System.out.println("Image introuvable : " + path);
        }

        img.setFitWidth(50);
        img.setPreserveRatio(true);

        //TEXTES
        Text txtNom = new Text(nom);
        txtNom.setStyle("-fx-font-weight:bold; -fx-fill:#2c3e50;");

        Text txtIp = new Text(ip);
        txtIp.setStyle("-fx-fill:#7f8c8d; -fx-font-size:11px;");

        //STATUT
        Text statut = new Text(etat);
        statut.setStyle("-fx-font-weight:bold; -fx-font-size:11px;");

        String etatClean = (etat != null) ? etat.toUpperCase() : "DOWN";
        switch(etatClean){
            case "UP" -> statut.setFill(Color.GREEN);
            case "DOWN" -> statut.setFill(Color.RED);
            case "MAINTENANCE" -> statut.setFill(Color.ORANGE);
            default -> statut.setFill(Color.GRAY);
        }

        card.getChildren().addAll(img, txtNom, txtIp, statut);
        return card;
    }
}