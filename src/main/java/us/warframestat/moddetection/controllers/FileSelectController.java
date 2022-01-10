package us.warframestat.moddetection.controllers;

import us.warframestat.moddetection.Manager;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;

/** Controls the login screen */
public class FileSelectController {
    @FXML public ChoiceBox platformSelect;
    @FXML private TextField fileLocation;
    @FXML private Manager manager;

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

    public void loadOpenCvScene(ActionEvent actionEvent) throws IOException {
        manager.showOpenCvScene(ImageIO.read(new File(fileLocation.getText())), platformSelect.getValue().toString());
    }
}