package us.warframestat.moddetection.gui.recognise;

import static org.bytedeco.javacpp.opencv_imgcodecs.imwrite;
import static org.bytedeco.javacpp.opencv_imgproc.rectangle;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.AbstractMap;
import java.util.Map;
import java.util.Objects;
import javax.imageio.ImageIO;
import net.sourceforge.tess4j.TesseractException;
import org.bytedeco.javacpp.opencv_core;
import org.bytedeco.javacpp.opencv_imgcodecs;
import org.bytedeco.javacpp.opencv_objdetect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import us.warframestat.moddetection.gui.exceptions.BaseRuntimeException;
import us.warframestat.moddetection.gui.utils.WarframeMarketAPI;

/** Created by BourneID on 22/01/2017. */
public class DetectMods {
  private BufferedImage modImage;
  private Path tempDirPath;
  private static Logger logger = LoggerFactory.getLogger(DetectMods.class);

  // Create the Detect Mods engine. If the image needs to be change, create a new instance of Detect
  // Mods
  public DetectMods(BufferedImage image) {
    this.modImage = image;

    try {
      tempDirPath = Files.createTempDirectory("optv-");
      logger.info("Created temp directory at {}", tempDirPath);
    } catch (IOException e) {
      throw new BaseRuntimeException("Unable to create temp directory", e);
    }
  }

  // Lets do some recognising!
  public Map.Entry<String, Integer> run(
      String stageFile, double scale, int neighbours, String platform) throws IOException {
    logger.debug("\nRunning Mod Detection");
    // weird bugs are weird -getClassLoader.getPath() prefixes the path with a / - this is obviously
    // a bug with Windows/Java, but this work around works
    String cascadeFilePath =
        new File(
                Objects.requireNonNull(
                        DetectMods.class
                            .getClassLoader()
                            .getResource("cascade/" + stageFile + ".xml"))
                    .getFile())
            .getAbsolutePath();

    // Create a new CascadeClassifier based of the cascades created - Which took over 35 computing
    // days to complete....
    opencv_objdetect.CascadeClassifier modDetector =
        new opencv_objdetect.CascadeClassifier(cascadeFilePath);

    // We need to create a Mat based on the image as, hopefully, we'll be drawing on it real soon
    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
    ImageIO.write(modImage, "jpg", byteArrayOutputStream);
    byteArrayOutputStream.flush();
    opencv_core.Mat image =
        opencv_imgcodecs.imdecode(
            new opencv_core.Mat(byteArrayOutputStream.toByteArray()),
            opencv_imgcodecs.CV_LOAD_IMAGE_UNCHANGED);

    // A list of rectangles we will draw if the detection is successful
    opencv_core.RectVector modDetections = new opencv_core.RectVector();

    // debugging time takes for performance
    long l = System.currentTimeMillis();
    // MAGIC!
    // todo: detect aspect ratio and change min/max size
    modDetector.detectMultiScale(
        image,
        modDetections,
        scale,
        neighbours,
        0,
        new opencv_core.Size(230, 100),
        new opencv_core.Size(270, 140));
    long l1 = System.currentTimeMillis();

    logger.debug("Detected {} mods in {} ms", modDetections.size(), l1 - l);
    DetectModInfo modInfo = new DetectModInfo();

    opencv_core.Mat rectangleMat = image.clone();
    int totalPrice = 0;
    // Draw a bounding box around each mod. And cross your fingers. And toes. And your pet's toes.
    // If they have toes...
    for (int i = 0; i < modDetections.size(); i++) {
      opencv_core.Rect rect = modDetections.get(i);
      opencv_core.Mat subMatrix = image.apply(rect);
      try {
        Map.Entry<String, String> mod = modInfo.detectModName(subMatrix);
        totalPrice += WarframeMarketAPI.getPrice(mod.getValue(), platform);
      } catch (TesseractException e) {
        e.printStackTrace();
      }
      imwrite(tempDirPath.resolve("mod-" + i + ".png").toString(), subMatrix);
      // Rectangle drawing!
      rectangle(
          rectangleMat,
          new opencv_core.Point(rect.x(), rect.y()),
          new opencv_core.Point(rect.x() + rect.width(), rect.y() + rect.height()),
          new opencv_core.Scalar(0, 255, 0, 0));
    }

    image = rectangleMat;

    // save resulting file for display. imwrite doesn't do temporary files, this will have to be a
    // bug report for now
    String fileLocation = tempDirPath.toString() + "workInProgress.png";
    logger.debug("Writing {}", fileLocation);
    imwrite(fileLocation, image);

    modDetector.close();
    return new AbstractMap.SimpleEntry<>(fileLocation, totalPrice);
  }
}
