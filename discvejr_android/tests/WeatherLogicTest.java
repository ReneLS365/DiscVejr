package dk.discvejr.widget;

/** Offline tests; run with javac/java on CI. */
public final class WeatherLogicTest {
    private static int checks=0;
    private static void check(boolean ok,String message) {
        checks++;
        if(!ok) throw new AssertionError(message);
    }
    private static void approx(double actual,double expected,double epsilon,String message) {
        check(Math.abs(actual-expected)<=epsilon,message+": "+actual+" != "+expected);
    }
    public static void main(String[] args) {
        WeatherLogic.Assessment calm=WeatherLogic.assess(10,17,5,15,3,false);
        WeatherLogic.Assessment cold=WeatherLogic.assess(10,17,5,2,3,false);
        WeatherLogic.Assessment windy=WeatherLogic.assess(32,48,5,15,3,false);
        WeatherLogic.Assessment storm=WeatherLogic.assess(30,55,80,7,95,false);
        WeatherLogic.Assessment glow=WeatherLogic.assess(10,17,5,15,3,true);
        check(calm.score>cold.score,"Cold lowers score");
        check(calm.score>windy.score,"Wind lowers score");
        check(storm.score==0 && storm.putting==0,"Thunder stops play");
        check(glow.score==calm.score && glow.tip.contains("glow"),"Glow not falsely unsafe");
        check(WeatherLogic.windFrom(0).equals("N"),"North");
        check(WeatherLogic.windFrom(225).equals("SV"),"Southwest");
        check(WindAnalysis.direction(-45).equals("NV"),"Negative angle");
        check(WindAnalysis.normalize(450)==90,"Normalizes heading");
        WindAnalysis.Result head=WindAnalysis.analyze(90,20,90);
        approx(head.headwind,20,.001,"Headwind");
        approx(head.crossFromRight,0,.001,"Headwind cross");
        WindAnalysis.Result tail=WindAnalysis.analyze(270,20,90);
        approx(tail.headwind,-20,.001,"Tailwind");
        WindAnalysis.Result left=WindAnalysis.analyze(0,20,90);
        approx(left.crossFromRight,-20,.001,"Wind from left");
        WindAnalysis.Result right=WindAnalysis.analyze(180,20,90);
        approx(right.crossFromRight,20,.001,"Wind from right");
        double density=WeatherLogic.airDensity(20,1013.25,50);
        check(density>1.19 && density<1.22,"Moist air density in kg/m3");
        check(Double.isNaN(WeatherLogic.airDensity(20,0,50)),"Invalid pressure");
        System.out.println(checks+" checks OK; density="+density);
    }
}
