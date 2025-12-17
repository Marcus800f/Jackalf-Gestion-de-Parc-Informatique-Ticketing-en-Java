import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class UILogin extends Application {

    @Override
    public void start(Stage stage) {

        StackPane root = new StackPane();
        root.setStyle("-fx-background-color: #0b101b;");

        VBox loginCard = new VBox(25);
        loginCard.setMaxWidth(420);
        loginCard.setPadding(new Insets(50));
        loginCard.setAlignment(Pos.CENTER_LEFT);

        loginCard.setStyle(
                "-fx-background-color: #151b26;" +
                        "-fx-background-radius: 8px;" +
                        "-fx-border-color: #2d3748; -fx-border-width: 1px; -fx-border-radius: 8px;"
        );

        ImageView logoView = new ImageView();
        try {
            logoView.setImage(new Image("file:src/style/image/logo.png"));
            logoView.setFitWidth(150);
            logoView.setPreserveRatio(true);
        } catch (Exception e) { logoView = null; }

        // TRADUCTION
        Label lblSubtitle = new Label("ACCÈS AUTORISÉ EXCLUSIVEMENT AU PERSONNEL HABILITE");
        lblSubtitle.setStyle("-fx-text-fill: #64748b; -fx-font-size: 11px; -fx-font-weight: bold; -fx-letter-spacing: 1px;");

        VBox header;
        if (logoView != null) {
            header = new VBox(15, logoView, lblSubtitle);
        } else {
            Label fallback = new Label("UNDER JACKAL");
            fallback.setStyle("-fx-font-family: 'Segoe UI'; -fx-font-size: 28px; -fx-font-weight: bold; -fx-text-fill: #f8fafc;");
            header = new VBox(15, fallback, lblSubtitle);
        }
        header.setAlignment(Pos.CENTER);
        header.setPadding(new Insets(0, 0, 40, 0));

        // TRADUCTION
        Label lblUser = new Label("IDENTIFIANT");
        lblUser.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 10px; -fx-font-weight: bold;");

        TextField userField = new TextField();
        userField.setPromptText("nom@domaine.com");
        userField.setPrefHeight(45);
        userField.setStyle("-fx-text-fill: white; -fx-font-size: 14px;");

        // TRADUCTION
        Label lblPass = new Label("MOT DE PASSE");
        lblPass.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 10px; -fx-font-weight: bold;");

        PasswordField passField = new PasswordField();
        passField.setPromptText("••••••••••••");
        passField.setPrefHeight(45);
        passField.setStyle("-fx-text-fill: white;");

        Label errorLabel = new Label("");
        errorLabel.setTextFill(Color.valueOf("#ef4444"));
        errorLabel.setStyle("-fx-font-weight: bold;");
        errorLabel.setVisible(false);

        // TRADUCTION
        Button btnLogin = new Button("SE CONNECTER");
        btnLogin.getStyleClass().add("btn-action");
        btnLogin.setMaxWidth(Double.MAX_VALUE);
        btnLogin.setPrefHeight(50);
        btnLogin.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");

        btnLogin.setOnAction(e -> processLogin(stage, userField, passField, errorLabel));
        userField.setOnAction(e -> processLogin(stage, userField, passField, errorLabel));
        passField.setOnAction(e -> processLogin(stage, userField, passField, errorLabel));

        loginCard.getChildren().addAll(header, lblUser, userField, new Region(), lblPass, passField, new Region(), errorLabel, btnLogin);
        root.getChildren().add(loginCard);

        Scene scene = new Scene(root, 1200, 800);
        try { scene.getStylesheets().add(getClass().getResource("/style/style.css").toExternalForm()); } catch (Exception e) {}

        stage.setScene(scene);
        stage.setTitle("Connexion - Admin Panel");
        stage.show();
    }

    private void processLogin(Stage stage, TextField userField, PasswordField passField, Label errorLabel) {
        String nom = userField.getText();
        String mdp = passField.getText();

        if(nom.isEmpty() || mdp.isEmpty()) {
            errorLabel.setText("CHAMPS REQUIS MANQUANTS");
            errorLabel.setVisible(true);
            return;
        }

        Utilisateur u = UtilisateurDAO.login(nom, mdp);

        if (u != null) {
            errorLabel.setVisible(false);
            if ("ADMIN".equals(u.getRole())) new UIAdmin().start(stage, u);
            else new UIUser(u).start(stage);
        } else {
            errorLabel.setText("ACCÈS REFUSÉ : IDENTIFIANTS INVALIDES");
            errorLabel.setVisible(true);
            userField.clear(); passField.clear();
        }
    }

    public static void main(String[] args) { launch(args); }
}