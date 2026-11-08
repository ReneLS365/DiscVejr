# DiscVejr 2.0 – vejr og vind til discgolf

Native Android-app og startskærmswidget på dansk. Kvaglund i Esbjerg er standardområde.
**Status:** v2-udviklingsversion, test på fysisk Android-telefon før almindelig brug.

## Kastkompas

- Nord vender altid opad. Gul pil viser **hvor vinden kommer fra** (peger ind mod midten). Grøn pil viser **hvor discen kastes hen** (peger ud fra midten).
- Indstil kastretningen 0–359° med skyderen eller drej ±45°. Kastretningen gemmes også til widgetten.
- Modvind (+) og medvind (−) beregnes ud fra vindens retning og kastets retning, med sidevind fra højre (+) eller venstre (−). Komponenterne er vejledende, baseret på modelvind ved 10 m.
- Eksempel: kast mod Ø (90°) med vind FRA N (0°) giver sidevind fra venstre. Kast mod Ø med vind FRA Ø giver modvind.

## Vejrdata i appen

- Temperatur, oplevet temperatur, vind, vindstød, vindretning
- Regnrisiko og forventet nedbørsmængde næste 3 timer samt timevis vind og vindstød
- Relativ luftfugtighed, dugpunkt, skydække, sigtbarhed
- Overfladetryk, **estimeret** fugtig lufttæthed (kg/m³), UV-indeks
- Dagslys, solopgang, solnedgang
- Vejledende score 0–100 for drive, approach, putting og samlet forhold
- Grebsadvarsler ved fugt, nedbør og kulde samt særskilt stopråd ved torden

Data: [Open-Meteo](https://open-meteo.com/en/docs), prognoser med modelusikkerhed, ikke lokale målinger ved tee. Vejr ved skov, bygninger og på banen kan afvige markant. Kastanalysen er ikke en discflysimulator og tager ikke mold, vægt, spin eller release-vinkel med. Slå ikke op efter vejr i stedet for at følge varsler om torden.

## Installer APK fra GitHub Actions

1. Åbn [GitHub Actions – Byg DiscVejr APK](https://github.com/ReneLS365/DiscVejr/actions/workflows/android-apk.yml).
2. Vælg en **grøn** kørsel på den ønskede branch/commit.
3. Download artifact `DiscVejr-Android-APK`, udpak ZIP og find `app-debug.apk`.
4. Installer med Android-pakkeinstallationen eller Termux ADB:
   `adb install -r "/sti/til/app-debug.apk"`.
5. Hvis debug-signaturen er anderledes: `adb uninstall dk.discvejr.widget` og installér igen. Det sletter lokale appdata.
6. Åbn DiscVejr, opdater vejr og tilføj widgetten fra Android-startskærmens widget-menu.

## Teknisk

- Java 17, native Android, `minSdk=26`, `targetSdk=35`
- Open-Meteo forecast API og lokale SharedPreferences til cache og kastretning
- Android AppWidgetProvider / RemoteViews, JobScheduler for periodisk eller manuel opdatering
- Ingen profiler, annoncer eller tracking-SDK
- Ikke kommerciel brug: undersøg Open-Meteos gældende licensvilkår inden distribution

## Tests

GitHub Actions kompilerer og tester Java-vindberegninger før Android-APK bygges. Selve APK'en skal derudover afprøves på fysisk Samsung/Android: installation, netværk, widget, rotationskontrol, offline cache og baggrundsopdatering.
