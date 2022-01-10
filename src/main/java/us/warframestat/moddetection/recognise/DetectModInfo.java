package us.warframestat.moddetection.recognise;

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

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class DetectModInfo {
    private final Tesseract tesseract;
    private static final HashMap<String, String> modNames = new HashMap<>();
    private static final HashMap<String, String> primedModNames = new HashMap<>();
    private static final Logger logger = LoggerFactory.getLogger(DetectModInfo.class);

    private static final List<String> rivenNames = Arrays.asList("Ampi", "Manti", "Argi", "Pura", "Geli", "Tori", "Uti", "Tempi", "Crita", "Pleci", "Acri", "Visi", "Vexi", "Igni", "Exi", "Croni", "Conci", "Magna", "Arma", "Sati", "Toxi", "Lexi", "Insi", "Feva", "Locti", "Sci", "Hexa", "Deci", "Zeti", "Hera");

    /**
     * Loads tesseract files and settings
     * Loads the mod names from the json file
     * Loads the weapon names from the json files
     */
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

        //make list of json mod names and weapon names
        InputStream modIS = ClassLoader.getSystemResourceAsStream("tesseract/Mods.json");

        //should never be null, but i dont like stupid ide warnings
        assert modIS != null;

        convertInputStreamToList(modIS).forEach((k, v) -> {
            if (k.startsWith("Primed")) {
                primedModNames.put(k, v);
            } else {
                modNames.put(k, v);
            }
        });

        logger.info("Mod names: {}", modNames);
    }

    /**
     * Gets List from inputstream
     * @return  Map with name, id, and primed
     */
    public static Map<String, String> convertInputStreamToList(InputStream is) {
        HashMap<String, String> map = new HashMap<>();
        try {
            //read inputstream
            String jsonText = IOUtils.toString(is, StandardCharsets.UTF_8);
            JSONArray json = new JSONArray(jsonText);
            //convert list
            json.forEach(o -> {
                JSONObject jsonObject = (JSONObject) o;
                Map.Entry<String, String> modEntry = getModEntry(jsonObject);
                map.put(modEntry.getKey(), modEntry.getValue());
            });
        } catch (IOException ignored) { /*should never happen **/ }
        return map;
    }


    /**
     * Converts json from the mods.json file to a map entry of a mod name and a mod id
     * @param json  json object with mod info
     * @return      key: mod name, value: mod id
     */
    private static Map.Entry<String, String> getModEntry(JSONObject json) {
        String name = json.getString("name");
        String wikiaUrl;
        // use wikia url here because it should contain the same id as warframe market
        if (json.has("wikiaUrl")) {
            wikiaUrl = json.getString("wikiaUrl").replace("https://warframe.fandom.com/wiki/", "").toLowerCase();
        } else {
            wikiaUrl = name.replace(" ", "_").replaceAll("[^a-zA-Z0-9]", " ").toLowerCase();
        }
        return new AbstractMap.SimpleEntry<>(name, wikiaUrl);
    }

    /**
     *  Detects the mod name from the image using Tesseract OCR
     * @param mat   the image to be processed
     * @return      map entry with key and value of the mod name and id
     * @throws TesseractException   if tesseract fails to recognize the image text
     */
    public Map.Entry<String, String> detectModName(opencv_core.Mat mat) throws TesseractException {
        //convert Matrix to BufferedImage for Tesseract processing
        OpenCVFrameConverter.ToMat converterToMat = new OpenCVFrameConverter.ToMat();
        Java2DFrameConverter converterToImage = new Java2DFrameConverter();

        //cut image for only text
        mat = mat.apply(new opencv_core.Range(35, mat.rows()-20), new opencv_core.Range(20, mat.cols()-10));

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
        List<String> fuzzyList = new ArrayList<>(modNames.keySet());

        //check if primed or not, use the first 10 characters in case of tesseract errors
        int primedChance = FuzzySearch.partialRatio(ocr.substring(0, Math.min(ocr.length(), 10)), "Primed");
        if (primedChance > 80) {
            fuzzyList = new ArrayList<>(primedModNames.keySet());
        }

        //match result from ocr to nearest mod name, ocr tends to add stuff like tm
        //because of little white stripes in the background of the image which the processing picks up as text
        ExtractedResult modName = FuzzySearch.extractOne(ocr, fuzzyList);

        if (modName.getScore() < 70) {
            //most likely a riven mod
            logger.info("OCR: {}", "Riven Mod");
            return new AbstractMap.SimpleEntry<>("Riven Mod", "riven_mod");
        }

        logger.info("OCR: {}, Confidence: {}", modName.getString(), modName.getScore());

        return new AbstractMap.SimpleEntry<>(modName.getString(), modNames.get(modName.getString()));
    }

}
