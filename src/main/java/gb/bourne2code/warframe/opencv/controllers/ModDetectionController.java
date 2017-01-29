package gb.bourne2code.warframe.opencv.controllers;

import gb.bourne2code.warframe.opencv.MainApplication;
import gb.bourne2code.warframe.opencv.Manager;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import gb.bourne2code.warframe.opencv.recognise.*;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;

import java.io.File;
import java.text.DecimalFormat;

/** Controls the main application screen */
public class ModDetectionController {
    @FXML
    public ChoiceBox stageChoice;
    @FXML
    public Slider scaleSlider;
    @FXML
    public Label scaleLabel;
    @FXML
    public Slider neighbourSlider;
    @FXML
    public Label neighbourLabel;
    @FXML
    private ImageView imageViewer;

    private Manager manager;
    private DetectMods recogniser;

    public void initScreen(Manager manager, String fileLocation) {
        this.manager = manager;

        setupImage(fileLocation);

        setupDragDrop();

        setupSliders();

        runModFinder();
    }

    //sets the values of the labels as they are being changed
    private void setupSliders() {
        scaleSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            DecimalFormat df = new DecimalFormat("#.000");
            String twoDp = df.format(newValue);
            scaleLabel.setText(twoDp);
        });

        neighbourSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            neighbourLabel.setText(String.valueOf(Math.round((Double) newValue)));
        });

        scaleSlider.valueChangingProperty().addListener((obs, wasChanging, isNowChanging) -> {
            if (! isNowChanging) {
                runModFinder();
            }
        });
        neighbourSlider.valueChangingProperty().addListener((obs, wasChanging, isNowChanging) -> {
            if (! isNowChanging) {
                runModFinder();
            }
        });
    }

    //sets the drag and drop ability to allow users to drag images into the application
    private void setupDragDrop() {
        Scene scene = imageViewer.getScene();

        scene.setOnDragOver(event -> {
            Dragboard db = event.getDragboard();
            if (db.hasFiles() && db.getFiles().size() == 1) {
                event.acceptTransferModes(TransferMode.COPY);
            } else {
                event.consume();
            }
        });

        // Dropping over surface
        scene.setOnDragDropped(event -> {
            Dragboard db = event.getDragboard();
            boolean success = false;
            if (db.hasFiles()) {
                success = true;
                String filePath = null;
                for (File file:db.getFiles()) {
                    filePath = file.getAbsolutePath();
                    setupImage(filePath);
                }
            }
            event.setDropCompleted(success);
            event.consume();
        });
    }

    //setup the image viewer and openCV recogniser
    private void setupImage(String fileLocation) {
        Image image = new Image("file:" + fileLocation);
        imageViewer.setImage(image);
        imageViewer.fitWidthProperty().bind(imageViewer.getScene().widthProperty().subtract(200));
        MainApplication.setHeight(400);
        MainApplication.setWidth(1200);
        stageChoice.getSelectionModel().selectLast();

        recogniser = new DetectMods(fileLocation); //needs image
    }

    private void runModFinder() {
        runModFinder(null);
    }

    // run the image against OpenCV with the adjustable values
    private void runModFinder(ActionEvent actionEvent) {
        String cascadeValue = (String) stageChoice.getValue(); //cascadexx
        double scale = scaleSlider.getValue();
        int neighbours = Math.toIntExact(Math.round(neighbourSlider.getValue()));
        String fileLocation = recogniser.run(cascadeValue, scale, neighbours);
        Image image = new Image("file:" + fileLocation);
        imageViewer.setImage(image);
        imageViewer.fitWidthProperty().bind(imageViewer.getScene().widthProperty().subtract(200));
    }

    public void increaseScale(ActionEvent actionEvent) {
        scaleSlider.setValue(scaleSlider.getValue() + 0.001);
        runModFinder(actionEvent);
    }

    public void decreaseScale(ActionEvent actionEvent) {
        scaleSlider.setValue(scaleSlider.getValue() - 0.001);
        runModFinder(actionEvent);
    }

    public void increaseNeighbour(ActionEvent actionEvent) {
        neighbourSlider.setValue(neighbourSlider.getValue() + 1);
        runModFinder(actionEvent);
    }

    public void decreaseNeighbour(ActionEvent actionEvent) {
        neighbourSlider.setValue(neighbourSlider.getValue() - 1);
        runModFinder(actionEvent);
    }

    public void runModFinderAction(ActionEvent actionEvent) {
        runModFinder();
    }
}