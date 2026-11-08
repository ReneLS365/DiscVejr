package dk.discvejr.widget;

/** Discspecific score; estimates convenience, not an official weather or safety rating. */
public final class WeatherLogic {
    private WeatherLogic() {}

    public static final class Assessment {
        public final int score;
        public final String label;
        public final String tip;
        Assessment(int score, String label, String tip) {
            this.score = score;
            this.label = label;
            this.tip = tip;
        }
    }

    public static Assessment assess(double wind, double gust, int rainNext3h,
                                    double temp, int weatherCode, boolean dark) {
        int deduction = 0;
        if (wind >= 40) deduction += 42;
        else if (wind >= 30) deduction += 26;
        else if (wind >= 20) deduction += 13;
        else if (wind >= 13) deduction += 5;

        if (gust >= 60) deduction += 30;
        else if (gust >= 45) deduction += 21;
        else if (gust >= 33) deduction += 12;
        else if (gust >= 24) deduction += 5;

        if (rainNext3h >= 75) deduction += 30;
        else if (rainNext3h >= 50) deduction += 18;
        else if (rainNext3h >= 30) deduction += 8;
        if (temp < 0) deduction += 20;
        else if (temp < 5) deduction += 10;
        else if (temp < 10) deduction += 4;

        if (weatherCode >= 95) deduction += 45; // Thunderstorm
        else if (weatherCode >= 71 && weatherCode <= 86) deduction += 22; // Snow / freezing
        else if (weatherCode >= 61 && weatherCode <= 67) deduction += 8; // Rain

        int score = Math.max(0, Math.min(100, 100 - deduction));
        String label = score >= 78 ? "GODE FORHOLD" :
                score >= 53 ? "SPILBART" :
                score >= 33 ? "KRÆVENDE" : "DÅRLIGE FORHOLD";

        String tip;
        if (weatherCode >= 95) tip = "Tordenrisiko: bliv inde";
        else if (dark) tip = "Mørkt: brug glow og lygte";
        else if (gust >= 35) tip = "Vindstød: vælg stabil disc";
        else if (wind >= 18) tip = "Modvind: kast mere stabilt";
        else if (rainNext3h >= 50) tip = "Regn mulig: tag håndklæde";
        else if (temp < 4) tip = "Koldt: hold kastehånden varm";
        else tip = "Rolige forhold til en runde";
        return new Assessment(score, label, tip);
    }

    public static String windFrom(double degrees) {
        String[] direction = {"N", "NØ", "Ø", "SØ", "S", "SV", "V", "NV"};
        int index = ((int) Math.floor((degrees + 22.5) / 45.0)) % 8;
        return direction[Math.max(0, index)];
    }
}
