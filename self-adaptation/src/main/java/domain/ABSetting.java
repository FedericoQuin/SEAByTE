package domain;

public class ABSetting {
    private int weightA;
    private int weightB;


    public ABSetting(int w1, int w2) {
        this.weightA = w1;
        this.weightB = w2;
    }

    public int getWeightA() {
        return this.weightA;
    }

    public int getWeightB() {
        return this.weightB;
    }
}
