package gb.bourne2code.warframe.opencv;

import gb.bourne2code.warframe.opencv.controllers.FileSelectController;
import gb.bourne2code.warframe.opencv.controllers.ModDetectionController;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/** Scene Manager, tells the scenes when to display **/
public class Manager {
    private Scene scene;

    public Manager(Scene scene) {
        this.scene = scene;
    }

    public void showLoginScreen() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getClassLoader().getResource("fxml/fileSelect.fxml")
            );
            scene.setRoot((Parent) loader.load());
            FileSelectController controller =
                    loader.getController();
            controller.initManager(this);
        } catch (IOException ex) {
            Logger.getLogger(Manager.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void showOpenCvScene(String fileLocation, String platform) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getClassLoader().getResource("fxml/opencv.fxml")
            );
            scene.setRoot((Parent) loader.load());
            ModDetectionController controller =
                    loader.<ModDetectionController>getController();
            controller.initScreen(this, fileLocation, platform);

        } catch (IOException ex) {
            Logger.getLogger(Manager.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}