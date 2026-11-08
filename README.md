# DiscVejr

Selvstaendig Android-app og startskærmswidget til discgolfvejr i **Kvaglund / Esbjerg**. Helt adskilt fra Akkordklar.

## Installer på Samsung/Android

1. Aabn [DiscVejr Android-bygninger](https://github.com/ReneLS365/DiscVejr/actions/workflows/android-apk.yml).
2. Vaelg den nyeste koersel med groent flueben.
3. Hent **DiscVejr-Android-APK** under *Artifacts* (GitHub-login kan vaere paakraevet).
4. Udpak ZIP-filen og aabn **app-debug.apk** med telefonens Filer-app. Tillad installation fra den konkrete kilde, hvis Android beder om det.
5. Aabn DiscVejr en gang. Hold fingeren paa startskærmen, vaelg **Widgets** og tilfoej **DiscVejr**.

**OBS:** Android/Samsung kan blokere installation af ukendte APK'er. `INSTALL_FAILED_VERIFICATION_FAILURE` er typisk et verifikations-/sikkerhedsafslag fra telefonen, ikke et problem med GitHub-repositoryets navn. Kontroller sikkerhedsadvarslerne paa telefonen. Installer kun APK'er du selv har bygget eller stoler paa.

## Hvad widgetten viser

- Vindstyrke, retning og vindstoed
- Temperatur og foeles-som
- Regnrisiko de naeste 3 timer
- Solnedgang, glow-forhold og discgolfscore (0-100)
- Råd om forholdene paa banen

Vejrdata hentes fra **Open-Meteo** uden API-noegle. Kvaglund er standardsted; valgfri GPS. Widgetten opdaterer cirka hver halve time, hvis Android tillader det.

## Kildekode og build

Selvstaendig Android/Gradle-kode ligger i `discvejr_android/`. Ved foerste build udpakker workflowet kilden fra en checksum-kontrolleret kopi i `.source/` og forsoeger automatisk at committe de udpakkede filer, saa de kan redigeres direkte i GitHub/Codex.

- Workflow: `.github/workflows/android-apk.yml`
- Krav: JDK 17, Gradle 8.9 og Android SDK 35
- Build: `cd discvejr_android && gradle assembleDebug`
- Unit test: `javac -d /tmp/discvejr-tests discvejr_android/app/src/main/java/dk/discvejr/widget/WeatherLogic.java discvejr_android/tests/WeatherLogicTest.java && java -cp /tmp/discvejr-tests dk.discvejr.widget.WeatherLogicTest`

**Signering:** Dette er en debug-bygning. Til udgivelser med stabile opdateringer skal appen fremover signeres med en permanent, privat release-noegle, som ikke maa committes til et offentligt repository.
