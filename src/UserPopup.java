import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class UserPopup {

    public static void open() {
        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.initStyle(StageStyle.TRANSPARENT); // Fenêtre sans bordure Windows


        Label title = new Label("NOUVEL UTILISATEUR");
        title.setStyle("-fx-font-family: 'Segoe UI'; -fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #38bdf8;");

        Button closeBtn = new Button("X");
        closeBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #94a3b8; -fx-font-weight: bold; -fx-cursor: hand;");
        closeBtn.setOnAction(e -> stage.close());

        HBox header = new HBox(title, new Region(), closeBtn);
        HBox.setHgrow(header.getChildren().get(1), Priority.ALWAYS);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(0, 0, 20, 0));

        //  FORMULAIRE
        VBox form = new VBox(15);

        TextField nomField = new TextField();
        nomField.setPromptText("Identifiant (ex: tech_support)");
        nomField.setStyle("-fx-background-color: #0f172a; -fx-text-fill: white; -fx-border-color: #334155; -fx-border-radius: 4;");

        PasswordField mdpField = new PasswordField();
        mdpField.setPromptText("Mot de passe");
        mdpField.setStyle("-fx-background-color: #0f172a; -fx-text-fill: white; -fx-border-color: #334155; -fx-border-radius: 4;");

        ComboBox<String> roleBox = new ComboBox<>();
        roleBox.getItems().addAll("USER", "ADMIN");
        roleBox.setValue("USER");
        roleBox.setMaxWidth(Double.MAX_VALUE);
        roleBox.setStyle("-fx-background-color: #0f172a; -fx-border-color: #334155; -fx-mark-color: white; -fx-text-fill: white;");


        Label l1 = new Label("Nom d'utilisateur"); l1.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 11px;");
        Label l2 = new Label("Mot de passe provisoire"); l2.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 11px;");
        Label l3 = new Label("Rôle / Permissions"); l3.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 11px;");

        form.getChildren().addAll(l1, nomField, l2, mdpField, l3, roleBox);

        //BOUTON
        Button btnCreate = new Button("CRÉER LE COMPTE");
        btnCreate.getStyleClass().add("btn-action"); // Bleu néon
        btnCreate.setMaxWidth(Double.MAX_VALUE);
        btnCreate.setPrefHeight(40);

        btnCreate.setOnAction(e -> {
            if(nomField.getText().isEmpty() || mdpField.getText().isEmpty()) return;

            // Appel au DAO
            UtilisateurDAO.creerUtilisateur(nomField.getText(), mdpField.getText(), roleBox.getValue());

            stage.close();
        });


        VBox root = new VBox(header, form, new Separator(), btnCreate);
        root.setSpacing(15);
        root.setPadding(new Insets(25));


        root.setStyle("-fx-background-color: #111827; -fx-border-color: #38bdf8; -fx-border-width: 1;");

        Scene scene = new Scene(root, 350, 420);
        scene.setFill(Color.TRANSPARENT);
        try { scene.getStylesheets().add(UserPopup.class.getResource("/style/style.css").toExternalForm()); } catch(Exception e){}

        stage.setScene(scene);
        stage.show();
    }
}