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
    private static final ZoneId LOCAL=ZoneId.of("Europe/Copenhagen");
    private static final Locale DA=Locale.forLanguageTag("da-DK");
    private static final int GREEN=Color.parseColor("#B2F26A");
    private static final int AMBER=Color.parseColor("#F8C673");
    private static final int RED=Color.parseColor("#F08C8C");

    public static void updateAll(Context ctx) {
        AppWidgetManager manager=AppWidgetManager.getInstance(ctx);
        int[] ids=manager.getAppWidgetIds(new ComponentName(ctx,DiscWidget.class));
        for(int id:ids)updateOne(ctx,manager,id);
    }
    public static void updateOne(Context ctx,AppWidgetManager manager,int id) {
        RemoteViews ui=new RemoteViews(ctx.getPackageName(),R.layout.widget);
        Intent open=new Intent(ctx,MainActivity.class);
        PendingIntent openPending=PendingIntent.getActivity(ctx,0,open,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        ui.setOnClickPendingIntent(R.id.widget_root,openPending);
        Intent refresh=new Intent(ctx,RefreshReceiver.class);
        PendingIntent refreshPending=PendingIntent.getBroadcast(ctx,0,refresh,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        ui.setOnClickPendingIntent(R.id.refresh,refreshPending);

        SharedPreferences prefs=ctx.getSharedPreferences("disc_weather",Context.MODE_PRIVATE);
        int heading=prefs.getInt("throw_heading",0);
        ui.setTextViewText(R.id.location,prefs.getString("label","KVAGLUND"));
        String cached=prefs.getString("weather",null);
        if(cached!=null) {
            try {
                WeatherData d=WeatherData.fromJson(cached);
                WeatherLogic.Assessment a=WeatherLogic.assess(d);
                WindAnalysis.Result w=WindAnalysis.analyze(d.windDirection,d.wind,heading);
                long age=Math.max(0,System.currentTimeMillis()-d.fetchedAt);
                boolean stale=age>2L*3600000;
                ui.setTextViewText(R.id.rating,stale?"ÆLDRE PROGNOSE":a.label);
                ui.setTextColor(R.id.rating,stale?AMBER:a.score>=78?GREEN:a.score>=53?AMBER:RED);
                ui.setTextViewText(R.id.tip,a.tip+" · "+a.score+"/100");
                ui.setTextViewText(R.id.temp,String.format(DA,"%.0f°",d.temperature));
                ui.setTextViewText(R.id.feels,String.format(DA,"Føles som %.0f°",d.apparent));
                ui.setTextViewText(R.id.wind,String.format(DA,"%.0f km/t",d.wind));
                ui.setTextViewText(R.id.gust,String.format(DA,"%.0f km/t",d.gust));
                ui.setTextViewText(R.id.rain,d.rainMax3h+" %");
                ui.setTextViewText(R.id.wind_direction,
                        "Fra "+WindAnalysis.direction(d.windDirection)+"  →  mod "+
                        WindAnalysis.direction(d.windDirection+180));
                ui.setTextViewText(R.id.throw_summary,"Kast mod "+WindAnalysis.direction(heading)+": "+w.label);
                ui.setImageViewBitmap(R.id.wind_compass,CompassArt.bitmap(d.windDirection,heading,180));
                ui.setContentDescription(R.id.wind_compass,"Gul pil: vind fra "+
                        WindAnalysis.direction(d.windDirection)+". Grøn pil: kast mod "+
                        WindAnalysis.direction(heading));
                ui.setTextViewText(R.id.h1,d.hourLabels[0]+" · "+d.hourRain[0]+"%");
                ui.setTextViewText(R.id.h2,d.hourLabels[1]+" · "+d.hourRain[1]+"%");
                ui.setTextViewText(R.id.h3,d.hourLabels[2]+" · "+d.hourRain[2]+"%");
                ui.setTextViewText(R.id.sunset,d.dark?"Mørkt · glow/lygte":"Solnedgang "+d.sunset);
                String updated=Instant.ofEpochMilli(d.fetchedAt).atZone(LOCAL)
                        .format(DateTimeFormatter.ofPattern("HH:mm",DA));
                ui.setTextViewText(R.id.updated,(stale?"Sidst ":"Opd. ")+updated);
            } catch(Exception invalid) {
                ui.setTextViewText(R.id.rating,"DATAFEJL · OPDATER");
                ui.setTextViewText(R.id.updated,"Tryk ↻");
            }
        } else {
            ui.setTextViewText(R.id.rating,"HENTER VEJRET");
            String error=prefs.getString("error",null);
            ui.setTextViewText(R.id.tip,error==null?"Opdateres når data er klar":"Netværksfejl · åbn appen");
            ui.setTextViewText(R.id.wind_direction,"Venter på vindretning");
            ui.setTextViewText(R.id.throw_summary,"Vælg kastretning i appen");
            ui.setTextViewText(R.id.updated,"Tryk ↻");
        }
        manager.updateAppWidget(id,ui);
    }
}
