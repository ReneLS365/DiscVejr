package dk.discvejr.widget;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Locale;

public final class MainActivity extends Activity {
    private static final int REQ_LOCATION = 88;
    private final Handler handler = new Handler(Looper.getMainLooper());

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        setContentView(R.layout.activity_main);
        findViewById(R.id.kvaglund_button).setOnClickListener(v -> {
            SharedPreferences prefs = getSharedPreferences("disc_weather", MODE_PRIVATE);
            prefs.edit().putString("lat", "55.49202").putString("lon", "8.49351")
                    .putString("label", "KVAGLUND").remove("weather").apply();
            refreshNow();
        });
        findViewById(R.id.location_button).setOnClickListener(v -> askForLocation());
        findViewById(R.id.refresh_button).setOnClickListener(v -> refreshNow());
        WeatherJobs.ensurePeriodic(this);
        updateUi();
        if (getSharedPreferences("disc_weather", MODE_PRIVATE).getString("weather", null) == null)
            WeatherJobs.refresh(this);
    }

    @Override protected void onResume() {
        super.onResume();
        updateUi();
    }

    private void refreshNow() {
        getSharedPreferences("disc_weather", MODE_PRIVATE).edit().remove("error").apply();
        WeatherJobs.refresh(this);
        Toast.makeText(this, "Opdatering bestilt", Toast.LENGTH_SHORT).show();
        updateUi();
        handler.postDelayed(this::updateUi, 3500);
        handler.postDelayed(this::updateUi, 8500);
    }

    private void updateUi() {
        SharedPreferences prefs = getSharedPreferences("disc_weather", Context.MODE_PRIVATE);
        String label = prefs.getString("label", "KVAGLUND");
        ((TextView) findViewById(R.id.current_place)).setText(
                "KVAGLUND".equals(label) ? "Kvaglund, Esbjerg" : "Telefonens position");
        String cached = prefs.getString("weather", null);
        TextView text = findViewById(R.id.summary);
        if (cached == null) {
            String problem = prefs.getString("error", null);
            text.setText(problem == null ? "Henter vejrdata fra Open-Meteo…" :
                    "Kunne ikke hente vejret: " + problem + "\nPrøv Opdater vejret nu.");
            return;
        }
        try {
            WeatherData d = WeatherData.fromJson(cached);
            WeatherLogic.Assessment a = WeatherLogic.assess(d.wind, d.gust, d.rainMax3h,
                    d.temperature, d.weatherCode, d.dark);
            long ageMinutes = Math.max(0, (System.currentTimeMillis() - d.fetchedAt) / 60000L);
            String stale = ageMinutes > 120 ? "\nOBS: Prognosen er ældre end 2 timer." : "";
            text.setText(String.format(Locale.forLanguageTag("da-DK"),
                    "%s  ·  %d/100\n\nTemperatur: %.0f°C (føles som %.0f°C)\n" +
                    "Vind: %.0f km/t fra %s\nVindstød: %.0f km/t\n" +
                    "Højeste regnrisiko de næste 3 timer: %d%%\n" +
                    "%s\n\n%s\n\nOpdateret for %d min. siden%s",
                    a.label, a.score, d.temperature, d.apparent,
                    d.wind, WeatherLogic.windFrom(d.windDirection),
                    d.gust, d.rainMax3h, d.dark ? "Det er mørkt nu" : "Solnedgang: " + d.sunset,
                    a.tip, ageMinutes, stale));
        } catch (Exception e) {
            text.setText("Kunne ikke læse gemte vejrdata. Tryk Opdater vejret nu.");
        }
    }

    private void askForLocation() {
        if (Build.VERSION.SDK_INT >= 23 &&
                checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, REQ_LOCATION);
            return;
        }
        useCurrentLocation();
    }

    @Override public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] results) {
        super.onRequestPermissionsResult(requestCode, permissions, results);
        if (requestCode == REQ_LOCATION && results.length > 0 && results[0] == PackageManager.PERMISSION_GRANTED) {
            useCurrentLocation();
        } else if (requestCode == REQ_LOCATION) {
            Toast.makeText(this, "Placering afvist. Kvaglund kan stadig bruges.", Toast.LENGTH_LONG).show();
        }
    }

    @SuppressWarnings("MissingPermission")
    private void useCurrentLocation() {
        LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (lm == null) {
            Toast.makeText(this, "Placering er ikke tilgængelig", Toast.LENGTH_LONG).show();
            return;
        }
        try {
            String provider = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER) ?
                    LocationManager.NETWORK_PROVIDER : LocationManager.GPS_PROVIDER;
            Location last = lm.getLastKnownLocation(provider);
            if (last != null && System.currentTimeMillis() - last.getTime() < 60 * 60_000L) {
                saveLocation(last);
                return;
            }
            if (Build.VERSION.SDK_INT >= 30) {
                lm.getCurrentLocation(provider, null, getMainExecutor(), loc -> {
                    if (loc != null) saveLocation(loc);
                    else Toast.makeText(this, "Ingen position fundet. Prøv igen eller brug Kvaglund.", Toast.LENGTH_LONG).show();
                });
                Toast.makeText(this, "Finder din position…", Toast.LENGTH_SHORT).show();
            } else if (last != null) {
                saveLocation(last);
            } else {
                Toast.makeText(this, "Ingen position fundet. Brug Kvaglund.", Toast.LENGTH_LONG).show();
            }
        } catch (SecurityException | IllegalArgumentException problem) {
            Toast.makeText(this, "Aktivér placering på telefonen og prøv igen.", Toast.LENGTH_LONG).show();
        }
    }

    private void saveLocation(Location loc) {
        if (isFinishing() || isDestroyed()) return;
        getSharedPreferences("disc_weather", MODE_PRIVATE).edit()
                .putString("lat", Double.toString(loc.getLatitude()))
                .putString("lon", Double.toString(loc.getLongitude()))
                .putString("label", "MIN POSITION")
                .remove("weather").apply();
        WidgetRenderer.updateAll(this);
        refreshNow();
    }
}
