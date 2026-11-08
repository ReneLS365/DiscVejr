# DiscVejr: rigtig Android-widget til discgolf

**Version:** 1.0.0, Android 8.0+ (API 26+). Dansk brugerflade. Standard: Kvaglund, Esbjerg (55.49202, 8.49351).

## Funktioner

- Ægte Android App Widget til hjemmeskærmen, 4 × 3 felter, kan ændres i størrelse.
- Live vejrprognose via HTTPS fra [Open-Meteo](https://open-meteo.com/): temperatur, føles som, vind, vindretning, vindstød, regnrisiko de næste 3 timer, 3-timers regnoverblik, solnedgang.
- Discgolf-vurdering **0–100**, beregnet fra vind, vindstød, nedbørsrisiko, temperatur og vejrkode, ikke fra golfvejr-indekser.
- Bliver mørkt? Råd om glow og lygte. Tordenrisiko? Advarsel om at blive inde.
- Manuel opdatering på widgetten og automatiske opdateringer ca. hver 30. minut, når Android tillader det.
- Vælg enten Kvaglund eller telefonens omtrentlige position. Ingen konto, tracking eller reklamer.
- Vejret gemmes lokalt og vises med advarsel, hvis det er mere end to timer gammelt.

## Byg en installérbar APK

### Nem metode: GitHub Actions

1. Opret et **privat** GitHub-repository, og upload hele projektet (inklusive `.github/workflows/android-apk.yml`).
2. Åbn fanen **Actions** → **Byg DiscVejr APK** → **Run workflow**. Push til `main` starter også bygningen.
3. Når bygningen er gennemført, hent **DiscVejr-APK** under **Artifacts**. Udpak ZIP'en. Inde i den ligger `app-debug.apk`.
4. Åbn APK-filen på Android og giv tilladelse til installation fra den pågældende app, hvis Android beder om det.

### Android Studio

1. Åbn projektmappen i Android Studio (JDK 17).
2. Vælg **Build → Build Bundle(s) / APK(s) → Build APK(s)**.
3. Installer den genererede `app-debug.apk` på telefonen.

### Tilføj til Samsung-startskærm

1. Åbn **DiscVejr** og tryk **Opdater vejret nu**.
2. Gå til startskærmen; hold nede på et tomt område; vælg **Widgets**.
3. Find **DiscVejr**, vælg 4 × 3-widgetten og placer den på startskærmen.
4. Klik **↻** på widgetten for at opdatere manuelt. Resten af widgetten åbner appen.

## Teknisk

- Ingen eksterne runtime-afhængigheder. Java, Android platform API, RemoteViews, JobScheduler, HttpURLConnection og `org.json`.
- `minSdk=26`, `targetSdk=35`, AGP 8.7.3, Gradle 8.9, JDK 17.
- Widgetten benytter `JobScheduler` frem for netværk i broadcast-receiveren; almindelige App Widgets opdateres ikke hyppigere end cirka 30 minutter via `updatePeriodMillis`.
- Standardkoordinatet er **Kvaglunds centrum**, ikke nogen privat adresse.
- Placeringsadgang bliver kun spurgt om, når brugeren aktivt vælger **Brug min position**. Der sendes koordinater til Open-Meteo for vejrdata, og de gemmes lokalt.
- Det er en **vejrudsigtsmodel**, ikke en lokal vejrstation. Vejret kan være anderledes på banen, og score er ikke en sikkerhedsgaranti.

## Test

```sh
mkdir -p /tmp/discvejr-tests
javac -d /tmp/discvejr-tests app/src/main/java/dk/discvejr/widget/WeatherLogic.java tests/WeatherLogicTest.java
java -cp /tmp/discvejr-tests dk.discvejr.widget.WeatherLogicTest
```

## Licens / data

Vejrudsigter leveres af Open-Meteo. Tjek deres [aktuelle API-vilkår](https://open-meteo.com/en/terms), hvis appen senere skal distribueres kommercielt. Oplysning om datakilde vises i appen.
