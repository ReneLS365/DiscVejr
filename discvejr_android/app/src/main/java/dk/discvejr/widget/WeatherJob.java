package dk.discvejr.widget;

import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.SharedPreferences;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class WeatherJob extends JobService {
    private static final ExecutorService IO = Executors.newSingleThreadExecutor();
    private volatile boolean stopped;

    @Override public boolean onStartJob(JobParameters params) {
        stopped = false;
        IO.execute(() -> {
            try {
                SharedPreferences prefs = getSharedPreferences("disc_weather", MODE_PRIVATE);
                double lat = Double.parseDouble(prefs.getString("lat", "55.49202"));
                double lon = Double.parseDouble(prefs.getString("lon", "8.49351"));
                String label = prefs.getString("label", "KVAGLUND");
                WeatherData data = WeatherApi.load(lat, lon, label);
                if (!stopped) {
                    prefs.edit().putString("weather", data.toJson().toString())
                            .remove("error").apply();
                }
            } catch (Exception ex) {
                getSharedPreferences("disc_weather", MODE_PRIVATE).edit()
                        .putString("error", ex.getMessage() == null ? "Netværksfejl" : ex.getMessage())
                        .apply();
            } finally {
                if (!stopped) {
                    WidgetRenderer.updateAll(this);
                    jobFinished(params, false);
                }
            }
        });
        return true;
    }

    @Override public boolean onStopJob(JobParameters params) {
        stopped = true;
        return true;
    }
}
