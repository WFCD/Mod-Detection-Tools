package gb.bourne2code.warframe.opencv.utils;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Logger;

public class WarframeMarketAPI {
    private WarframeMarketAPI() {}

    private static Logger logger = Logger.getLogger(WarframeMarketAPI.class.getName());

    private static final String BASE = "https://api.warframe.market/v1";
    private static final String ITEMS = BASE + "/items";

    private static URL getOrdersUrl(String itemName) {
        try {
            return new URL(ITEMS + "/" + itemName + "/orders");
        } catch (MalformedURLException ignored) { /* should never happen */ }
        return null;
    }

    public static int getPrice(String item, String platform) {
        HttpURLConnection connection;
        try {
            URL url = getOrdersUrl(item);

            assert url != null; //shoudn't be, but go away ide

            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.addRequestProperty("platform", platform.toLowerCase());

            //im going to kill someone if i have to do this bullshit again why can i just put the inputstream into the json parser
            BufferedReader in = new BufferedReader(new InputStreamReader(
                    connection.getInputStream()));
            String inputLine;
            StringBuilder response = new StringBuilder();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            //check for rate limit, if so, wait and try again
            if (connection.getResponseCode() == 503) {
                logger.info("RIP: Rate limited. Retrying in 5 seconds.");
                Thread.sleep(5000);
                return getPrice(item, platform);
            }

            //finally fucking parse the json
            JSONObject json = new JSONObject(response.toString()).getJSONObject("payload");
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
            Thread.currentThread().interrupt();
        }

        return 0;
    }


}
