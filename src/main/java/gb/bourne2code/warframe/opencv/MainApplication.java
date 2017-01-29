package gb.bourne2code.warframe.opencv;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.io.IOException;

/** Main application class for the login demo application */
public class MainApplication extends Application {
    public static void main(String[] args) { launch(args); }

    private static Stage mainStage;

    public static Stage getMainStage() {
        return mainStage;
    }

    public static void setMainStage(Stage mainStage) {
        MainApplication.mainStage = mainStage;
    }

    public static void setMaximised(boolean maximised) {
        mainStage.setMaximized(maximised);
    }

    @Override public void start(Stage stage) throws IOException {
        MainApplication.setMainStage(stage);
        Scene scene = new Scene(new StackPane());

        Manager manager = new Manager(scene);
        manager.showLoginScreen();

        stage.setScene(scene);
        stage.show();
    }

    public static void setWidth(double width) {
        mainStage.setWidth(width);
    }

    public static void setHeight(double height) {
        mainStage.setHeight(height);
    }
}