import javafx.application.Application;
import javafx.stage.Stage;

// Héritage obligatoire pour transformer la classe en app javafx
public class LauncherApp extends Application {

    @Override
    public void start(Stage stage) {
        new UILogin().start(stage);
        stage.show();
    }
}
