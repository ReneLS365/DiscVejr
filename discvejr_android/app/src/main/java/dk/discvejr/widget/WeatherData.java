package dk.discvejr.widget;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/** A compact, offline-safe snapshot for RemoteViews. */
public final class WeatherData {
    public double temperature;
    public double apparent;
    public double wind;
    public double gust;
    public double windDirection;
    public int rainMax3h;
    public int weatherCode;
    public boolean dark;
    public String sunset = "--:--";
    public String[] hourLabels = {"--", "--", "--"};
    public int[] hourRain = {0, 0, 0};
    public long fetchedAt;
    public String locationLabel = "KVAGLUND";

    public JSONObject toJson() throws JSONException {
        JSONObject obj = new JSONObject();
        obj.put("temperature", temperature).put("apparent", apparent)
                .put("wind", wind).put("gust", gust)
                .put("windDirection", windDirection)
                .put("rainMax3h", rainMax3h).put("weatherCode", weatherCode)
                .put("dark", dark).put("sunset", sunset)
                .put("fetchedAt", fetchedAt).put("locationLabel", locationLabel);
        JSONArray hours = new JSONArray();
        JSONArray rain = new JSONArray();
        for (int i = 0; i < 3; ++i) {
            hours.put(hourLabels[i]);
            rain.put(hourRain[i]);
        }
        obj.put("hourLabels", hours).put("hourRain", rain);
        return obj;
    }

    public static WeatherData fromJson(String json) throws JSONException {
        JSONObject obj = new JSONObject(json);
        WeatherData d = new WeatherData();
        d.temperature = obj.getDouble("temperature");
        d.apparent = obj.getDouble("apparent");
        d.wind = obj.getDouble("wind");
        d.gust = obj.getDouble("gust");
        d.windDirection = obj.getDouble("windDirection");
        d.rainMax3h = obj.getInt("rainMax3h");
        d.weatherCode = obj.getInt("weatherCode");
        d.dark = obj.getBoolean("dark");
        d.sunset = obj.getString("sunset");
        d.fetchedAt = obj.getLong("fetchedAt");
        d.locationLabel = obj.optString("locationLabel", "KVAGLUND");
        JSONArray hours = obj.getJSONArray("hourLabels");
        JSONArray rain = obj.getJSONArray("hourRain");
        for (int i = 0; i < 3; ++i) {
            d.hourLabels[i] = hours.getString(i);
            d.hourRain[i] = rain.getInt(i);
        }
        return d;
    }
}
