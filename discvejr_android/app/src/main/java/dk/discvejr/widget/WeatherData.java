package dk.discvejr.widget;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/** Cached forecast model snapshot, compatible with 1.x cache. */
public final class WeatherData {
    public double temperature, apparent, wind, gust, windDirection;
    public double humidity, pressure, dewPoint, cloud, visibility, precipitation, rainfallNext3h, uvIndex;
    public int rainMax3h, weatherCode;
    public boolean dark;
    public String sunset = "--:--", sunrise = "--:--";
    public String[] hourLabels = {"--","--","--"};
    public int[] hourRain = {0,0,0};
    public double[] hourRainMm = {0,0,0}, hourWind = {0,0,0}, hourGust={0,0,0};
    public long fetchedAt;
    public String locationLabel = "KVAGLUND";

    public JSONObject toJson() throws JSONException {
        JSONObject o=new JSONObject();
        o.put("temperature",temperature).put("apparent",apparent).put("wind",wind)
         .put("gust",gust).put("windDirection",windDirection).put("rainMax3h",rainMax3h)
         .put("weatherCode",weatherCode).put("dark",dark).put("sunset",sunset)
         .put("sunrise",sunrise).put("fetchedAt",fetchedAt).put("locationLabel",locationLabel)
         .put("humidity",humidity).put("pressure",pressure).put("dewPoint",dewPoint)
         .put("cloud",cloud).put("visibility",visibility).put("precipitation",precipitation)
         .put("rainfallNext3h",rainfallNext3h).put("uvIndex",uvIndex);
        JSONArray labels=new JSONArray(), probability=new JSONArray(), amount=new JSONArray(),
                speed=new JSONArray(), gusts=new JSONArray();
        for (int i=0;i<3;i++) {
            labels.put(hourLabels[i]); probability.put(hourRain[i]); amount.put(hourRainMm[i]);
            speed.put(hourWind[i]); gusts.put(hourGust[i]);
        }
        o.put("hourLabels",labels).put("hourRain",probability).put("hourRainMm",amount)
         .put("hourWind",speed).put("hourGust",gusts);
        return o;
    }
    public static WeatherData fromJson(String json) throws JSONException {
        JSONObject o=new JSONObject(json);
        WeatherData d=new WeatherData();
        d.temperature=o.getDouble("temperature"); d.apparent=o.getDouble("apparent");
        d.wind=o.getDouble("wind"); d.gust=o.getDouble("gust");
        d.windDirection=o.getDouble("windDirection"); d.rainMax3h=o.getInt("rainMax3h");
        d.weatherCode=o.getInt("weatherCode"); d.dark=o.getBoolean("dark");
        d.sunset=o.getString("sunset"); d.sunrise=o.optString("sunrise","--:--");
        d.fetchedAt=o.getLong("fetchedAt"); d.locationLabel=o.optString("locationLabel","KVAGLUND");
        d.humidity=o.optDouble("humidity",0); d.pressure=o.optDouble("pressure",0);
        d.dewPoint=o.optDouble("dewPoint",0); d.cloud=o.optDouble("cloud",0);
        d.visibility=o.optDouble("visibility",0); d.precipitation=o.optDouble("precipitation",0);
        d.rainfallNext3h=o.optDouble("rainfallNext3h",0); d.uvIndex=o.optDouble("uvIndex",0);
        JSONArray labels=o.getJSONArray("hourLabels"), rain=o.getJSONArray("hourRain");
        JSONArray mm=o.optJSONArray("hourRainMm"), speed=o.optJSONArray("hourWind"),
                gusts=o.optJSONArray("hourGust");
        for (int i=0;i<3;i++) {
            d.hourLabels[i]=labels.getString(i); d.hourRain[i]=rain.getInt(i);
            if (mm!=null) d.hourRainMm[i]=mm.optDouble(i,0);
            if (speed!=null) d.hourWind[i]=speed.optDouble(i,0);
            if (gusts!=null) d.hourGust[i]=gusts.optDouble(i,0);
        }
        return d;
    }
}
