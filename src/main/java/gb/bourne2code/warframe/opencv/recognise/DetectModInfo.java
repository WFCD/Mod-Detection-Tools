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

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

public class DetectModInfo {
    private final Tesseract tesseract;
    private static final HashMap<String, String> modNames = new HashMap<>();
    private static final HashMap<String, String> weaponNames = new HashMap<>();
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

        //weapons
        InputStream meleeIS = ClassLoader.getSystemResourceAsStream("tesseract/Melee.json");
        InputStream primaryIS = ClassLoader.getSystemResourceAsStream("tesseract/Primary.json");
        InputStream secondaryIS = ClassLoader.getSystemResourceAsStream("tesseract/Secondary.json");
        InputStream archGunIS = ClassLoader.getSystemResourceAsStream("tesseract/Arch-Gun.json");
        InputStream sentinelWeaponsIS = ClassLoader.getSystemResourceAsStream("tesseract/SentinelWeapons.json");
        //should never be null, but i dont like stupid ide warnings
        assert modIS != null;

        //weapons
        assert meleeIS != null;
        assert primaryIS != null;
        assert secondaryIS != null;
        assert archGunIS != null;
        assert sentinelWeaponsIS != null;

        modNames.putAll(convertInputStreamToList(modIS));

        //weapons
        weaponNames.putAll(convertInputStreamToList(meleeIS));
        weaponNames.putAll(convertInputStreamToList(primaryIS));
        weaponNames.putAll(convertInputStreamToList(secondaryIS));
        weaponNames.putAll(convertInputStreamToList(archGunIS));
        weaponNames.putAll(convertInputStreamToList(sentinelWeaponsIS));

        logger.info("Mod names: {}", modNames);
        logger.info("Weapon names: {}", weaponNames);
    }

    /**
     * Gets List from inputstream
     * @return  Map with name and id
     */
    public static Map<String, String> convertInputStreamToList(InputStream is) {
        HashMap<String, String> map = new HashMap<>();
        try {
            //read inputstream
            String jsonText = IOUtils.toString(is, StandardCharsets.UTF_8);
            JSONArray json = new JSONArray(jsonText);
            //convert list
            json.forEach(o -> map.put(getModEntry((JSONObject) o).getKey(), getModEntry((JSONObject) o).getValue()));
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
        ExtractedResult modName = FuzzySearch.extractOne(ocr, modNames.keySet());

        if (modName.getScore() < 30) {
            //most likely a riven
            //split into weapon name and riven mod name
            List<String> rivenString = Arrays.asList(ocr.split(" "));
            logger.info("Riven detected: {}", rivenString);
            if (rivenString.size() == 2) {
                //riven
                String[] rivenName = rivenString.get(1).split("-");
                List<ExtractedResult> results = new ArrayList<>();

                //weapon
                results.add(FuzzySearch.extractOne(rivenName[0], weaponNames.keySet()));

                //riven
                for (String name : rivenName) {
                    //split by uppercase letter
                    for (String attribute : name.split("(?=\\p{Upper})")) {
                        //match result from ocr to nearest riven name
                        logger.info("riven attribute: {}", attribute);
                        results.add(FuzzySearch.extractOne(attribute, rivenNames));
                    }
                }
                int averageScore = results.stream().mapToInt(ExtractedResult::getScore).sum() / results.size();
                String weaponName = results.get(0).getString() ;
                modName = new ExtractedResult(weaponName, averageScore, 0);
            }
            
        }

        logger.info("OCR: {}, {}", modName.getString(), modName.getScore());

        return new AbstractMap.SimpleEntry<>(modName.getString(), modNames.get(modName.getString()));
    }

}
