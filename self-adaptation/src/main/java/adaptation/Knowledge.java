package adaptation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import domain.URLRequest;
import domain.URLRequest.ABInstance;
import domain.experiment.Experiment;
import domain.experiment.StatisticalTest.StatisticalResult;
import domain.experiment.TransitionRule;
import domain.experiment.UserProfile;
import domain.setup.Setup;

public class Knowledge {

    // The data samples gathered from the AB component currently deployed in the managed system
    private Map<ABInstance, List<URLRequest>> samples;

    // The name of the deployed AB component in the managed system
    private String abComponentName;

    // The network ports exposed on the local machine for each AB component (AB component name -> network port)
    private Map<String, Integer> abComponentPort;

    // The setup that has to be executed before deploying the feedback loop
    private Setup setup;

    // The experiment that is currently being conducted
    private Experiment<?> currentExperiment;

    // A complete list of all experiments the feedback loop can (has) to go through
    private List<Experiment<?>> experiments;

    // The list of all transition rules to transition between different experiments
    private List<TransitionRule> transitionRules;


    // Temporary result of the statistical test conducted in the current experiment
    private StatisticalResult statisticalResult;

    // Temporary experiment stored by planner to be used by executor afterwards
    private String nextExperimentName;


    // Keep track of all the steps that were taken in the self-AB-test
    private List<WorkflowStep> history;
    


    public Knowledge() {
        this.reset();
    }

    public void setSetup(Setup setup) {
        this.setup = setup;
    }

    public int getABComponentPort(String abComponentName) {
        return this.abComponentPort.get(abComponentName);
    }

    public String getABComponentName() {
        return this.abComponentName;
    }


    public Optional<Setup> getSetup() {
        return Optional.ofNullable(setup);
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


    public void addTransitionRules(Collection<TransitionRule> rules) {
        this.transitionRules.addAll(rules);
    }


    public List<TransitionRule> getTransitionRules() {
        return this.transitionRules;
    }


    public void setNextExperimentName(String name) {
        this.nextExperimentName = name;
    }

    public Optional<Experiment<?>> getNextExperiment() {
        if (this.nextExperimentName == null) {
            return Optional.empty();
        }

        return this.experiments.stream()
            .filter(e -> e.getName().equals(this.nextExperimentName))
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




    // Reset the knowledge to the default state
    public void reset() {
        this.samples = Collections.synchronizedMap(new HashMap<>());
        this.samples.put(ABInstance.A, Collections.synchronizedList(new ArrayList<>()));
        this.samples.put(ABInstance.B, Collections.synchronizedList(new ArrayList<>()));

        this.abComponentName = null;
        this.abComponentPort = new HashMap<>();

        this.setup = null;
        this.currentExperiment = null;
        this.nextExperimentName = null;
        this.statisticalResult = null;
        this.experiments = new ArrayList<>();

        this.transitionRules = new ArrayList<>();
        this.history = new ArrayList<>();
    }


    public record WorkflowStep(String type, String name) {}
}
