package dashboard.model;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import domain.experiment.Experiment;
import domain.experiment.TransitionRule;
import domain.experiment.UserProfile;
import domain.pipeline.Pipeline;
import domain.setup.Setup;
import domain.split.PopulationSplit;

public class ABRepository {
    
    private Map<String, Setup> setupCollection;
    private Map<String, Experiment<?>> experimentCollection;
    private Map<String, TransitionRule> transitionRuleCollection;
    private Map<String, UserProfile> userProfileCollection;
    private Map<String, PopulationSplit> populationSplitCollection;
    private Map<String, Pipeline> pipelineCollection;


    public ABRepository() {
        this.setupCollection = Collections.synchronizedMap(new HashMap<>());
        this.experimentCollection = Collections.synchronizedMap(new HashMap<>());
        this.transitionRuleCollection = Collections.synchronizedMap(new HashMap<>());
        this.userProfileCollection = Collections.synchronizedMap(new HashMap<>());
        this.populationSplitCollection = Collections.synchronizedMap(new HashMap<>());
        this.pipelineCollection = Collections.synchronizedMap(new HashMap<>());
    }


    public void addSetup(Setup setup) {
        this.setupCollection.put(setup.getName(), setup);
    }

    public Collection<Setup> getAllSetups() {
        return this.setupCollection.values();
    }

    public boolean hasSetup(String name) {
        return this.setupCollection.containsKey(name);
    }

    public Setup getSetup(String name) {
        return this.setupCollection.get(name);
    }
    


    public void addExperiment(Experiment<?> experiment) {
        this.experimentCollection.put(experiment.getName(), experiment);
    }

    public Experiment<?> getExperiment(String name) {
        return this.experimentCollection.get(name);
    }

    public Collection<Experiment<?>> getAllExperiments() {
        return this.experimentCollection.values();
    }



    public void addTransitionRule(TransitionRule rule) {
        this.transitionRuleCollection.put(rule.getName(), rule);
    }

    public TransitionRule getTransitionRule(String name) {
        return this.transitionRuleCollection.get(name);
    }

    public Collection<TransitionRule> getAllTransitionRules() {
        return this.transitionRuleCollection.values();
    }



    public void addUserProfile(UserProfile userProfile) {
        this.userProfileCollection.put(userProfile.getName(), userProfile);
    }

    public UserProfile getUserProfile(String name) {
        return this.userProfileCollection.get(name);
    }

    public Collection<UserProfile> getAllUserProfiles() {
        return this.userProfileCollection.values();
    }


    public void addPopulationSplit(PopulationSplit split) {
        this.populationSplitCollection.put(split.getName(), split);
    }

    public PopulationSplit getPopulationSplit(String name) {
        return this.populationSplitCollection.get(name);
    }
    
    public Collection<PopulationSplit> getAllPopulationSplits() {
        return this.populationSplitCollection.values();
    }


    public void addPipeline(Pipeline pipeline) {
        this.pipelineCollection.put(pipeline.getName(), pipeline);
    }

    public Pipeline getPipeline(String name) {
        return this.pipelineCollection.get(name);
    }

    public Collection<Pipeline> getAllPipelines() {
        return this.pipelineCollection.values();
    }
}
