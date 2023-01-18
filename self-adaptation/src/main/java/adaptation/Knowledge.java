package adaptation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import domain.ABComponent;
import domain.URLRequest;
import domain.URLRequest.ABInstance;
import domain.experiment.Experiment;
import domain.experiment.TransitionRule;
import domain.experiment.UserProfile;
import domain.experiment.statistic.StatisticalTest.StatisticalResult;
import domain.pipeline.Pipeline;
import domain.setup.Setup;
import domain.split.PopulationSplit;

public class Knowledge {

    // The data samples gathered from the AB component currently deployed in the managed system
    private Map<ABInstance, List<URLRequest>> samples;

    // The name of the deployed AB component in the managed system
    private String abComponentName;

    // The network ports exposed on the local machine for each AB component (AB component name -> network port)
    private Map<String, Integer> abComponentPort;


    // The current setup that is deployed in the system
    private Setup currentSetup;

    // The setups available to be executed before starting an experiment
    private Set<Setup> setups;

    // The experiment that is currently being conducted
    private Experiment<?> currentExperiment;

    // A complete set of all experiments the feedback loop can (has) to go through
    private Set<Experiment<?>> experiments;

    // The set of all transition rules to transition between different experiments
    private Set<TransitionRule> transitionRules;


    // The set of all population splits
    private Set<PopulationSplit> populationSplits;


    // The set of pipelines
    private Set<Pipeline> pipelines;


    // Temporary result of the statistical test conducted in the current experiment
    private StatisticalResult statisticalResult;

    // Temporary component stored by planner to be used by executor afterwards
    private String nextComponentName;


    // Keep track of all the steps that were taken in the self-AB-test
    private List<WorkflowStep> history;
    


    public Knowledge() {
        this.reset();
    }

    public void setCurrentSetup(Setup setup) {
        this.currentSetup = setup;
    }

    public Optional<Setup> getCurrentSetup() {
        return Optional.ofNullable(currentSetup);
    }

    public void addSetups(Collection<Setup> setups) {
        this.setups.addAll(setups);
    }

    public Collection<Setup> getSetups() {
        return this.setups;
    }

    public int getABComponentPort(String abComponentName) {
        return this.abComponentPort.get(abComponentName);
    }

    public String getABComponentName() {
        return this.abComponentName;
    }

    public void removeExposedPort(String abComponentName) {
        this.abComponentPort.remove(abComponentName);
    }



    public void addRequests(Collection<URLRequest> requests, ABInstance instance) {
        this.samples.get(instance).addAll(requests);
    }

    public List<URLRequest> getRequestsA() {
        return this.samples.get(ABInstance.A);
    }

    public List<URLRequest> getRequestsB() {
        return this.samples.get(ABInstance.B);
    }

    public void clearSamples() {
        this.samples.put(ABInstance.A, Collections.synchronizedList(new ArrayList<>()));
        this.samples.put(ABInstance.B, Collections.synchronizedList(new ArrayList<>()));
    }


    public void setABComponentName(String name) {
        this.abComponentName = name;
    }

    public void setExposedPort(int port, String abName) {
        this.abComponentPort.put(abName, port);
    }


    public void setCurrentExperiment(Experiment<?> experiment) {
        this.currentExperiment = experiment;
    }

    public void addExperiments(Collection<Experiment<?>> experiments) {
        this.experiments.addAll(experiments);
    }

    public Optional<Experiment<?>> getCurrentExperiment() {
        return Optional.ofNullable(this.currentExperiment);
    }

    public Collection<Experiment<?>> getExperiments() {
        return this.experiments;
    }

    public Experiment<?> getExperiment(String name) {
        return this.experiments.stream().filter(e -> e.getName().equals(name)).findFirst().orElse(null);
    }


    public void addTransitionRules(Collection<TransitionRule> rules) {
        this.transitionRules.addAll(rules);
    }


    public Set<TransitionRule> getTransitionRules() {
        return this.transitionRules;
    }

    public TransitionRule getTransitionRule(String name) {
        return this.transitionRules.stream().filter(r -> r.getName().equals(name)).findFirst().orElse(null);
    }


    public void setNextComponentName(String name) {
        this.nextComponentName = name;
    }

    public Optional<ABComponent> getNextComponent() {
        if (this.nextComponentName == null) {
            return Optional.empty();
        }

        return Stream.concat(this.experiments.stream(), this.populationSplits.stream())
            .filter(e -> e.getName().equals(this.nextComponentName))
            .findFirst();
    }


    public void setStatisticalResult(StatisticalResult result) {
        this.statisticalResult = result;
    }

    public void clearStatisticalResult() {
        this.statisticalResult = null;
    }

    public StatisticalResult getStatisticalResult() {
        return this.statisticalResult;
    }


    public UserProfile getUserProfile() {
        return this.currentExperiment.getUserProfile();
    }


    public void addToHistory(WorkflowStep step) {
        this.history.add(step);
    }

    public List<WorkflowStep> getHistory() {
        return this.history;
    }

    public void addPopulationSplit(PopulationSplit populationSplit) {
        this.populationSplits.add(populationSplit);
    }

    public void addPopulationSplits(Collection<PopulationSplit> populationSplits) {
        populationSplits.forEach(this::addPopulationSplit);
    }

    public PopulationSplit getPopulationSplit(String name) {
        return this.populationSplits.stream().filter(s -> s.getName().equals(name)).findFirst().orElseThrow();
    }



    public void addPipeline(Pipeline pipeline) {
        this.pipelines.add(pipeline);
    }

    public void addPipelines(Collection<Pipeline> pipelines) {
        pipelines.forEach(this::addPipeline);
    }

    public Pipeline getPipeline(String name) {
        return this.pipelines.stream().filter(p -> p.getName().equals(name)).findFirst().orElse(null);
    }


    // Reset the knowledge to the default state
    public void reset() {
        this.samples = Collections.synchronizedMap(new HashMap<>());
        this.samples.put(ABInstance.A, Collections.synchronizedList(new ArrayList<>()));
        this.samples.put(ABInstance.B, Collections.synchronizedList(new ArrayList<>()));

        this.abComponentName = null;
        this.abComponentPort = new HashMap<>();

        this.currentSetup = null;
        this.currentExperiment = null;
        this.nextComponentName = null;
        this.statisticalResult = null;
        this.setups = new HashSet<>();
        this.experiments = new HashSet<>();

        this.transitionRules = new HashSet<>();
        this.populationSplits = new HashSet<>();
        this.pipelines = new HashSet<>();
        this.history = new ArrayList<>();
    }



    public Optional<ABComponent> getComponent(String name) {
        return Stream.concat(this.experiments.stream(), this.populationSplits.stream())
            .filter(c -> c.getName().equals(name))
            .findFirst();
    }


    public enum WorkflowStepType {
        Setup("Setup"), 
        Experiment("Experiment"), 
        Rule("Transition rule"), 
        Split("Population split");

        private String value;

        private WorkflowStepType(String value) {
            this.value = value;
        }

        public String getValue() {
            return this.value;
        }
    }
    public record WorkflowStep(WorkflowStepType type, String name) {}
}
