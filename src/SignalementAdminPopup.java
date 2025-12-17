import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import java.sql.*;

public class SignalementAdminPopup {

    public static void open(int id, String statut, String desc, int idAdmin, Runnable onUpdate) {
        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle("Admin - Ticket #" + id);

        // Titre
        Label title = new Label("TRAITEMENT DU TICKET #" + id);

        title.setStyle("-fx-font-family: 'Segoe UI'; -fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #f8fafc;");

        //  Description utilisation
        Label lblDesc = new Label("Problème signalé :");
        lblDesc.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 12px; -fx-font-weight: bold;");

        TextArea txtDesc = new TextArea(desc);
        txtDesc.setEditable(false);
        txtDesc.setWrapText(true);
        txtDesc.setPrefRowCount(3);
        // Fond sombre pour la description
        txtDesc.setStyle("-fx-control-inner-background: #0f172a; -fx-background-color: #0f172a; -fx-text-fill: #94a3b8; -fx-border-color: #334155; -fx-border-radius: 4;");

        // Statut
        Label lblStatut = new Label("Nouveau Statut :");
        lblStatut.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 12px; -fx-font-weight: bold;");

        ComboBox<String> statutBox = new ComboBox<>();
        statutBox.getItems().addAll("OUVERT", "EN_COURS", "RESOLU");
        statutBox.setValue(statut);
        statutBox.setMaxWidth(Double.MAX_VALUE);
        // Style Dark ComboBox
        statutBox.setStyle("-fx-background-color: #0f172a; -fx-border-color: #334155; -fx-mark-color: white; -fx-text-fill: white;");

        // Rapport
        Label lblRapport = new Label("Rapport d'intervention (Visible par l'utilisateur) :");
        lblRapport.setStyle("-fx-text-fill: #38bdf8; -fx-font-size: 12px; -fx-font-weight: bold;"); // Bleu Cyan

        TextArea txtRapport = new TextArea();
        txtRapport.setPromptText("Décrivez la solution technique apportée...");
        txtRapport.setWrapText(true);
        txtRapport.setPrefRowCount(5);

        txtRapport.setStyle("-fx-control-inner-background: #0f172a; -fx-background-color: #0f172a; -fx-text-fill: #e2e8f0; -fx-border-color: #38bdf8; -fx-border-width: 0 0 0 2;");

        // Charger rapport existant
        try(Connection c=BDManager.getConnection(); PreparedStatement ps=c.prepareStatement("SELECT rapport_admin FROM signalements WHERE id=?")){
            ps.setInt(1, id);
            ResultSet rs=ps.executeQuery();
            if(rs.next() && rs.getString("rapport_admin")!=null) txtRapport.setText(rs.getString("rapport_admin"));
        }catch(Exception e){}

        // Bouton

        // Bouton Sauvegarder
        Button btnSave = new Button("Mettre à jour & M'assigner");
        btnSave.getStyleClass().add("btn-action");
        btnSave.setMaxWidth(Double.MAX_VALUE);

        btnSave.setOnAction(e -> {
            try(Connection c=BDManager.getConnection(); PreparedStatement ps=c.prepareStatement("UPDATE signalements SET statut=?, rapport_admin=?, id_admin=? WHERE id=?")){
                ps.setString(1, statutBox.getValue());
                ps.setString(2, txtRapport.getText());
                ps.setInt(3, idAdmin);
                ps.setInt(4, id);
                ps.executeUpdate();
            }catch(Exception ex){ex.printStackTrace();}
            if(onUpdate!=null) onUpdate.run();
            stage.close();
        });

        // Bouton Supprimer
        Button btnDelete = new Button("Supprimer le ticket");
        btnDelete.setStyle("-fx-background-color: #450a0a; -fx-text-fill: #fca5a5; -fx-border-color: #7f1d1d; -fx-border-radius: 6; -fx-cursor: hand; -fx-font-weight: bold;");
        btnDelete.setMaxWidth(Double.MAX_VALUE);

        btnDelete.setOnAction(e -> {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Voulez-vous vraiment supprimer ce ticket ?", ButtonType.YES, ButtonType.NO);
            if(alert.showAndWait().orElse(ButtonType.NO) == ButtonType.YES) {
                try(Connection c=BDManager.getConnection(); PreparedStatement ps=c.prepareStatement("DELETE FROM signalements WHERE id=?")){
                    ps.setInt(1,id);
                    ps.executeUpdate();
                }catch(Exception ex){ex.printStackTrace();}
                if(onUpdate!=null) onUpdate.run();
                stage.close();
            }
        });

        // Racine
        VBox root = new VBox(12, title, lblDesc, txtDesc, new Separator(), lblStatut, statutBox, lblRapport, txtRapport, new Separator(), btnSave, btnDelete);
        root.setPadding(new Insets(25));


        root.setStyle("-fx-background-color: #111827; -fx-border-color: #1f2937; -fx-border-width: 1;");

        Scene scene = new Scene(root, 450, 680); // Hauteur ajustée
        try{scene.getStylesheets().add(SignalementAdminPopup.class.getResource("/style/style.css").toExternalForm());}catch(Exception e){}

        stage.setScene(scene);
        stage.show();
    }
}