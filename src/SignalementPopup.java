import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import java.sql.*;
import java.util.function.Consumer;

public class SignalementPopup {

    public static void modifySignalement(int id, Runnable onUpdate) {
        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle("Détails du Ticket #" + id);

        // Titre header
        Label title = new Label("DÉTAILS DU TICKET #" + id);
        title.setStyle("-fx-font-family: 'Segoe UI'; -fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #f8fafc;");

        // Form
        GridPane grid = new GridPane();
        grid.setHgap(20);
        grid.setVgap(15);
        grid.setPadding(new Insets(20, 0, 20, 0));

        // Champs Dark Style
        TextField sujetField = new TextField();
        sujetField.setStyle("-fx-text-fill: white; -fx-background-color: #0f172a; -fx-border-color: #334155; -fx-border-radius: 4;");

        TextArea descField = new TextArea();
        descField.setWrapText(true);
        descField.setPrefRowCount(3);
        descField.setStyle("-fx-text-fill: white; -fx-control-inner-background: #0f172a; -fx-background-color: #0f172a; -fx-border-color: #334155; -fx-border-radius: 4;");

        ComboBox<String> severiteField = new ComboBox<>();
        severiteField.getItems().addAll("BASSE", "MOYENNE", "HAUTE", "CRITIQUE");
        severiteField.setMaxWidth(Double.MAX_VALUE);
        severiteField.setStyle("-fx-background-color: #0f172a; -fx-border-color: #334155; -fx-mark-color: white; -fx-text-fill: white;");

        Label statutBadge = new Label();
        statutBadge.getStyleClass().add("badge-pill");

        // Zone Rapport Admin
        Label lblReponse = new Label("RÉPONSE DU SUPPORT");
        lblReponse.setStyle("-fx-font-weight: bold; -fx-font-size: 11px; -fx-text-fill: #10b981;");

        TextArea txtRapportAdmin = new TextArea();
        txtRapportAdmin.setEditable(false);
        txtRapportAdmin.setWrapText(true);
        txtRapportAdmin.setPrefRowCount(3);
        txtRapportAdmin.setStyle("-fx-control-inner-background: #0f172a; -fx-background-color: #0f172a; -fx-text-fill: #94a3b8; -fx-border-color: #10b981; -fx-border-width: 0 0 0 2;");

        // Chargement Données
        dbQuery("SELECT sujet, description, statut, severite, rapport_admin FROM signalements WHERE id = ?", rs -> {
            try {
                if (rs.next()) {
                    sujetField.setText(rs.getString("sujet"));
                    descField.setText(rs.getString("description"));

                    String stat = rs.getString("statut");
                    statutBadge.setText(stat);

                    if ("RESOLU".equals(stat)) statutBadge.setStyle("-fx-background-color: #064e3b; -fx-text-fill: #34d399; -fx-padding: 5 12; -fx-background-radius: 15;");
                    else if ("EN_COURS".equals(stat)) statutBadge.setStyle("-fx-background-color: #1e3a8a; -fx-text-fill: #60a5fa; -fx-padding: 5 12; -fx-background-radius: 15;");
                    else if ("OUVERT".equals(stat)) statutBadge.setStyle("-fx-background-color: #7f1d1d; -fx-text-fill: #fca5a5; -fx-padding: 5 12; -fx-background-radius: 15;");
                    else statutBadge.setStyle("-fx-background-color: #4c1d95; -fx-text-fill: #a78bfa; -fx-padding: 5 12; -fx-background-radius: 15;");

                    String s = rs.getString("severite");
                    severiteField.setValue(s != null ? s : "MOYENNE");

                    String rapport = rs.getString("rapport_admin");
                    if (rapport != null && !rapport.isEmpty()) txtRapportAdmin.setText(rapport);
                    else {
                        txtRapportAdmin.setText("Aucune réponse pour le moment.");
                        txtRapportAdmin.setStyle("-fx-control-inner-background: #0f172a; -fx-text-fill: #475569; -fx-font-style: italic; -fx-border-color: #334155; -fx-border-width: 0 0 0 2;");
                    }
                }
            } catch (Exception e) { e.printStackTrace(); }
        }, id);

        // Labels
        Label lStatut = new Label("Statut :"); lStatut.setStyle("-fx-text-fill: #94a3b8;");

        // Sévérité
        Label lUrgence = new Label("Sévérité :"); lUrgence.setStyle("-fx-text-fill: #94a3b8;");

        Label lSujet = new Label("Sujet :"); lSujet.setStyle("-fx-text-fill: #94a3b8;");
        Label lDesc = new Label("Description :"); lDesc.setStyle("-fx-text-fill: #94a3b8;");

        // Placement Grille
        HBox statusBox = new HBox(10, lStatut, statutBadge);
        statusBox.setAlignment(Pos.CENTER_LEFT);
        grid.add(statusBox, 0, 0);

        VBox sevBox = new VBox(5, lUrgence, severiteField);
        grid.add(sevBox, 1, 0);

        VBox sujetBox = new VBox(5, lSujet, sujetField);
        grid.add(sujetBox, 0, 1, 2, 1);

        VBox descBox = new VBox(5, lDesc, descField);
        grid.add(descBox, 0, 2, 2, 1);

        VBox rapportBox = new VBox(5, lblReponse, txtRapportAdmin);
        grid.add(rapportBox, 0, 3, 2, 1);

        ColumnConstraints col1 = new ColumnConstraints(); col1.setPercentWidth(50);
        ColumnConstraints col2 = new ColumnConstraints(); col2.setPercentWidth(50);
        grid.getColumnConstraints().addAll(col1, col2);

        // Bouton
        Button btnCancel = new Button("Fermer");
        btnCancel.setStyle("-fx-background-color: transparent; -fx-text-fill: #94a3b8; -fx-border-color: #475569; -fx-border-radius: 6; -fx-cursor: hand;");
        btnCancel.setOnAction(e -> stage.close());
        btnCancel.setOnMouseEntered(e -> btnCancel.setStyle("-fx-background-color: #1e293b; -fx-text-fill: white; -fx-border-color: #94a3b8; -fx-border-radius: 6; -fx-cursor: hand;"));
        btnCancel.setOnMouseExited(e -> btnCancel.setStyle("-fx-background-color: transparent; -fx-text-fill: #94a3b8; -fx-border-color: #475569; -fx-border-radius: 6; -fx-cursor: hand;"));

        Button btnSave = new Button("Mettre à jour");
        btnSave.getStyleClass().add("btn-action");
        btnSave.setOnAction(e -> {
            dbExec("UPDATE signalements SET sujet=?, description=?, severite=? WHERE id=?",
                    sujetField.getText(), descField.getText(), severiteField.getValue(), id);
            if(onUpdate != null) onUpdate.run();
            stage.close();
        });

        Button btnDelete = new Button("Supprimer");
        btnDelete.setStyle("-fx-background-color: #450a0a; -fx-text-fill: #fca5a5; -fx-border-color: #7f1d1d; -fx-border-radius: 6; -fx-cursor: hand;");
        btnDelete.setOnAction(e -> {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Voulez-vous vraiment supprimer ce ticket ?", ButtonType.YES, ButtonType.NO);
            if(alert.showAndWait().orElse(ButtonType.NO) == ButtonType.YES) {
                dbExec("DELETE FROM signalements WHERE id=?", id);
                if(onUpdate != null) onUpdate.run();
                stage.close();
            }
        });

        HBox actions = new HBox(10, btnCancel, btnSave);
        actions.setAlignment(Pos.CENTER_RIGHT);

        HBox footer = new HBox(btnDelete, new Region(), actions);
        HBox.setHgrow(footer.getChildren().get(1), Priority.ALWAYS);
        footer.setPadding(new Insets(20, 0, 0, 0));

        // Racine
        VBox root = new VBox(title, new Separator(), grid, footer);
        root.setPadding(new Insets(25));
        root.setStyle("-fx-background-color: #111827; -fx-border-color: #1f2937; -fx-border-width: 1;");

        Scene scene = new Scene(root, 550, 650);
        try { scene.getStylesheets().add(SignalementPopup.class.getResource("/style/style.css").toExternalForm()); }
        catch(Exception e) {}

        stage.setScene(scene);
        stage.show();
    }

    public static void dbExec(String sql, Object... params) {
        try (Connection c = BDManager.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++) ps.setObject(i + 1, params[i]);
            ps.executeUpdate();
        } catch (Exception e) { e.printStackTrace(); new Alert(Alert.AlertType.ERROR, "Erreur BD: " + e.getMessage()).show(); }
    }

    public static void dbQuery(String sql, Consumer<ResultSet> consumer, Object... params) {
        try (Connection c = BDManager.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++) ps.setObject(i + 1, params[i]);
            try (ResultSet rs = ps.executeQuery()) { consumer.accept(rs); }
        } catch (Exception e) { e.printStackTrace(); }
    }
}