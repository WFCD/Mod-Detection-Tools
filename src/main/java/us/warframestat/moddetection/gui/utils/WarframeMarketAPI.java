package us.warframestat.moddetection.gui.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.logging.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class WarframeMarketAPI {
  private WarframeMarketAPI() {}

  private static final Logger logger = Logger.getLogger(WarframeMarketAPI.class.getName());

  private static final String BASE = "https://api.warframe.market/v1";
  private static final String ITEMS = BASE + "/items";

  /**
   * Get the URL to retrieve the orders for a mod.
   *
   * @param itemName item id
   * @return url to get the orders for the item
   */
  private static URL getOrdersUrl(String itemName) {
    try {
      return new URL(ITEMS + "/" + itemName + "/orders");
    } catch (MalformedURLException ignored) {
      /* should never happen */
    }
    return null;
  }

  /**
   * Get the current average platinum price of a mod.
   *
   * @param item Mod id
   * @param platform Platform, defaults to pc (choice of pc, xbox, ps4, or switch)
   * @return Average platinum price of the mod
   */
  public static int getPrice(String item, String platform) {
    // check for riven
    if (Objects.equals(item, "riven_mod")) {
      return 0;
    }

    HttpURLConnection connection = null;
    try {
      URL url = getOrdersUrl(item);

      assert url != null; // shoudn't be, but go away ide

      connection = (HttpURLConnection) url.openConnection();
      connection.setRequestMethod("GET");
      connection.addRequestProperty("platform", platform.toLowerCase());

      // im going to kill someone if i have to do this bullshit again why can i just put the
      // inputstream into the json parser
      BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
      String inputLine;
      StringBuilder response = new StringBuilder();

      while ((inputLine = in.readLine()) != null) {
        response.append(inputLine);
      }
      in.close();

      // finally parse the json
      JSONArray json;
      try {
        json = new JSONObject(response.toString()).getJSONObject("payload").getJSONArray("orders");
      } catch (JSONException e) {
        return 0;
      }

      List<Integer> prices = new ArrayList<>();
      for (int i = 0; i < json.length(); i++) {
        JSONObject order = json.getJSONObject(i);
        String orderType = order.getString("order_type");
        int price = order.getInt("platinum");
        if (orderType.equals("sell")) {
          prices.add(price);
        }
      }

      Arrays.sort(prices.toArray());
      double median;
      if (prices.size() % 2 == 0)
        median =
            ((double) prices.get(prices.size() / 2) + (double) prices.get(prices.size() / 2 - 1))
                / 2;
      else median = (double) prices.get(prices.size() / 2);
      return (int) median;

    } catch (IOException e) {
      if (connection != null) {
        // check for rate limit, if so, wait and try again
        try {
          if (connection.getResponseCode() == 503) {
            logger.info("RIP: Rate limited. Retrying in 5 seconds.");
            Thread.sleep(5000);
            return getPrice(item, platform);
          }
        } catch (InterruptedException | IOException error2) {
          e.printStackTrace();
          if (error2 instanceof InterruptedException) {
            Thread.currentThread().interrupt();
          }
        }
      }
    }
    return 0;
  }
}
