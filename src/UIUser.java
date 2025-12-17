import javafx.animation.FadeTransition;
import javafx.geometry.*;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.shape.SVGPath;
import javafx.scene.paint.Color;
import javafx.scene.text.*;
import javafx.stage.Stage;
import javafx.util.Duration;
import java.time.format.DateTimeFormatter;

public class UIUser {
    private final Utilisateur user;
    private final FlowPane flow = new FlowPane(20, 20);

    public UIUser(Utilisateur u) { this.user = u; }

    public void start(Stage stage) {

        Label mainTitle = new Label("Tickets");
        mainTitle.getStyleClass().add("section-title");

        Label subTitle = new Label("Gestion des incidents de sécurité");
        subTitle.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 13px;");

        VBox header = new VBox(5, mainTitle, subTitle);
        header.setPadding(new Insets(0, 0, 25, 0));


        VBox formContainer = new VBox(15);
        formContainer.getStyleClass().add("card-modern");

        Label formTitle = new Label("Nouveau Signalement");
        formTitle.setStyle("-fx-font-weight: bold; -fx-font-size: 16px; -fx-text-fill: #f8fafc;");

        GridPane grid = new GridPane();
        grid.setHgap(20); grid.setVgap(15);

        ComboBox<String> appBox = new ComboBox<>();
        SignalementPopup.dbQuery("SELECT nom FROM appareils", rs -> { try { while(rs.next()) appBox.getItems().add(rs.getString("nom")); } catch(Exception e){} });
        appBox.setMaxWidth(Double.MAX_VALUE);
        appBox.setPromptText("Cible...");

        appBox.setStyle("-fx-background-color: #0f172a; -fx-text-fill: white; -fx-border-color: #334155;");

        ComboBox<String> severiteBox = new ComboBox<>();
        severiteBox.getItems().addAll("BASSE", "MOYENNE", "HAUTE", "CRITIQUE");
        severiteBox.setValue("MOYENNE");
        severiteBox.setMaxWidth(Double.MAX_VALUE);

        severiteBox.setStyle("-fx-background-color: #0f172a; -fx-text-fill: white; -fx-border-color: #334155;");

        TextField titre = new TextField();
        titre.setPromptText("Ex: Panne réseau...");

        titre.setStyle("-fx-background-color: #0f172a; -fx-text-fill: white; -fx-prompt-text-fill: #475569; -fx-border-color: #334155;");

        TextArea desc = new TextArea();
        desc.setPromptText("Contexte et détails...");
        desc.setPrefRowCount(2);
        desc.setWrapText(true);

        desc.setStyle("-fx-control-inner-background: #0f172a; -fx-background-color: #0f172a; -fx-text-fill: white; -fx-prompt-text-fill: #475569; -fx-border-color: #334155;");

        // Labels traduits
        Label l1 = new Label("Cible"); l1.setStyle("-fx-text-fill: #94a3b8;");
        Label l2 = new Label("Sévérité"); l2.setStyle("-fx-text-fill: #94a3b8;");
        Label l3 = new Label("Sujet"); l3.setStyle("-fx-text-fill: #94a3b8;");
        Label l4 = new Label("Description"); l4.setStyle("-fx-text-fill: #94a3b8;");

        grid.add(l1, 0, 0); grid.add(appBox, 0, 1);
        grid.add(l2, 1, 0); grid.add(severiteBox, 1, 1);
        grid.add(l3, 0, 2, 2, 1); grid.add(titre, 0, 3, 2, 1);
        grid.add(l4, 0, 4, 2, 1); grid.add(desc, 0, 5, 2, 1);

        ColumnConstraints col1 = new ColumnConstraints(); col1.setPercentWidth(50);
        ColumnConstraints col2 = new ColumnConstraints(); col2.setPercentWidth(50);
        grid.getColumnConstraints().addAll(col1, col2);

        Button btnSend = new Button("Envoyer");
        btnSend.getStyleClass().add("btn-action");
        btnSend.setMaxWidth(Double.MAX_VALUE);
        btnSend.setPrefHeight(40);

        btnSend.setOnAction(e -> {
            if (appBox.getValue() == null || titre.getText().isEmpty()) return;
            SignalementPopup.dbExec("INSERT INTO signalements(id_user,id_appareil,sujet,description,date_signalement,statut,severite) VALUES (?,(SELECT id FROM appareils WHERE nom=?),?,?,NOW(),'EN_ATTENTE',?)",
                    user.getId(), appBox.getValue(), titre.getText(), desc.getText(), severiteBox.getValue());
            titre.clear(); desc.clear(); severiteBox.setValue("MOYENNE"); update();
        });

        formContainer.getChildren().addAll(formTitle, grid, new Separator(), btnSend);

        // Liste
        Label listTitle = new Label("Historique");
        listTitle.getStyleClass().add("section-title");
        listTitle.setPadding(new Insets(30, 0, 10, 0));

        flow.setPrefWrapLength(1000);
        flow.setAlignment(Pos.TOP_CENTER);

        ScrollPane scroll = new ScrollPane(flow);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background: transparent; -fx-background-color:transparent;");
        VBox.setVgrow(scroll, Priority.ALWAYS);

        VBox root = new VBox(header, formContainer, listTitle, scroll);
        root.setPadding(new Insets(30));

        update();
        Scene scene = new Scene(root, 1100, 900);
        try { scene.getStylesheets().add(getClass().getResource("/style/style.css").toExternalForm()); }
        catch (Exception ignored){}
        stage.setScene(scene);
        stage.setTitle("User Dashboard");
        stage.show();
    }

    private void update() {
        flow.getChildren().clear();
        String sql = "SELECT s.id, s.sujet, s.description, s.date_signalement, s.statut, s.severite, a.nom AS device_name, u2.nom AS admin_name " +
                "FROM signalements s " +
                "LEFT JOIN appareils a ON s.id_appareil=a.id " +
                "LEFT JOIN utilisateurs u2 ON s.id_admin = u2.id " +
                "WHERE s.id_user=? ORDER BY s.date_signalement DESC";

        SignalementPopup.dbQuery(sql, rs -> {
            try { while(rs.next()) {
                addCard(rs.getInt("id"), rs.getString("sujet"), rs.getString("device_name"),
                        rs.getString("description"), rs.getString("statut"), rs.getString("severite"),
                        rs.getTimestamp("date_signalement"), rs.getString("admin_name"));
            } } catch(Exception e){}
        }, user.getId());
    }

    // Card
    private void addCard(int id, String t, String app, String txt, String stat, String severite, java.sql.Timestamp date, String adminName) {
        VBox card = new VBox(10);
        card.getStyleClass().add("card-modern");
        card.setPrefWidth(1000);

        // --- LIGNE 1 : Badges + Bouton ---
        HBox topRow = new HBox(10);
        topRow.setAlignment(Pos.CENTER_LEFT);

        Label lblStatut = new Label(stat);
        lblStatut.getStyleClass().add("badge-pill");
        if("RESOLU".equals(stat)) lblStatut.setStyle("-fx-background-color: #10b981;");
        else if("EN_COURS".equals(stat)) lblStatut.setStyle("-fx-background-color: #3b82f6;");
        else lblStatut.setStyle("-fx-background-color: #8b5cf6;");

        String safeSev = (severite == null) ? "MOYENNE" : severite;
        Label lblSev = new Label(safeSev);
        lblSev.getStyleClass().add("badge-pill");
        lblSev.setStyle("-fx-background-color: #f59e0b; -fx-text-fill: black;");

        Region spacerTop = new Region();
        HBox.setHgrow(spacerTop, Priority.ALWAYS);

        Button btnView = new Button("Détails");
        btnView.getStyleClass().add("btn-outline");
        btnView.setOnAction(e -> SignalementPopup.modifySignalement(id, this::update));

        topRow.getChildren().addAll(lblStatut, lblSev, spacerTop, btnView);

        // Titre
        Text title = new Text(t);
        title.getStyleClass().add("text-title");
        title.setStyle("-fx-fill: #f8fafc; -fx-font-size: 16px; -fx-font-family: 'Consolas', monospace;");

        // INFOS
        HBox metaRow = new HBox(15);
        metaRow.setAlignment(Pos.CENTER_LEFT);

        SVGPath iconUser = new SVGPath();
        iconUser.setContent("M12 12c2.21 0 4-1.79 4-4s-1.79-4-4-4-4 1.79-4 4 1.79 4 4 4zm0 2c-2.67 0-8 1.34-8 4v2h16v-2c0-2.66-5.33-4-8-4z");
        iconUser.setFill(Color.web("#64748b")); iconUser.setScaleX(0.7); iconUser.setScaleY(0.7);

        Label lblAdmin = new Label(adminName != null ? adminName : "Non Attribué");
        lblAdmin.getStyleClass().add("text-meta");

        Label lblDate = new Label(date != null ? date.toLocalDateTime().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) : "");
        lblDate.getStyleClass().add("text-meta");

        Label lblDevice = new Label(app);
        lblDevice.getStyleClass().add("text-meta");
        lblDevice.setStyle("-fx-text-fill: #38bdf8;");

        metaRow.getChildren().addAll(
                new HBox(5, iconUser, lblAdmin),
                new Label("|"),
                lblDate,
                new Label("| Cible :"),
                lblDevice
        );

        // contexte
        VBox contextBox = new VBox(5);
        contextBox.setStyle("-fx-background-color: #0f172a; -fx-padding: 10; -fx-background-radius: 4;");

        Label lblContextTitle = new Label("Contexte :");
        lblContextTitle.setStyle("-fx-text-fill: #64748b; -fx-font-size: 10px;");
        Text body = new Text(txt.length() > 100 ? txt.substring(0, 100) + "..." : txt);
        body.setStyle("-fx-fill: #94a3b8; -fx-font-size: 12px;");
        contextBox.getChildren().addAll(lblContextTitle, body);

        card.getChildren().addAll(topRow, title, metaRow, contextBox);

        flow.getChildren().add(card);
        FadeTransition ft = new FadeTransition(Duration.millis(400), card);
        ft.setFromValue(0); ft.setToValue(1); ft.play();
    }
}