package gb.bourne2code.warframe.opencv.recognise;

import gb.bourne2code.warframe.opencv.exceptions.BaseRuntimeException;
import org.bytedeco.javacpp.opencv_core;
import org.bytedeco.javacpp.opencv_objdetect;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.bytedeco.javacpp.opencv_imgcodecs.imread;
import static org.bytedeco.javacpp.opencv_imgcodecs.imwrite;
import static org.bytedeco.javacpp.opencv_imgproc.rectangle;

/**
 * Created by BourneID on 22/01/2017.
 */
public class DetectMods {
    private String modImagePath;
    private Path tempDirPath;

    //Create the Detect Mods engine. If the image needs to be change, create a new instance of Detect Mods
    public DetectMods(String image) {
        this.modImagePath = image;

        try {
            tempDirPath = Files.createTempDirectory("optv-");
        } catch (IOException e) {
            throw new BaseRuntimeException("Unable to create temp directory", e);
        }
    }

    //Lets do some recognising!
    public String run(String stageFile, double scale, int neighbours) {
        System.out.println("\nRunning Mod Detection");
        //weird bugs are weird -getClassLoader.getPath() prefixes the path with a / - this is obviously a bug with Windows/Java, but this work around works
        String cascadeFilePath = new File(DetectMods.class.getClassLoader().getResource("cascade/" + stageFile + ".xml").getFile()).getAbsolutePath();

        //Create a new CascadeClassifier based of the cascades created - Which took over 35 computing days to complete....
        opencv_objdetect.CascadeClassifier modDetector = new opencv_objdetect.CascadeClassifier(cascadeFilePath);

        //We need to create a Mat based on the image as, hopefully, we'll be drawing on it real soon
        opencv_core.Mat image = imread(modImagePath);

        //A list of rectangles we will draw if the detection is successful
        opencv_core.RectVector modDetections = new opencv_core.RectVector();

        //debugging time takes for performance
        long l = System.currentTimeMillis();
        //MAGIC!
        modDetector.detectMultiScale(image, modDetections, scale, neighbours, 0, new opencv_core.Size(230,100), new opencv_core.Size(270,140));
        long l1 = System.currentTimeMillis();

        System.out.println(String.format("Detected %s mods in %s ms", modDetections.size(), l1-l));

        // Draw a bounding box around each mod. And cross your fingers. And toes. And your pet's toes. If they have toes...
        for (int i=0;i<modDetections.size();i++) {
            opencv_core.Rect rect = modDetections.get(i);
            //Rectangle drawing!
            rectangle(image, new opencv_core.Point(rect.x(), rect.y()), new opencv_core.Point(rect.x() + rect.width(), rect.y() + rect.height()), new opencv_core.Scalar(0, 255, 0, 0));
        }

        //save resulting file for display. imwrite doesn't do temporary files, this will have to be a bug report for now
        String fileLocation = tempDirPath.toString() + "workInProgress.png";
        System.out.println(String.format("Writing %s", fileLocation));
        imwrite(fileLocation, image);

        return fileLocation;
    }
}