package us.warframestat.moddetection;

import us.warframestat.moddetection.controllers.FileSelectController;
import us.warframestat.moddetection.controllers.ModDetectionController;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;

import java.awt.image.BufferedImage;
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

    public void showOpenCvScene(BufferedImage image, String platform) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getClassLoader().getResource("fxml/opencv.fxml")
            );
            scene.setRoot((Parent) loader.load());
            ModDetectionController controller =
                    loader.<ModDetectionController>getController();
            controller.initScreen(this, image, platform);

        } catch (IOException ex) {
            Logger.getLogger(Manager.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}