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

/** Official Open-Meteo forecast API; no API key and no tracking SDK. */
public final class WeatherApi {
    private WeatherApi() {}
    private static final ZoneId COPENHAGEN = ZoneId.of("Europe/Copenhagen");

    public static WeatherData load(double lat, double lon, String label) throws Exception {
        String endpoint = String.format(Locale.US,
                "https://api.open-meteo.com/v1/forecast?latitude=%.5f&longitude=%.5f" +
                "&current=temperature_2m,apparent_temperature,wind_speed_10m,wind_direction_10m,wind_gusts_10m,weather_code" +
                "&hourly=precipitation_probability,temperature_2m" +
                "&daily=sunrise,sunset&timezone=Europe%%2FCopenhagen&forecast_days=2",
                lat, lon);
        HttpURLConnection connection = (HttpURLConnection) new URL(endpoint).openConnection();
        connection.setRequestProperty("Accept", "application/json");
        connection.setRequestProperty("User-Agent", "DiscVejr-Android/1.0");
        connection.setConnectTimeout(7000);
        connection.setReadTimeout(7000);
        try {
            int status = connection.getResponseCode();
            if (status != 200) throw new IllegalStateException("Vejrserver HTTP " + status);
            ByteArrayOutputStream buf = new ByteArrayOutputStream(32768);
            try (InputStream in = connection.getInputStream()) {
                byte[] chunk = new byte[4096];
                int size;
                while ((size = in.read(chunk)) != -1) {
                    buf.write(chunk, 0, size);
                    if (buf.size() > 512000) throw new IllegalStateException("For stort vejrsvar");
                }
            }
            return parse(new JSONObject(buf.toString(StandardCharsets.UTF_8.name())), label);
        } finally {
            connection.disconnect();
        }
    }

    static WeatherData parse(JSONObject root, String label) throws Exception {
        JSONObject current = root.getJSONObject("current");
        JSONObject hourly = root.getJSONObject("hourly");
        JSONObject daily = root.getJSONObject("daily");
        WeatherData out = new WeatherData();
        out.temperature = current.getDouble("temperature_2m");
        out.apparent = current.getDouble("apparent_temperature");
        out.wind = current.getDouble("wind_speed_10m");
        out.gust = current.getDouble("wind_gusts_10m");
        out.windDirection = current.getDouble("wind_direction_10m");
        out.weatherCode = current.getInt("weather_code");
        out.fetchedAt = System.currentTimeMillis();
        out.locationLabel = label;

        LocalDateTime now = LocalDateTime.now(COPENHAGEN);
        LocalDateTime hourStart = now.withMinute(0).withSecond(0).withNano(0);
        JSONArray times = hourly.getJSONArray("time");
        JSONArray rain = hourly.getJSONArray("precipitation_probability");
        int firstIndex = -1;
        for (int i = 0; i < times.length(); ++i) {
            LocalDateTime when = LocalDateTime.parse(times.getString(i));
            if (!when.isBefore(hourStart)) {
                firstIndex = i;
                break;
            }
        }
        if (firstIndex < 0 || firstIndex + 2 >= times.length()) {
            throw new IllegalStateException("Ingen timeprognose tilgængelig");
        }
        for (int i = 0; i < 3; ++i) {
            int index = firstIndex + i;
            out.hourLabels[i] = LocalDateTime.parse(times.getString(index))
                    .format(DateTimeFormatter.ofPattern("HH", new Locale("da", "DK")));
            out.hourRain[i] = Math.max(0, Math.min(100, rain.getInt(index)));
            out.rainMax3h = Math.max(out.rainMax3h, out.hourRain[i]);
        }

        JSONArray dates = daily.getJSONArray("time");
        JSONArray sunsets = daily.getJSONArray("sunset");
        JSONArray sunrises = daily.getJSONArray("sunrise");
        int dayIndex = -1;
        for (int i = 0; i < dates.length(); ++i) {
            if (LocalDate.parse(dates.getString(i)).equals(now.toLocalDate())) {
                dayIndex = i;
                break;
            }
        }
        if (dayIndex < 0) throw new IllegalStateException("Mangler dagens soldata");
        LocalDateTime rise = LocalDateTime.parse(sunrises.getString(dayIndex));
        LocalDateTime set = LocalDateTime.parse(sunsets.getString(dayIndex));
        out.dark = now.isBefore(rise) || !now.isBefore(set);
        out.sunset = set.format(DateTimeFormatter.ofPattern("HH:mm", new Locale("da", "DK")));
        return out;
    }
}
