package dk.discvejr.widget;

/** Weather direction is where wind comes FROM; throw bearing is where the disc goes TO. */
public final class WindAnalysis {
    private WindAnalysis() {}

    public static int normalize(int degrees) { return ((degrees % 360) + 360) % 360; }
    public static String direction(double degrees) {
        String[] labels = {"N", "NØ", "Ø", "SØ", "S", "SV", "V", "NV"};
        int index = ((int) Math.floor(((degrees % 360 + 360) % 360 + 22.5) / 45)) % 8;
        return labels[index];
    }
    public static final class Result {
        public final double headwind; // Positive: headwind, negative: tailwind
        public final double crossFromRight; // Positive: from right, negative: from left
        public final String label;
        public final String guidance;
        Result(double head, double cross, String label, String guidance) {
            this.headwind = head;
            this.crossFromRight = cross;
            this.label = label;
            this.guidance = guidance;
        }
    }
    public static Result analyze(double windFrom, double speedKmh, int throwTowards) {
        double delta = Math.toRadians(windFrom - normalize(throwTowards));
        double head = speedKmh * Math.cos(delta);
        double cross = speedKmh * Math.sin(delta);
        if (speedKmh < 3)
            return new Result(head, cross, "Næsten vindstille", "Vinden påvirker kastet minimalt.");
        boolean appreciableHead = Math.abs(head) >= Math.max(3, speedKmh * 0.35);
        boolean appreciableCross = Math.abs(cross) >= Math.max(3, speedKmh * 0.35);
        String label = appreciableHead ? (head > 0 ? "Modvind" : "Medvind") : "";
        if (appreciableCross) {
            if (!label.isEmpty()) label += " + ";
            label += cross > 0 ? "sidevind fra højre" : "sidevind fra venstre";
        }
        if (label.isEmpty()) label = "Skrå vind";
        String guidance;
        if (head >= 12) guidance = "Modvind: højere risiko for turn. Overvej mere stabil disc og lavere kast.";
        else if (head >= 6) guidance = "Modvind: discen kan turne mere; hold styr på næsevinklen.";
        else if (head <= -12) guidance = "Medvind: mindre relativ lufthastighed; ofte mindre turn og mere fade.";
        else if (head <= -6) guidance = "Medvind: regn med mindre turn og ændret glidefase.";
        else if (Math.abs(cross) >= 9) guidance = "Sidevind: discens hyzer/anhyzer kan løftes eller presses ned.";
        else guidance = "Let vindpåvirkning. Kastets vinkel og discvalg betyder stadig mest.";
        return new Result(head, cross, label, guidance);
    }
}
