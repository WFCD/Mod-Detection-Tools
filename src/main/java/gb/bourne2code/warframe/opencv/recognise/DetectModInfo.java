package gb.bourne2code.warframe.opencv.recognise;

import me.xdrop.fuzzywuzzy.FuzzySearch;
import me.xdrop.fuzzywuzzy.model.ExtractedResult;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import net.sourceforge.tess4j.util.LoadLibs;
import org.apache.commons.io.IOUtils;
import org.bytedeco.javacpp.opencv_core;
import org.bytedeco.javacpp.opencv_imgproc;
import org.bytedeco.javacv.Java2DFrameConverter;
import org.bytedeco.javacv.OpenCVFrameConverter;
import org.json.JSONArray;
import org.json.JSONObject;
import org.opencv.core.CvType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class DetectModInfo {
    private final Tesseract tesseract;
    private static final List<String> modNames = new ArrayList<>();
    private static final Logger logger = LoggerFactory.getLogger(DetectModInfo.class);

    public DetectModInfo() {
        //set some tesseract options
        File tmpFolder = LoadLibs.extractTessResources("win32-x86-64");
        System.setProperty("java.library.path", tmpFolder.getPath());
        logger.info(tmpFolder.getAbsolutePath());
        tesseract = new Tesseract();
        tesseract.setLanguage("eng");

        Path dataDirectory;
        try {
            dataDirectory = Paths.get(ClassLoader.getSystemResource("tesseract").toURI());
        } catch (URISyntaxException e) {
            return;
        }
        tesseract.setDatapath(dataDirectory.toString());

        //make list of json mod names
        InputStream is = ClassLoader.getSystemResourceAsStream("tesseract/Mods.json");
        //should never be null, but i dont like stupid ide warnings
        assert is != null;
        try {
            //read json file
            String jsonText = IOUtils.toString(is, StandardCharsets.UTF_8);
            JSONArray json = new JSONArray(jsonText);
            //convert list of mod items to mod names
            json.forEach(o -> modNames.add(((JSONObject) o).getString("name")));
            logger.info("Mod names: {}", modNames);
        } catch (IOException ignored) { /*should never happen **/ }
    }

    public void detectModName(opencv_core.Mat mat) throws TesseractException {
        //convert Matrix to BufferedImage for Tesseract processing
        OpenCVFrameConverter.ToMat converterToMat = new OpenCVFrameConverter.ToMat();
        Java2DFrameConverter converterToImage = new Java2DFrameConverter();

        //cut image for only text
        mat = mat.apply(new opencv_core.Range(40, mat.rows()-35), new opencv_core.Range(20, mat.cols()-10));

        //make grayscale
        opencv_core.Mat gray = new opencv_core.Mat(mat.size(), CvType.CV_8U);
        opencv_imgproc.cvtColor(mat, gray, opencv_imgproc.COLOR_BGR2GRAY, 1);

        //do some magic image processing
        opencv_core.Mat newMat = new opencv_core.Mat(mat.size(), CvType.CV_8U);
        opencv_imgproc.threshold(gray, newMat, 170, 255, opencv_imgproc.THRESH_BINARY_INV);

        //convert to BufferedImage
        BufferedImage image = converterToImage.convert(converterToMat.convert(newMat));

        //get mod name
        String ocr = tesseract.doOCR(image);
        //match result from ocr to nearest mod name, ocr tends to add stuff like tm
        //because of little white stripes in the background of the image which the processing picks up as text
        ExtractedResult modName = FuzzySearch.extractOne(ocr, modNames);
        logger.info("OCR: {}, {}", modName.getString(), modName.getScore());

        //just for testing so i can see the output image
        File outputfile = new File("image.jpg");
        try {
            ImageIO.write(image, "jpg", outputfile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}