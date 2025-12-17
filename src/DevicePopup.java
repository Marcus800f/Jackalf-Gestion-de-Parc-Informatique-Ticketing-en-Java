import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import java.sql.*;

public class DevicePopup {

    // Petite classe pour gérer l'affichage dans la ComboBox
    private static class UserItem {
        int id;
        String nom;
        public UserItem(int id, String nom) { this.id = id; this.nom = nom; }
        @Override public String toString() { return nom; }
        // IMPORTANT : Pour que la ComboBox reconnaisse l'égalité
        @Override public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            UserItem userItem = (UserItem) o;
            return id == userItem.id;
        }
    }

    // Modif appareil
    public static void modifyDevice(int id, String nomActuel, String ipActuelle, String etatActuel, String typeActuel, String ownerActuel, Runnable onUpdate){
        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle("Configuration Équipement");

        Label title = new Label("Modifier l'équipement");
        title.getStyleClass().add("popup-title");
        title.setMaxWidth(Double.MAX_VALUE);

        GridPane grid = createFormGrid();

        TextField nomField = new TextField(nomActuel);
        TextField ipField = new TextField(ipActuelle);

        ComboBox<String> typeBox = new ComboBox<>();
        typeBox.getItems().addAll("Routeur", "Switch", "Workstation");
        typeBox.setValue(typeActuel);
        typeBox.setMaxWidth(Double.MAX_VALUE);

        ComboBox<String> etatBox = new ComboBox<>();
        etatBox.getItems().addAll("UP", "DOWN", "MAINTENANCE");
        etatBox.setValue(etatActuel);
        etatBox.setMaxWidth(Double.MAX_VALUE);

        // Sélection du propriétaire
        ComboBox<UserItem> userBox = new ComboBox<>();
        userBox.setMaxWidth(Double.MAX_VALUE);

        // On charge la liste ET on sélectionne le bon user
        loadUsers(userBox, ownerActuel);

        addFormRow(grid, 0, "Nom de l'hôte :", nomField);
        addFormRow(grid, 1, "Adresse IP :", ipField);
        addFormRow(grid, 2, "Type matériel :", typeBox);
        addFormRow(grid, 3, "État actuel :", etatBox);
        addFormRow(grid, 4, "Attribué à :", userBox);

        Button btnCancel = new Button("Annuler");
        btnCancel.setOnAction(e -> stage.close());

        Button btnDelete = new Button("Supprimer");
        btnDelete.getStyleClass().add("btn-danger");
        btnDelete.setOnAction(ev -> {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Supprimer définitivement ?", ButtonType.YES, ButtonType.NO);
            if(alert.showAndWait().orElse(ButtonType.NO) == ButtonType.YES) {
                dbExec("DELETE FROM appareils WHERE id=?", id);
                if(onUpdate != null) onUpdate.run();
                stage.close();
            }
        });

        Button btnSave = new Button("Enregistrer");
        btnSave.getStyleClass().add("btn-action");
        btnSave.setOnAction(ev -> {
            // Récupération ID (null si "Aucun")
            Integer idUser = (userBox.getValue() != null && userBox.getValue().id != 0) ? userBox.getValue().id : null;

            dbExec("UPDATE appareils SET nom=?, ip=?, type=?, etat=?, id_user=? WHERE id=?",
                    nomField.getText(), ipField.getText(), typeBox.getValue(), etatBox.getValue(), idUser, id);

            if(onUpdate != null) onUpdate.run();
            stage.close();
        });

        HBox actions = new HBox(10, btnCancel, btnSave);
        actions.setAlignment(Pos.CENTER_RIGHT);

        HBox buttonBar = new HBox(btnDelete, new Region(), actions);
        HBox.setHgrow(buttonBar.getChildren().get(1), Priority.ALWAYS);
        buttonBar.setPadding(new Insets(15, 0, 0, 0));

        VBox root = new VBox(15, title, grid, new Separator(), buttonBar);
        root.getStyleClass().add("popup-root");

        Scene scene = new Scene(root, 480, 480); // Hauteur ajustée
        loadCSS(scene);
        stage.setScene(scene);
        stage.show();
    }

    // Ajout appareil
    public static void addDevice(Stage mainStage, Runnable onUpdate){
        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.initOwner(mainStage);
        stage.setTitle("Nouvel Équipement");

        Label title = new Label("Ajouter un équipement");
        title.getStyleClass().add("popup-title");
        title.setMaxWidth(Double.MAX_VALUE);

        GridPane grid = createFormGrid();

        TextField nomField = new TextField(); nomField.setPromptText("Ex: SW-Core-01");
        TextField ipField = new TextField();  ipField.setPromptText("Ex: 192.168.1.254");

        ComboBox<String> typeBox = new ComboBox<>();
        typeBox.getItems().addAll("Routeur", "Switch", "Workstation");
        typeBox.setPromptText("Sélectionner...");
        typeBox.setMaxWidth(Double.MAX_VALUE);

        ComboBox<String> etatBox = new ComboBox<>();
        etatBox.getItems().addAll("UP", "DOWN", "MAINTENANCE");
        etatBox.setValue("UP");
        etatBox.setMaxWidth(Double.MAX_VALUE);

        ComboBox<UserItem> userBox = new ComboBox<>();
        userBox.setPromptText("Attribuer à...");
        userBox.setMaxWidth(Double.MAX_VALUE);
        loadUsers(userBox, null); // Chargement sans sélection

        addFormRow(grid, 0, "Nom de l'hôte :", nomField);
        addFormRow(grid, 1, "Adresse IP :", ipField);
        addFormRow(grid, 2, "Type matériel :", typeBox);
        addFormRow(grid, 3, "État initial :", etatBox);
        addFormRow(grid, 4, "Attribué à :", userBox);

        Button btnCancel = new Button("Annuler");
        btnCancel.setOnAction(e -> stage.close());

        Button btnAdd = new Button("Créer l'appareil");
        btnAdd.getStyleClass().add("btn-success");
        btnAdd.setOnAction(ev -> {
            if(nomField.getText().isEmpty() || typeBox.getValue() == null) return;

            Integer idUser = (userBox.getValue() != null && userBox.getValue().id != 0) ? userBox.getValue().id : null;

            dbExec("INSERT INTO appareils(nom,ip,type,etat,id_user) VALUES(?,?,?,?,?)",
                    nomField.getText(), ipField.getText(), typeBox.getValue(), etatBox.getValue(), idUser);

            if(onUpdate != null) onUpdate.run();
            stage.close();
        });

        HBox buttonBar = new HBox(10, btnCancel, btnAdd);
        buttonBar.setAlignment(Pos.CENTER_RIGHT);
        buttonBar.setPadding(new Insets(15, 0, 0, 0));

        VBox root = new VBox(15, title, grid, new Separator(), buttonBar);
        root.getStyleClass().add("popup-root");

        Scene scene = new Scene(root, 480, 480);
        loadCSS(scene);
        stage.setScene(scene);
        stage.show();
    }

    // Logique de séléection
    private static void loadUsers(ComboBox<UserItem> box, String currentOwnerName) {
        UserItem defaultOption = new UserItem(0, "Aucun (Non attribué)");
        box.getItems().add(defaultOption);
        box.setValue(defaultOption); // Par défaut

        try (Connection c = BDManager.getConnection();
             Statement st = c.createStatement();
             ResultSet rs = st.executeQuery("SELECT id, nom FROM utilisateurs ORDER BY nom")) {

            while (rs.next()) {
                UserItem item = new UserItem(rs.getInt("id"), rs.getString("nom"));
                box.getItems().add(item);

                // Comparaison plus souple (ignore casse et espaces)
                if (currentOwnerName != null && currentOwnerName.trim().equalsIgnoreCase(item.nom.trim())) {
                    box.setValue(item);
                }
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    private static GridPane createFormGrid() {
        GridPane grid = new GridPane();
        grid.setHgap(15); grid.setVgap(15);
        grid.setPadding(new Insets(10, 0, 10, 0));
        ColumnConstraints col1 = new ColumnConstraints(); col1.setPercentWidth(30);
        ColumnConstraints col2 = new ColumnConstraints(); col2.setPercentWidth(70);
        grid.getColumnConstraints().addAll(col1, col2);
        return grid;
    }

    private static void addFormRow(GridPane grid, int row, String labelText, Control input) {
        Label lbl = new Label(labelText);
        lbl.setStyle("-fx-font-weight:bold; -fx-text-fill:#7f8c8d;");
        GridPane.setHalignment(lbl, javafx.geometry.HPos.RIGHT);
        grid.add(lbl, 0, row);
        grid.add(input, 1, row);
    }

    private static void loadCSS(Scene scene) {
        try { scene.getStylesheets().add(DevicePopup.class.getResource("/style/style.css").toExternalForm()); }
        catch (Exception e) {}
    }

    private static void dbExec(String sql, Object... params) {
        try (Connection c = BDManager.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++) ps.setObject(i + 1, params[i]);
            ps.executeUpdate();
        } catch (Exception e) { e.printStackTrace(); }
    }
}