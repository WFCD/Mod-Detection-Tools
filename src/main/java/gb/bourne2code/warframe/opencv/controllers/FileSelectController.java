package gb.bourne2code.warframe.opencv.controllers;

import gb.bourne2code.warframe.opencv.Manager;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;

import java.io.File;

/** Controls the login screen */
public class FileSelectController {
        @FXML private TextField fileLocation;
        private Manager manager;

    public void initManager(final Manager manager) {
        this.manager = manager;
    }

    public void browseFiles(ActionEvent actionEvent) {
        FileChooser chooser = new FileChooser();
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Images", "*.png","*.jpg"));
        File chosenFile = chooser.showOpenDialog(fileLocation.getScene().getWindow());
        if (chosenFile != null) {
            fileLocation.setText(chosenFile.getPath());
        }
    }

    public void loadOpenCvScene(ActionEvent actionEvent) {
        manager.showOpenCvScene(fileLocation.getText());
    }
}