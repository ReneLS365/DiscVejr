package dk.discvejr.widget;

/** Disc-golf heuristics, not measured aerodynamic performance or safety guarantees. */
public final class WeatherLogic {
    private WeatherLogic() {}
    public static final class Assessment {
        public final int score, drive, approach, putting;
        public final String label, tip, safety;
        Assessment(int score, int drive, int approach, int putting,
                   String label, String tip, String safety) {
            this.score=score; this.drive=drive; this.approach=approach; this.putting=putting;
            this.label=label; this.tip=tip; this.safety=safety;
        }
    }
    private static int bounded(int x) { return Math.max(0, Math.min(100, x)); }
    public static Assessment assess(double wind, double gust, int rainNext3h,
                                    double temp, int weatherCode, boolean dark) {
        int windPenalty = wind >= 40 ? 42 : wind >= 30 ? 26 : wind >= 20 ? 13 : wind >= 13 ? 5 : 0;
        int gustPenalty = gust >= 60 ? 30 : gust >= 45 ? 21 : gust >= 33 ? 12 : gust >= 24 ? 5 : 0;
        int rainPenalty = rainNext3h >= 75 ? 30 : rainNext3h >= 50 ? 18 : rainNext3h >= 30 ? 8 : 0;
        int tempPenalty = temp < 0 ? 20 : temp < 5 ? 10 : temp < 10 ? 4 : 0;
        int snowPenalty = weatherCode >= 71 && weatherCode <= 86 ? 22 : 0;
        int rainActualPenalty = weatherCode >= 61 && weatherCode <= 67 ? 8 : 0;
        boolean thunder = weatherCode >= 95;
        int drive = bounded(100 - windPenalty - gustPenalty - rainPenalty/2 - tempPenalty
                - snowPenalty - rainActualPenalty);
        int approach = bounded(100 - windPenalty/2 - gustPenalty - rainPenalty - tempPenalty
                - snowPenalty - rainActualPenalty);
        int putting = bounded(100 - windPenalty - gustPenalty*2 - rainPenalty/2 - tempPenalty
                - snowPenalty - rainActualPenalty);
        if (thunder) { drive=0; approach=0; putting=0; }
        int score = (drive + approach + putting) / 3;
        String label = thunder ? "STOP · TORDEN" : score >= 78 ? "GODE FORHOLD" :
                score >= 53 ? "SPILBART" : score >= 33 ? "KRÆVENDE" : "DÅRLIGE FORHOLD";
        String safety = thunder ? "Torden: forlad banen og søg sikker bygning." :
                (gust >= 60 || wind >= 45 ? "Kraftig vind: pas på faldende grene." : "");
        String tip;
        if (thunder) tip = "Torden: spil ikke";
        else if (dark) tip = "Mørkt: brug glow og lygte";
        else if (gust >= 35) tip = "Vindstød: prioriter kontrol";
        else if (wind >= 18) tip = "Vinden påvirker discens turn og fade";
        else if (rainNext3h >= 50) tip = "Regn mulig: tag håndklæde";
        else if (temp < 4) tip = "Koldt: hold greb og fingre varme";
        else tip = "Rolige forhold til en runde";
        return new Assessment(score,drive,approach,putting,label,tip,safety);
    }
    public static Assessment assess(WeatherData d) {
        Assessment base = assess(d.wind,d.gust,d.rainMax3h,d.temperature,d.weatherCode,d.dark);
        int grip = d.rainfallNext3h >= 2 ? 10 : d.rainfallNext3h >= .5 ? 5 : 0;
        int sight = d.visibility > 0 && d.visibility < 1000 ? 25 : d.visibility > 0 && d.visibility < 3000 ? 10 : 0;
        int drive=bounded(base.drive - grip - sight);
        int approach=bounded(base.approach - grip - sight);
        int putting=bounded(base.putting - grip - sight);
        int score=(drive+approach+putting)/3;
        if (d.weatherCode >= 95) score=drive=approach=putting=0;
        String label=d.weatherCode >= 95 ? "STOP · TORDEN" : score>=78 ? "GODE FORHOLD" :
                score>=53 ? "SPILBART" : score>=33 ? "KRÆVENDE" : "DÅRLIGE FORHOLD";
        return new Assessment(score,drive,approach,putting,label,base.tip,base.safety);
    }
    /** Approximate moist-air density from pressure at the surface, temperature and RH. */
    public static double airDensity(double celsius, double pressureHpa, double humidityPercent) {
        if (pressureHpa < 500 || pressureHpa > 1200 || celsius < -80 || celsius > 70) return Double.NaN;
        double kelvin = celsius + 273.15;
        double saturatedPa = 611.2 * Math.exp(17.67*celsius / (celsius+243.5));
        double vaporPa = Math.max(0, Math.min(100, humidityPercent))/100d * saturatedPa;
        double totalPa = pressureHpa*100;
        return (totalPa-vaporPa)/(287.05*kelvin) + vaporPa/(461.495*kelvin);
    }
    public static String windFrom(double degrees) { return WindAnalysis.direction(degrees); }
}
