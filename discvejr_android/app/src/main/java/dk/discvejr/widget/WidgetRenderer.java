package dk.discvejr.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.widget.RemoteViews;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public final class WidgetRenderer {
    private WidgetRenderer() {}
    private static final int FRESH_HOURS = 2;
    private static final ZoneId LOCAL = ZoneId.of("Europe/Copenhagen");
    private static final int GREEN = Color.parseColor("#B2F26A");
    private static final int AMBER = Color.parseColor("#F8C673");
    private static final int RED = Color.parseColor("#F08C8C");

    public static void updateAll(Context ctx) {
        AppWidgetManager mgr = AppWidgetManager.getInstance(ctx);
        int[] ids = mgr.getAppWidgetIds(new ComponentName(ctx, DiscWidget.class));
        for (int id : ids) updateOne(ctx, mgr, id);
    }

    public static void updateOne(Context ctx, AppWidgetManager manager, int id) {
        RemoteViews ui = new RemoteViews(ctx.getPackageName(), R.layout.widget);
        Intent open = new Intent(ctx, MainActivity.class);
        PendingIntent openPending = PendingIntent.getActivity(ctx, 0, open,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        ui.setOnClickPendingIntent(R.id.widget_root, openPending);
        Intent refresh = new Intent(ctx, RefreshReceiver.class);
        PendingIntent refreshPending = PendingIntent.getBroadcast(ctx, 0, refresh,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        ui.setOnClickPendingIntent(R.id.refresh, refreshPending);

        SharedPreferences prefs = ctx.getSharedPreferences("disc_weather", Context.MODE_PRIVATE);
        ui.setTextViewText(R.id.location, prefs.getString("label", "KVAGLUND"));
        String cached = prefs.getString("weather", null);
        if (cached != null) {
            try {
                WeatherData d = WeatherData.fromJson(cached);
                WeatherLogic.Assessment assess = WeatherLogic.assess(d.wind, d.gust,
                        d.rainMax3h, d.temperature, d.weatherCode, d.dark);
                long ageMs = Math.max(0, System.currentTimeMillis() - d.fetchedAt);
                boolean stale = ageMs > FRESH_HOURS * 3600000L;
                ui.setTextViewText(R.id.rating, stale ? "ÆLDRE PROGNOSE" : assess.label);
                ui.setTextColor(R.id.rating, stale ? AMBER :
                        assess.score >= 78 ? GREEN : assess.score >= 53 ? AMBER : RED);
                ui.setTextViewText(R.id.tip, assess.tip + " · " + assess.score + "/100");
                ui.setTextViewText(R.id.temp, String.format(Locale.getDefault(), "%.0f°", d.temperature));
                ui.setTextViewText(R.id.feels, String.format(Locale.getDefault(), "Føles som %.0f°", d.apparent));
                ui.setTextViewText(R.id.wind, String.format(Locale.getDefault(), "%.0f km/t %s",
                        d.wind, WeatherLogic.windFrom(d.windDirection)));
                ui.setTextViewText(R.id.gust, String.format(Locale.getDefault(), "%.0f km/t", d.gust));
                ui.setTextViewText(R.id.rain, d.rainMax3h + " %");
                ui.setTextViewText(R.id.h1, d.hourLabels[0] + " · " + d.hourRain[0] + "%");
                ui.setTextViewText(R.id.h2, d.hourLabels[1] + " · " + d.hourRain[1] + "%");
                ui.setTextViewText(R.id.h3, d.hourLabels[2] + " · " + d.hourRain[2] + "%");
                ui.setTextViewText(R.id.sunset, d.dark ? "Mørkt nu · glow/lygte" : "Solnedgang " + d.sunset);
                String updated = Instant.ofEpochMilli(d.fetchedAt).atZone(LOCAL)
                        .format(DateTimeFormatter.ofPattern("HH:mm", new Locale("da", "DK")));
                ui.setTextViewText(R.id.updated, (stale ? "Sidst " : "Opd. ") + updated);
            } catch (Exception invalid) {
                ui.setTextViewText(R.id.rating, "DATAFEJL · OPDATER");
                ui.setTextViewText(R.id.updated, "Tryk ↻");
            }
        } else {
            ui.setTextViewText(R.id.rating, "HENTER VEJRET");
            ui.setTextViewText(R.id.tip, "Kræver internet første gang");
            ui.setTextViewText(R.id.updated, "Tryk ↻");
        }
        manager.updateAppWidget(id, ui);
    }
}
