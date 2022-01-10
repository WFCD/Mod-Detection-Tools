package us.warframestat.moddetectiongui;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Main application class for the login demo application */
public class MainApplication extends Application {
  private static Stage mainStage;
  private static Logger logger = LoggerFactory.getLogger(MainApplication.class);

  public static void main(String[] args) {
    launch(args);
  }

  public static Stage getMainStage() {
    return mainStage;
  }

  public static void setMainStage(Stage mainStage) {
    MainApplication.mainStage = mainStage;
  }

  public static void setMaximised(boolean maximised) {
    mainStage.setMaximized(maximised);
  }

  @Override
  public void start(Stage stage) {
    logger.info("Starting...");

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
