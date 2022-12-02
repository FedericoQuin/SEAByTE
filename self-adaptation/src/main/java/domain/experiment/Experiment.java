package domain.experiment;

import java.util.List;

import domain.ABSetting;

public class Experiment<T> {
    
    private String name;

    private String setup;

    private UserProfile userProfile;
    
    private ABSetting abSetting;
    
    private List<String> metrics;

    private StatisticalTest<T> statisticalTest;
    

    public Experiment(String name, String setup, ABSetting setting, 
            UserProfile userProfile, List<String> metrics, StatisticalTest<T> test) {
        this.name = name;
        this.setup = setup;
        this.userProfile = userProfile;
        this.abSetting = setting;
        this.metrics = metrics;
        this.statisticalTest = test;
    }

    public String getName() {
        return this.name;
    }

    public String getSetup() {
        return this.setup;
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
