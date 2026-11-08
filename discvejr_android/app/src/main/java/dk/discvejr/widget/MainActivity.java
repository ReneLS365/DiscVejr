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
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public final class MainActivity extends Activity {
    private static final int REQ_LOCATION=88;
    private static final Locale DA=Locale.forLanguageTag("da-DK");
    private final Handler handler=new Handler(Looper.getMainLooper());
    private WindCompassView compass;
    private SeekBar throwSeek;
    private WeatherData currentData;
    private int heading;

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        setContentView(R.layout.activity_main);
        compass=findViewById(R.id.compass);
        throwSeek=findViewById(R.id.throw_seek);
        heading=preferences().getInt("throw_heading",0);
        throwSeek.setProgress(heading);
        throwSeek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar seek,int progress,boolean user) {
                heading=WindAnalysis.normalize(progress);
                updateThrowPreview();
            }
            @Override public void onStartTrackingTouch(SeekBar seek) {}
            @Override public void onStopTrackingTouch(SeekBar seek) {
                saveThrowHeading();
            }
        });
        findViewById(R.id.bearing_left).setOnClickListener(v->
                changeHeading(heading-45));
        findViewById(R.id.bearing_right).setOnClickListener(v->
                changeHeading(heading+45));
        findViewById(R.id.kvaglund_button).setOnClickListener(v->{
            preferences().edit().putString("lat","55.49202").putString("lon","8.49351")
                    .putString("label","KVAGLUND").remove("weather").apply();
            currentData=null;
            updateUi();
            refreshNow();
        });
        findViewById(R.id.location_button).setOnClickListener(v->askForLocation());
        findViewById(R.id.refresh_button).setOnClickListener(v->refreshNow());
        WeatherJobs.ensurePeriodic(this);
        updateUi();
        if(preferences().getString("weather",null)==null)
            WeatherJobs.refresh(this);
        handler.postDelayed(this::updateUi,4000);
        handler.postDelayed(this::updateUi,11000);
    }
    private SharedPreferences preferences() {
        return getSharedPreferences("disc_weather",Context.MODE_PRIVATE);
    }
    @Override protected void onResume() {
        super.onResume();
        if(compass!=null) updateUi();
    }
    private void changeHeading(int bearing) {
        heading=WindAnalysis.normalize(bearing);
        throwSeek.setProgress(heading);
        saveThrowHeading();
    }
    private void saveThrowHeading() {
        preferences().edit().putInt("throw_heading",heading).apply();
        WidgetRenderer.updateAll(this);
    }
    private void set(int id,String value) {
        ((TextView)findViewById(id)).setText(value);
    }
    private void updateThrowPreview() {
        set(R.id.bearing_label,"KAST MOD "+WindAnalysis.direction(heading)+"  /  "+heading+"°");
        if(compass!=null) {
            compass.setAngles(currentData==null?0:currentData.windDirection,heading);
            compass.setVisibility(currentData==null?View.INVISIBLE:View.VISIBLE);
        }
        if(currentData==null) {
            set(R.id.relative_wind,"Afventer vinddata");
            set(R.id.wind_components,"Modvind, medvind og sidevind vises her.");
            set(R.id.throw_tip,"Vælg den retning, du vil kaste mod.");
            return;
        }
        WindAnalysis.Result r=WindAnalysis.analyze(currentData.windDirection,
                currentData.wind,heading);
        set(R.id.relative_wind,r.label);
        String length=String.format(DA,"%.0f km/t",Math.abs(r.headwind));
        String cross=String.format(DA,"%.0f km/t",Math.abs(r.crossFromRight));
        set(R.id.wind_components,(r.headwind>=0?"Modvind: ":"Medvind: ")+length+
                "\nSidevind fra "+(r.crossFromRight>=0?"højre: ":"venstre: ")+cross);
        set(R.id.throw_tip,r.guidance+"\n"+
                "Retningen kan variere langs hullet og tæt ved træer.");
    }
    private void updateUi() {
        if(compass==null)return;
        SharedPreferences p=preferences();
        String label=p.getString("label","KVAGLUND");
        set(R.id.current_place,"KVAGLUND".equals(label)?"Kvaglund, Esbjerg":"Telefonens position");
        String cached=p.getString("weather",null);
        if(cached==null) {
            currentData=null;
            set(R.id.hero_status,"HENTER VEJRET");
            set(R.id.hero_index,"-- / 100");
            String error=p.getString("error",null);
            set(R.id.hero_sub,error==null?"Henter prognose fra Open-Meteo…":
                    "Kunne ikke hente vejret: "+error+"\nTryk Opdater vejret nu.");
            set(R.id.updated_at,"Afventer data");
            updateThrowPreview();
            return;
        }
        try {
            WeatherData d=WeatherData.fromJson(cached);
            currentData=d;
            WeatherLogic.Assessment a=WeatherLogic.assess(d);
            long minutes=Math.max(0,(System.currentTimeMillis()-d.fetchedAt)/60000);
            boolean stale=minutes>120;
            set(R.id.hero_status,stale?"ÆLDRE PROGNOSE":a.label);
            set(R.id.hero_index,a.score+" / 100");
            set(R.id.hero_sub,(a.safety.isEmpty()?a.tip:a.safety)+
                    (stale?"\nAdvarsel: data er over 2 timer gamle.":""));
            set(R.id.drive_score,"DRIVE\n"+a.drive+" /100");
            set(R.id.approach_score,"APPROACH\n"+a.approach+" /100");
            set(R.id.putting_score,"PUTTING\n"+a.putting+" /100");
            set(R.id.updated_at,Instant.ofEpochMilli(d.fetchedAt)
                    .atZone(ZoneId.of("Europe/Copenhagen"))
                    .format(DateTimeFormatter.ofPattern("HH:mm",DA))+
                    (stale?" · gammel":""));
            set(R.id.wind_source,String.format(DA,
                    "Vind FRA %s (%.0f°)  →  mod %s",
                    WindAnalysis.direction(d.windDirection),d.windDirection,
                    WindAnalysis.direction(d.windDirection+180)));
            set(R.id.data_wind,String.format(DA,"Vind ved 10 m: %.0f km/t",d.wind));
            set(R.id.data_gust,String.format(DA,"Vindstød ved 10 m: %.0f km/t",d.gust));
            set(R.id.data_temperature,String.format(DA,
                    "Temperatur: %.1f°C  ·  føles som %.1f°C",d.temperature,d.apparent));
            set(R.id.data_rain,String.format(DA,
                    "Regn næste 3 timer: op til %d%%  ·  %.1f mm forventet",
                    d.rainMax3h,d.rainfallNext3h));
            set(R.id.data_humidity,String.format(DA,"Relativ luftfugtighed: %.0f%%",d.humidity));
            set(R.id.data_dew,String.format(DA,"Dugpunkt: %.1f°C",d.dewPoint));
            set(R.id.data_cloud,String.format(DA,"Skydække: %.0f%%",d.cloud));
            set(R.id.data_visibility,d.visibility>0?String.format(DA,
                    "Sigtbarhed: %.1f km",d.visibility/1000d):"Sigtbarhed: ikke oplyst");
            set(R.id.data_pressure,d.pressure>0?String.format(DA,
                    "Lufttryk ved jord: %.0f hPa",d.pressure):"Lufttryk: ikke oplyst");
            double density=WeatherLogic.airDensity(d.temperature,d.pressure,d.humidity);
            set(R.id.data_density,Double.isFinite(density)?String.format(DA,
                    "Lufttæthed, est.: %.3f kg/m³",density):"Lufttæthed: ikke beregnet");
            set(R.id.data_uv,String.format(DA,"UV-indeks (timeprognose): %.1f",d.uvIndex));
            set(R.id.data_sun,(d.dark?"Mørkt nu":"Dagslys nu")+
                    "  ·  solopgang "+d.sunrise+"  ·  solnedgang "+d.sunset);
            boolean moisture=d.rainfallNext3h>=.5 ||
                    (d.humidity>=88 && d.temperature-d.dewPoint<=3);
            set(R.id.grip_warning,moisture?
                    "GREB: fugt/regn kan gøre disc og hånd glatte. Tag håndklæde og ekstra disc.":
                    (d.temperature<4?"GREB: lave temperaturer kan gøre fingrene stive.":
                    "GREB: ingen tydelig fugtindikator i prognosen. Tjek discen lokalt."));
            StringBuilder hours=new StringBuilder();
            for(int i=0;i<3;i++) {
                if(i>0) hours.append("\n");
                hours.append(String.format(DA,
                        "Kl. %s   Regn %d%%  ·  %.1f mm  ·  vind %.0f / stød %.0f km/t",
                        d.hourLabels[i],d.hourRain[i],d.hourRainMm[i],
                        d.hourWind[i],d.hourGust[i]));
            }
            set(R.id.hourly_forecast,hours.toString());
        } catch(Exception error) {
            currentData=null;
            set(R.id.hero_status,"DATAFEJL");
            set(R.id.hero_sub,"Ugyldig gemt prognose. Tryk Opdater vejret nu.");
        }
        updateThrowPreview();
    }
    private void refreshNow() {
        preferences().edit().remove("error").apply();
        WeatherJobs.refresh(this);
        Toast.makeText(this,"Opdaterer prognosen",Toast.LENGTH_SHORT).show();
        updateUi();
        handler.postDelayed(this::updateUi,4000);
        handler.postDelayed(this::updateUi,11000);
    }
    private void askForLocation() {
        if(Build.VERSION.SDK_INT>=23 &&
                checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION)!=PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},REQ_LOCATION);
            return;
        }
        useCurrentLocation();
    }
    @Override public void onRequestPermissionsResult(int code,String[] permissions,int[] results) {
        super.onRequestPermissionsResult(code,permissions,results);
        if(code==REQ_LOCATION && results.length>0 && results[0]==PackageManager.PERMISSION_GRANTED)
            useCurrentLocation();
        else if(code==REQ_LOCATION)
            Toast.makeText(this,"Placering afvist; Kvaglund virker stadig",Toast.LENGTH_LONG).show();
    }
    @SuppressWarnings("MissingPermission")
    private void useCurrentLocation() {
        LocationManager lm=(LocationManager)getSystemService(Context.LOCATION_SERVICE);
        if(lm==null){Toast.makeText(this,"Ingen positionsservice",Toast.LENGTH_LONG).show();return;}
        try {
            String provider=lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER)?
                    LocationManager.NETWORK_PROVIDER:LocationManager.GPS_PROVIDER;
            Location last=lm.getLastKnownLocation(provider);
            if(last!=null && System.currentTimeMillis()-last.getTime()<60*60_000L) {
                saveLocation(last);return;
            }
            if(Build.VERSION.SDK_INT>=30) {
                lm.getCurrentLocation(provider,null,getMainExecutor(),loc->{
                    if(loc!=null)saveLocation(loc);
                    else Toast.makeText(this,"Ingen position fundet. Brug Kvaglund.",Toast.LENGTH_LONG).show();
                });
                Toast.makeText(this,"Finder din position…",Toast.LENGTH_SHORT).show();
            } else if(last!=null)saveLocation(last);
            else Toast.makeText(this,"Ingen position fundet. Brug Kvaglund.",Toast.LENGTH_LONG).show();
        } catch(SecurityException|IllegalArgumentException e) {
            Toast.makeText(this,"Aktivér placering og prøv igen.",Toast.LENGTH_LONG).show();
        }
    }
    private void saveLocation(Location location) {
        if(isFinishing()||isDestroyed())return;
        preferences().edit().putString("lat",Double.toString(location.getLatitude()))
                .putString("lon",Double.toString(location.getLongitude()))
                .putString("label","MIN POSITION").remove("weather").apply();
        currentData=null;updateUi();WidgetRenderer.updateAll(this);refreshNow();
    }
}
