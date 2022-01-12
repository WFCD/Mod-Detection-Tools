package us.warframestat.moddetection.gui.controllers;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Map;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javax.imageio.ImageIO;
import org.json.JSONObject;
import us.warframestat.moddetection.api.detection.DetectMods;
import us.warframestat.moddetection.gui.MainApplication;
import us.warframestat.moddetection.gui.Manager;

/** Controls the main application screen */
public class ModDetectionController {
  @FXML public ChoiceBox stageChoice;
  @FXML public Slider scaleSlider;
  @FXML public Label scaleLabel;
  @FXML public Slider neighbourSlider;
  @FXML public Label neighbourLabel;
  @FXML public Label totalWorthLabel;
  @FXML private ImageView imageViewer;

  private Manager manager;
  private String platform;
  private BufferedImage image;

  public void initScreen(Manager manager, BufferedImage image, String platform) {
    this.manager = manager;
    this.platform = platform;

    setupImage(image);

    setupDragDrop();

    setupSliders();

    runModFinder();
  }

  // sets the values of the labels as they are being changed
  private void setupSliders() {
    scaleSlider
        .valueProperty()
        .addListener(
            (observable, oldValue, newValue) -> {
              DecimalFormat df = new DecimalFormat("#.000");
              String twoDp = df.format(newValue);
              scaleLabel.setText(twoDp);
            });

    neighbourSlider
        .valueProperty()
        .addListener(
            (observable, oldValue, newValue) -> {
              neighbourLabel.setText(String.valueOf(Math.round((Double) newValue)));
            });

    scaleSlider
        .valueChangingProperty()
        .addListener(
            (obs, wasChanging, isNowChanging) -> {
              if (!isNowChanging) {
                runModFinder();
              }
            });
    neighbourSlider
        .valueChangingProperty()
        .addListener(
            (obs, wasChanging, isNowChanging) -> {
              if (!isNowChanging) {
                runModFinder();
              }
            });
  }

  // sets the drag and drop ability to allow users to drag images into the application
  private void setupDragDrop() {
    Scene scene = imageViewer.getScene();

    scene.setOnDragOver(
        event -> {
          Dragboard db = event.getDragboard();
          if (db.hasFiles() && db.getFiles().size() == 1) {
            event.acceptTransferModes(TransferMode.COPY);
          } else {
            event.consume();
          }
        });

    // Dropping over surface
    scene.setOnDragDropped(
        event -> {
          Dragboard db = event.getDragboard();
          boolean success = false;
          if (db.hasFiles()) {
            success = true;
            for (File file : db.getFiles()) {
              try {
                setupImage(ImageIO.read(file));
              } catch (IOException ignored) {
                /* invalid image */
              }
            }
          }
          event.setDropCompleted(success);
          event.consume();
        });
  }

  // setup the image viewer and openCV recogniser
  private void setupImage(BufferedImage image) {
    imageViewer.setImage(SwingFXUtils.toFXImage(image, null));
    imageViewer.fitWidthProperty().bind(imageViewer.getScene().widthProperty().subtract(200));
    MainApplication.setHeight(400);
    MainApplication.setWidth(1200);
    stageChoice.getSelectionModel().selectLast();

    this.image = image;
  }

  private void runModFinder() {
    try {
      runModFinder(null);
    } catch (IOException ignored) {
      /* invalid image */
    }
  }

  // run the image against OpenCV with the adjustable values
  private void runModFinder(ActionEvent actionEvent) throws IOException {
    String cascadeValue = (String) stageChoice.getValue(); // cascadexx
    double scale = scaleSlider.getValue();
    int neighbours = Math.toIntExact(Math.round(neighbourSlider.getValue()));
    Map.Entry<BufferedImage, JSONObject> results =
        DetectMods.run(image, cascadeValue, scale, neighbours, platform);
    Image imageShown = SwingFXUtils.toFXImage(results.getKey(), null);
    imageViewer.setImage(imageShown);
    imageViewer.fitWidthProperty().bind(imageViewer.getScene().widthProperty().subtract(200));

    totalWorthLabel.setText("Total worth: " + results.getValue().getInt("totalPrice"));
  }

  public void increaseScale(ActionEvent actionEvent) throws IOException {
    scaleSlider.setValue(scaleSlider.getValue() + 0.001);
    runModFinder();
  }

  public void decreaseScale(ActionEvent actionEvent) {
    scaleSlider.setValue(scaleSlider.getValue() - 0.001);
    runModFinder();
  }

  public void increaseNeighbour(ActionEvent actionEvent) {
    neighbourSlider.setValue(neighbourSlider.getValue() + 1);
    runModFinder();
  }

  public void decreaseNeighbour(ActionEvent actionEvent) {
    neighbourSlider.setValue(neighbourSlider.getValue() - 1);
    runModFinder();
  }

  public void runModFinderAction(ActionEvent actionEvent) {
    runModFinder();
  }
}
