package dk.discvejr.widget;

import org.json.JSONArray;
import org.json.JSONObject;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/** Open-Meteo forecasts (model estimates, not measurements at the throwing tee). */
public final class WeatherApi {
    private WeatherApi() {}
    private static final ZoneId COPENHAGEN=ZoneId.of("Europe/Copenhagen");
    public static WeatherData load(double lat,double lon,String label) throws Exception {
        String endpoint=String.format(Locale.US,
                "https://api.open-meteo.com/v1/forecast?latitude=%.5f&longitude=%.5f"+
                "&current=temperature_2m,apparent_temperature,relative_humidity_2m,surface_pressure,"+
                "cloud_cover,precipitation,wind_speed_10m,wind_direction_10m,wind_gusts_10m,weather_code"+
                "&hourly=precipitation_probability,precipitation,dew_point_2m,visibility,uv_index,"+
                "wind_speed_10m,wind_gusts_10m"+
                "&daily=sunrise,sunset&timezone=Europe%%2FCopenhagen&forecast_days=3",lat,lon);
        HttpURLConnection connection=(HttpURLConnection)new URL(endpoint).openConnection();
        connection.setRequestProperty("Accept","application/json");
        connection.setRequestProperty("User-Agent","DiscVejr-Android/2.0");
        connection.setConnectTimeout(9000); connection.setReadTimeout(9000);
        try {
            int status=connection.getResponseCode();
            if(status!=200) throw new IllegalStateException("Vejrserver HTTP "+status);
            ByteArrayOutputStream buf=new ByteArrayOutputStream(32768);
            try(InputStream in=connection.getInputStream()) {
                byte[] block=new byte[4096]; int n;
                while((n=in.read(block))!=-1) {
                    buf.write(block,0,n);
                    if(buf.size()>512000) throw new IllegalStateException("For stort vejrsvar");
                }
            }
            return parse(new JSONObject(buf.toString(StandardCharsets.UTF_8.name())),label);
        } finally {connection.disconnect();}
    }
    static WeatherData parse(JSONObject root,String label) throws Exception {
        JSONObject current=root.getJSONObject("current"), hourly=root.getJSONObject("hourly"),
                   daily=root.getJSONObject("daily");
        WeatherData d=new WeatherData();
        d.temperature=current.getDouble("temperature_2m");
        d.apparent=current.getDouble("apparent_temperature");
        d.wind=current.getDouble("wind_speed_10m"); d.gust=current.getDouble("wind_gusts_10m");
        d.windDirection=current.getDouble("wind_direction_10m");
        d.weatherCode=current.getInt("weather_code"); d.fetchedAt=System.currentTimeMillis();
        d.locationLabel=label;
        d.humidity=current.optDouble("relative_humidity_2m",0);
        d.pressure=current.optDouble("surface_pressure",0);
        d.cloud=current.optDouble("cloud_cover",0);
        d.precipitation=current.optDouble("precipitation",0);
        LocalDateTime now=LocalDateTime.now(COPENHAGEN), hour=now.withMinute(0).withSecond(0).withNano(0);
        JSONArray times=hourly.getJSONArray("time");
        int first=-1;
        for(int i=0;i<times.length();i++) {
            if(!LocalDateTime.parse(times.getString(i)).isBefore(hour)) {first=i;break;}
        }
        if(first<0 || first+2>=times.length()) throw new IllegalStateException("Ingen timeprognose");
        JSONArray rain=hourly.getJSONArray("precipitation_probability");
        JSONArray amount=hourly.optJSONArray("precipitation"), dew=hourly.optJSONArray("dew_point_2m");
        JSONArray vis=hourly.optJSONArray("visibility"), uv=hourly.optJSONArray("uv_index");
        JSONArray wind=hourly.optJSONArray("wind_speed_10m"), gust=hourly.optJSONArray("wind_gusts_10m");
        for(int i=0;i<3;i++) {
            int index=first+i;
            d.hourLabels[i]=LocalDateTime.parse(times.getString(index))
                    .format(DateTimeFormatter.ofPattern("HH",Locale.forLanguageTag("da-DK")));
            d.hourRain[i]=Math.max(0,Math.min(100,rain.optInt(index,0)));
            d.rainMax3h=Math.max(d.rainMax3h,d.hourRain[i]);
            d.hourRainMm[i]=amount==null ? 0 : amount.optDouble(index,0);
            d.rainfallNext3h+=d.hourRainMm[i];
            d.hourWind[i]=wind==null ? d.wind : wind.optDouble(index,d.wind);
            d.hourGust[i]=gust==null ? d.gust : gust.optDouble(index,d.gust);
        }
        d.dewPoint=dew==null ? 0 : dew.optDouble(first,0);
        d.visibility=vis==null ? 0 : vis.optDouble(first,0);
        d.uvIndex=uv==null ? 0 : uv.optDouble(first,0);
        JSONArray dates=daily.getJSONArray("time");
        JSONArray rises=daily.getJSONArray("sunrise"),sets=daily.getJSONArray("sunset");
        int day=-1;
        for(int i=0;i<dates.length();i++)
            if(LocalDate.parse(dates.getString(i)).equals(now.toLocalDate())) {day=i;break;}
        if(day<0) throw new IllegalStateException("Mangler dagens soldata");
        LocalDateTime sunrise=LocalDateTime.parse(rises.getString(day));
        LocalDateTime sunset=LocalDateTime.parse(sets.getString(day));
        d.dark=now.isBefore(sunrise)||!now.isBefore(sunset);
        d.sunrise=sunrise.format(DateTimeFormatter.ofPattern("HH:mm"));
        d.sunset=sunset.format(DateTimeFormatter.ofPattern("HH:mm"));
        return d;
    }
}
