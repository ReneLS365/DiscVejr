# DiscVejr

Selvstændig Android-app med **ægte startskærmswidget** til discgolf-vejr i Kvaglund, Esbjerg. Projektet er adskilt fra Akkordklar.

## Download og installation

**[Hent APK-bygninger på GitHub Actions](https://github.com/ReneLS365/DiscVejr/actions/workflows/android-apk.yml)**

1. Åbn seneste gennemførte bygning med grønt flueben.
2. Download `DiscVejr-Android-APK` under **Artifacts** (GitHub-login kan kræves).
3. Pak ZIP-arkivet ud på mobilen og åbn `app-debug.apk`.
4. Hvis Android beder om det, tillad installation fra den app, du åbnede APK'en i.
5. Åbn DiscVejr, vælg **Opdater vejret nu**, og placer widgetten: hold på startskærmen → **Widgets** → **DiscVejr** (4 × 3).

Ved fejl som `INSTALL_FAILED_VERIFICATION_FAILURE` er det Androids pakkeverifikation, der afviser installationen. Se telefonens sikkerhedsadvarsler og kontrollér eventuel Samsung Auto Blocker. En gyldig APK er ikke en garanti for, at Android tillader installation.

## Funktioner

- Vindstyrke, vindretning og vindstød
- Temperatur og føles-som-temperatur
- Regnrisiko de næste 3 timer og solnedgang
- Discgolf-score fra 0 til 100 med råd om vejrforholdene
- Glow-/mørkeforhold og tordenadvarsler
- Opdateringsknap og opdatering cirka hvert 30. minut, når Android tillader det
- Kvaglund som standard, valgfrit telefonens omtrentlige position

Vejrdata leveres af [Open-Meteo](https://open-meteo.com/). Ingen API-nøgle, reklamer eller brugertracking.

## Kildekode

Alt ligger i `discvejr_android/`, klar til udvikling via GitHub, Codex eller Android Studio.

- Android Java / native widget: `discvejr_android/app/src/main/`
- Beregninger: `WeatherLogic.java`
- Tests: `discvejr_android/tests/WeatherLogicTest.java`
- CI og APK-build: `.github/workflows/android-apk.yml`

Krav: **Java 17, Gradle 8.9, Android SDK 35**.

Lokalt build fra repositoryets rod:

```bash
cd discvejr_android
gradle assembleDebug
```

Dette er en debug-signeret testudgave. Fremtidige stabile udgivelser og opdateringer bør signeres med en vedvarende, privat release-nøgle, som aldrig lægges i et offentligt repository.

Læs [Android-projektets dokumentation](discvejr_android/README.md) for flere detaljer.
