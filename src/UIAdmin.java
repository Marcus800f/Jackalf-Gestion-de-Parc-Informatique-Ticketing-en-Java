import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.shape.SVGPath;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import java.sql.*;

public class UIAdmin {

    private FlowPane affichage;
    private Stage mainStage;
    private Utilisateur adminConnecte;

    private HBox filterContainer;
    private HBox dashboardSummaryBox;

    private ComboBox<String> statutFilter;
    private ComboBox<String> severiteFilter;
    private ComboBox<String> typeDeviceFilter;
    private ComboBox<String> etatDeviceFilter;

    public void start(Stage stage, Utilisateur admin) {
        this.mainStage = stage;
        this.adminConnecte = admin;

        Button btnAppareils = new Button("Infrastructure");
        btnAppareils.getStyleClass().add("nav-button");
        btnAppareils.setOnAction(e -> showDevicesInterface());

        Button btnSignalements = new Button("Incidents & Alertes");
        btnSignalements.getStyleClass().add("nav-button");
        btnSignalements.setOnAction(e -> showSignalementsInterface());

        Button btnUsers = new Button("Utilisateurs");
        btnUsers.getStyleClass().add("nav-button");
        btnUsers.setOnAction(e -> UserPopup.open());

        HBox navBar = new HBox(20, btnAppareils, btnSignalements, btnUsers);
        navBar.getStyleClass().add("nav-bar");
        navBar.setAlignment(Pos.CENTER_LEFT);

        dashboardSummaryBox = new HBox(20);
        dashboardSummaryBox.setPadding(new Insets(25, 30, 0, 30));
        dashboardSummaryBox.setAlignment(Pos.CENTER);

        filterContainer = new HBox(15);
        filterContainer.setPadding(new Insets(20, 30, 20, 30));
        filterContainer.setAlignment(Pos.CENTER_LEFT);

        affichage = new FlowPane(20, 20);
        affichage.setPadding(new Insets(10, 30, 30, 30));
        affichage.setPrefWrapLength(1200);
        affichage.setAlignment(Pos.TOP_CENTER);

        ScrollPane scroll = new ScrollPane(affichage);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background: transparent; -fx-background-color:transparent;");
        VBox.setVgrow(scroll, Priority.ALWAYS);

        VBox root = new VBox(navBar, dashboardSummaryBox, filterContainer, scroll);

        Scene scene = new Scene(root, 1200, 900);
        try { scene.getStylesheets().add(getClass().getResource("/style/style.css").toExternalForm()); }
        catch (Exception e) {}

        stage.setScene(scene);
        stage.setTitle("Admin Dashboard - " + (admin != null ? admin.getNom() : "Inconnu"));
        stage.show();

        showDevicesInterface();
    }

    private void showDevicesInterface() {
        dashboardSummaryBox.setVisible(true);
        dashboardSummaryBox.setManaged(true);
        refreshDeviceTiles();

        filterContainer.getChildren().clear();

        Label titleView = new Label("Parc Informatique");
        titleView.getStyleClass().add("section-title");

        typeDeviceFilter = new ComboBox<>();
        typeDeviceFilter.getItems().addAll("Routeur", "Switch", "Workstation");
        typeDeviceFilter.setPromptText("Type");

        etatDeviceFilter = new ComboBox<>();
        etatDeviceFilter.getItems().addAll("UP", "DOWN", "MAINTENANCE");
        etatDeviceFilter.setPromptText("État");

        Button filter = new Button("Filtrer");
        filter.getStyleClass().add("btn-action");
        filter.setOnAction(e -> refreshDevices(typeDeviceFilter.getValue(), etatDeviceFilter.getValue()));

        Button addDevice = new Button("+ Ajouter Appareil");
        addDevice.getStyleClass().add("btn-success");
        addDevice.setOnAction(e -> DevicePopup.addDevice(mainStage, () -> {
            refreshDeviceTiles();
            refreshDevices(typeDeviceFilter.getValue(), etatDeviceFilter.getValue());
        }));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        filterContainer.getChildren().addAll(titleView, spacer, typeDeviceFilter, etatDeviceFilter, filter, addDevice);
        refreshDevices(null, null);
    }

    private void refreshDeviceTiles() {
        dashboardSummaryBox.getChildren().clear();
        dashboardSummaryBox.getChildren().addAll(
                createSummaryTile("Total Équipements", getCount("SELECT COUNT(*) FROM appareils"), "#1e293b"),
                createSummaryTile("Routeurs", getCount("SELECT COUNT(*) FROM appareils WHERE type='Routeur'"), "#0f172a"),
                createSummaryTile("Switchs", getCount("SELECT COUNT(*) FROM appareils WHERE type='Switch'"), "#064e3b"),
                createSummaryTile("Postes de travail", getCount("SELECT COUNT(*) FROM appareils WHERE type='Workstation'"), "#374151")
        );
    }

    private void refreshDevices(String type, String etat){
        affichage.getChildren().clear();
        try(Connection c = BDManager.getConnection()) {
            String sql = "SELECT a.id, a.nom, a.ip, a.type, a.etat, u.nom AS owner " +
                    "FROM appareils a " +
                    "LEFT JOIN utilisateurs u ON a.id_user = u.id " +
                    "WHERE 1=1";

            if(type != null) sql += " AND a.type=?";
            if(etat != null) sql += " AND a.etat=?";

            PreparedStatement ps = c.prepareStatement(sql);
            int i=1;
            if(type != null) ps.setString(i++, type);
            if(etat != null) ps.setString(i++, etat);

            ResultSet rs = ps.executeQuery();
            while(rs.next()){
                String dOwner = rs.getString("owner");
                int dId = rs.getInt("id");
                String dNom = rs.getString("nom");
                String dIp = rs.getString("ip");
                String dEtat = rs.getString("etat");
                String dType = rs.getString("type");

                VBox card = createDeviceCard(dId, dNom, dIp, dEtat, dType, dOwner);

                card.setOnMouseClicked(e -> DevicePopup.modifyDevice(dId, dNom, dIp, dEtat, dType, dOwner, () -> {
                    refreshDeviceTiles();
                    refreshDevices(typeDeviceFilter.getValue(), etatDeviceFilter.getValue());
                }));

                affichage.getChildren().add(card);
            }
        } catch(Exception ex){ ex.printStackTrace(); }
    }

    private VBox createDeviceCard(int id, String nom, String ip, String etat, String type, String owner) {
        VBox card = new VBox(10);
        card.getStyleClass().add("card-modern");
        card.setPrefWidth(200);

        SVGPath icon = new SVGPath();
        String color = "#94a3b8";
        if("Routeur".equalsIgnoreCase(type)) {
            icon.setContent("M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zM4 12c0-.61.08-1.21.21-1.78L8.99 15v1c0 1.1.9 2 2 2v1.93C7.06 19.43 4 16.07 4 12zm13.89 5.4c-.26-.81-1-1.4-1.9-1.4h-1v-3c0-.55-.45-1-1-1h-6v-2h2c.55 0 1-.45 1-1V7h2c1.1 0 2-.9 2-2v-.41C17.92 5.77 20 8.65 20 12c0 2.08-.81 3.98-2.11 5.4z");
            color = "#3b82f6";
        } else if ("Switch".equalsIgnoreCase(type)) {
            icon.setContent("M19 13h-6v6h-2v-6H5v-2h6V5h2v6h6v2z");
            color = "#10b981";
        } else {
            icon.setContent("M20 18c1.1 0 1.99-.9 1.99-2L22 6c0-1.1-.9-2-2-2H4c-1.1 0-2 .9-2 2v10c0 1.1.9 2 2 2H0v2h24v-2h-4zM4 6h16v10H4V6z");
        }
        icon.setFill(Color.web(color)); icon.setScaleX(1.5); icon.setScaleY(1.5);

        HBox top = new HBox(icon); top.setAlignment(Pos.CENTER); top.setPadding(new Insets(10));

        Label lblNom = new Label(nom); lblNom.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 13px;");
        Label lblIp = new Label(ip); lblIp.setStyle("-fx-text-fill: #64748b; -fx-font-size: 11px;");

        Label lblOwner = new Label(owner != null ? "Utilisateur : " + owner : "Non attribué");
        lblOwner.setStyle("-fx-text-fill: " + (owner != null ? "#38bdf8" : "#475569") + "; -fx-font-size: 10px; -fx-font-style: italic;");

        Label lblEtat = new Label(etat); lblEtat.getStyleClass().add("badge-pill");
        if("UP".equalsIgnoreCase(etat)) lblEtat.setStyle("-fx-background-color: #064e3b; -fx-text-fill: #34d399;");
        else if("DOWN".equalsIgnoreCase(etat)) lblEtat.setStyle("-fx-background-color: #450a0a; -fx-text-fill: #f87171;");
        else lblEtat.setStyle("-fx-background-color: #451a03; -fx-text-fill: #fbbf24;");

        HBox bot = new HBox(lblEtat); bot.setAlignment(Pos.CENTER_RIGHT);

        card.getChildren().addAll(top, lblNom, lblIp, lblOwner, new Separator(), bot);
        return card;
    }

    private void showSignalementsInterface() {
        dashboardSummaryBox.setVisible(true);
        dashboardSummaryBox.setManaged(true);
        refreshSignalementTiles();

        filterContainer.getChildren().clear();

        Label title = new Label("Alertes de Sécurité");
        title.getStyleClass().add("section-title");

        statutFilter = new ComboBox<>();
        statutFilter.getItems().addAll("OUVERT", "EN_COURS", "RESOLU");
        statutFilter.setPromptText("Statut");

        severiteFilter = new ComboBox<>();
        severiteFilter.getItems().addAll("CRITIQUE", "HAUTE", "MOYENNE", "BASSE");
        severiteFilter.setPromptText("Sévérité");

        Button btnFilter = new Button("Appliquer");
        btnFilter.getStyleClass().add("btn-action");
        btnFilter.setOnAction(e -> {
            refreshSignalementTiles();
            refreshSignalements(statutFilter.getValue(), severiteFilter.getValue());
        });

        Button btnReset = new Button("Réinitialiser");
        btnReset.setOnAction(e -> {
            statutFilter.setValue(null);
            severiteFilter.setValue(null);
            refreshSignalementTiles();
            refreshSignalements(null, null);
        });

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        filterContainer.getChildren().addAll(title, spacer, statutFilter, severiteFilter, btnFilter, btnReset);
        refreshSignalements(null, null);
    }

    private void refreshSignalementTiles() {
        dashboardSummaryBox.getChildren().clear();
        dashboardSummaryBox.getChildren().addAll(
                createSummaryTile("CRITIQUES (Actifs)", getCount("SELECT COUNT(*) FROM signalements WHERE severite = 'CRITIQUE' AND statut != 'RESOLU'"), "#7f1d1d"),
                createSummaryTile("Non Résolus", getCount("SELECT COUNT(*) FROM signalements WHERE statut != 'RESOLU'"), "#78350f"),
                createSummaryTile("Tickets Fermés", getCount("SELECT COUNT(*) FROM signalements WHERE statut = 'RESOLU'"), "#064e3b")
        );
    }

    private void refreshSignalements(String statutVal, String severiteVal) {
        affichage.getChildren().clear();
        try (Connection c = BDManager.getConnection()) {
            StringBuilder sql = new StringBuilder(
                    "SELECT s.id, s.sujet, s.description, s.statut, s.date_signalement, s.severite, a.nom AS appareil, u.nom AS user " +
                            "FROM signalements s " +
                            "JOIN appareils a ON s.id_appareil = a.id " +
                            "JOIN utilisateurs u ON s.id_user = u.id " +
                            "WHERE 1=1 ");
            if (statutVal != null) sql.append("AND s.statut = ? ");
            if (severiteVal != null) sql.append("AND s.severite = ? ");
            sql.append("ORDER BY CASE WHEN s.statut != 'RESOLU' THEN 1 ELSE 2 END, s.date_signalement DESC");

            PreparedStatement ps = c.prepareStatement(sql.toString());
            int i = 1;
            if (statutVal != null) ps.setString(i++, statutVal);
            if (severiteVal != null) ps.setString(i++, severiteVal);

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                VBox card = createSignalementCard(
                        rs.getInt("id"), rs.getString("sujet"), rs.getString("user"),
                        rs.getString("appareil"), rs.getString("description"),
                        rs.getString("statut"), rs.getString("severite"),
                        rs.getString("date_signalement")
                );
                affichage.getChildren().add(card);
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    private int getCount(String sql) {
        try (Connection c = BDManager.getConnection(); Statement st = c.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            if (rs.next()) return rs.getInt(1);
        } catch (Exception e) { e.printStackTrace(); }
        return 0;
    }

    private VBox createSummaryTile(String title, int count, String colorHex) {
        VBox tile = new VBox(5);
        tile.getStyleClass().add("dashboard-tile");
        tile.setStyle("-fx-background-color: " + colorHex + ";");
        Text txtCount = new Text(String.valueOf(count));
        txtCount.getStyleClass().add("tile-count");
        Label lblTitle = new Label(title);
        lblTitle.getStyleClass().add("tile-label");
        tile.getChildren().addAll(txtCount, lblTitle);
        tile.setPrefWidth(240);
        HBox.setHgrow(tile, Priority.ALWAYS);
        return tile;
    }

    private VBox createSignalementCard(int id, String sujet, String user, String device, String desc, String statut, String severite, String date) {
        VBox card = new VBox(10);
        card.getStyleClass().add("card-modern");
        card.setPrefWidth(1000);

        HBox topRow = new HBox(10);
        topRow.setAlignment(Pos.CENTER_LEFT);

        Label lblStatut = new Label(statut);
        lblStatut.getStyleClass().add("badge-pill");
        if("RESOLU".equals(statut)) lblStatut.setStyle("-fx-background-color: #10b981;");
        else if("EN_COURS".equals(statut)) lblStatut.setStyle("-fx-background-color: #3b82f6;");
        else lblStatut.setStyle("-fx-background-color: #8b5cf6;");

        String safeSev = (severite == null) ? "MOYENNE" : severite;
        Label lblSev = new Label(safeSev);
        lblSev.getStyleClass().add("badge-pill");
        if("CRITIQUE".equals(safeSev)) lblSev.setStyle("-fx-background-color: #ef4444;");
        else if("HAUTE".equals(safeSev)) lblSev.setStyle("-fx-background-color: #f97316;");
        else lblSev.setStyle("-fx-background-color: #eab308; -fx-text-fill: black;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button btnAction = new Button("DÉTAILS");
        btnAction.getStyleClass().add("btn-outline");
        btnAction.setOnAction(e -> SignalementAdminPopup.open(id, statut, desc, adminConnecte.getId(), () -> {
            refreshSignalementTiles();
            refreshSignalements(statutFilter.getValue(), severiteFilter.getValue());
        }));

        topRow.getChildren().addAll(lblStatut, lblSev, spacer, btnAction);

        Text title = new Text(sujet);
        title.getStyleClass().add("text-title");
        title.setStyle("-fx-fill: #f8fafc; -fx-font-family: 'Consolas', monospace; -fx-font-size: 15px;");

        HBox metaRow = new HBox(15);
        metaRow.setAlignment(Pos.CENTER_LEFT);

        SVGPath iconUser = new SVGPath();
        iconUser.setContent("M12 12c2.21 0 4-1.79 4-4s-1.79-4-4-4-4 1.79-4 4 1.79 4 4 4zm0 2c-2.67 0-8 1.34-8 4v2h16v-2c0-2.66-5.33-4-8-4z");
        iconUser.setFill(Color.web("#64748b")); iconUser.setScaleX(0.7); iconUser.setScaleY(0.7);

        Label lblUser = new Label(user);
        lblUser.getStyleClass().add("text-meta");

        Label lblDate = new Label(date.length() > 16 ? date.substring(0, 16) : date);
        lblDate.getStyleClass().add("text-meta");

        Label lblTarget = new Label("CIBLE : " + device);
        lblTarget.setStyle("-fx-text-fill: #38bdf8; -fx-font-weight: bold; -fx-font-size: 11px;");

        metaRow.getChildren().addAll(
                new HBox(5, iconUser, lblUser),
                new Label("|"),
                lblDate,
                new Label("|"),
                lblTarget
        );

        VBox contextBox = new VBox(5);
        contextBox.setStyle("-fx-background-color: #0f172a; -fx-padding: 8; -fx-background-radius: 4;");
        Text body = new Text(desc.length() > 120 ? desc.substring(0, 120) + "..." : desc);
        body.setStyle("-fx-fill: #94a3b8; -fx-font-size: 12px;");
        contextBox.getChildren().add(body);

        card.getChildren().addAll(topRow, title, metaRow, contextBox);
        return card;
    }
}