package util;

public class Weight {
    private double value;
    private static final int BASE = 100;


    public static Weight fromPercentage(double value) {
        return new Weight(value);
    }

    public static Weight fromFraction(double value) {
        return new Weight(value * Weight.BASE);
    }

    private Weight(double weight) {
        this.value = weight;
    }



    public double getWeightBase100() {
        return this.value;
    }

    public double getWeightBase1() {
        return this.value / Weight.BASE;
    }
}
