package domain.experiment;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

import adaptation.FeedbackLoop;
import domain.ABComponent;
import domain.ABSetting;
import domain.command.Command;
import domain.experiment.statistic.StatisticalTest;
import domain.setup.Setup;

public class Experiment<T> implements ABComponent {
    
    private String name;

    private String setup;

    @JsonIgnore
    private Setup deployedSetup;

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
        this.deployedSetup = null;
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


    @JsonIgnore
    public List<Command> getStartCommands(Setup setup, int networkPort) {
        this.deployedSetup = setup;
        return this.deployedSetup.generateCommands(networkPort);
    }

    @Override
    @JsonIgnore
    public List<Command> getStopCommands() {
        if (this.deployedSetup != null) {
            return this.deployedSetup.generateReverseCommandsWithReboot();
        }

        return List.of();
    }

    @Override
    public void handleComponentInPipeline(FeedbackLoop feedbackLoop) {
        feedbackLoop.handleComponent(this);
    }
}
