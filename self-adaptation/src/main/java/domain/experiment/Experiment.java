package domain.experiment;

import java.util.List;

import domain.ABSetting;

public class Experiment<T> {
    
    private String name;

    private String nameVariantA;
    private String nameVariantB;

    private UserProfile userProfile;
    
    private ABSetting abSetting;
    
    private List<String> metrics;

    private StatisticalTest<T> statisticalTest;
    

    public Experiment(String name, String variantA, String variantB, ABSetting setting, 
            UserProfile userProfile, List<String> metrics, StatisticalTest<T> test) {
        this.name = name;
        this.nameVariantA = variantA;
        this.nameVariantB = variantB;
        this.userProfile = userProfile;
        this.abSetting = setting;
        this.metrics = metrics;
        this.statisticalTest = test;
    }

    public String getName() {
        return this.name;
    }

    public String getVariantA() {
        return this.nameVariantA;
    }

    public String getVariantB() {
        return this.nameVariantB;
    }

    public UserProfile getUserProfile() {
        return this.userProfile;
    }

    public ABSetting getABSetting() {
        return this.abSetting;
    }

    public List<String> getMetrics() {
        return this.metrics;
    }

    public StatisticalTest<T> getStatisticalTest() {
        return this.statisticalTest;
    }
}
