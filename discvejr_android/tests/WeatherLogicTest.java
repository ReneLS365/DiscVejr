package dk.discvejr.widget;

/** Run with javac/java, no Android runtime needed. */
public class WeatherLogicTest {
    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
    public static void main(String[] args) {
        WeatherLogic.Assessment calm = WeatherLogic.assess(10, 17, 5, 15, 3, false);
        WeatherLogic.Assessment cold = WeatherLogic.assess(10, 17, 5, 2, 3, false);
        WeatherLogic.Assessment windy = WeatherLogic.assess(32, 48, 5, 15, 3, false);
        WeatherLogic.Assessment storm = WeatherLogic.assess(30, 55, 80, 7, 95, false);
        WeatherLogic.Assessment glow = WeatherLogic.assess(10, 17, 5, 15, 3, true);
        check(calm.score > cold.score, "Temperature adjustment");
        check(calm.score > windy.score, "Wind adjustment");
        check(storm.score < windy.score, "Thunder / storm adjustment");
        check(glow.score == calm.score && glow.tip.contains("glow"), "Glow support");
        check(WeatherLogic.windFrom(0).equals("N"), "Wind direction N");
        check(WeatherLogic.windFrom(225).equals("SV"), "Wind direction SV");
        System.out.println("6 checks OK; calm=" + calm.score + ", wind=" + windy.score + ", storm=" + storm.score);
    }
}
