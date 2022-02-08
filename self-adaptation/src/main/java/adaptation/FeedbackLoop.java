package adaptation;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import adaptation.Knowledge.WorkflowStep;
import adaptation.mape.Analyzer;
import adaptation.mape.Executor;
import adaptation.mape.Monitor;
import adaptation.mape.Planner;
import domain.Constants;
import domain.URLRequest;
import domain.experiment.Experiment;
import domain.experiment.TransitionRule;
import domain.experiment.UserProfile;
import domain.locust.LocustRunner;
import domain.setup.Setup;

public class FeedbackLoop {

    private Logger logger = Logger.getLogger(FeedbackLoop.class.getName());

    private Knowledge knowledge;

    private Monitor monitor;
    private Analyzer analyzer;
    private Planner planner;
    private Executor executor;

    private IProbe probe;
    private IEffector effector;

    private boolean isActive;

    private ScheduledExecutorService service;

    // The runners which emulate the specified User profile
    private List<LocustRunner> locustRunners;



    public FeedbackLoop() {
        this.knowledge = new Knowledge();

        this.monitor = new Monitor(this);
        this.analyzer = new Analyzer(this);
        this.planner = new Planner(this);
        this.executor = new Executor(this);

        this.probe = new Probe(this);
        this.effector = new Effector(this);

        this.isActive = false;
        this.service = Executors.newSingleThreadScheduledExecutor();
        this.locustRunners = new ArrayList<>();
    }


    public boolean isActive() {
        return this.isActive;
    }

    public void initializeFeedbackLoop(Setup setup, 
            List<Experiment<?>> experiments,
            List<TransitionRule> rules, 
            String initialExperiment) {
                
        // Handling the setup
        knowledge.setSetup(setup);
        knowledge.setABComponentName(setup.getABComponent().serviceName());

        int port = this.effector.deploySetup(setup.getName());
        knowledge.setExposedPort(port, setup.getABComponent().serviceName());
        knowledge.addToHistory(new WorkflowStep("Setup", setup.getName()));

        // Handling the experiments
        knowledge.addExperiments(experiments);
        var currentExperiment = experiments.stream()
            .filter(e -> e.getName().equals(initialExperiment))
            .findFirst().orElseThrow();
        knowledge.setCurrentExperiment(currentExperiment);

        // Handling the evolution rules
        knowledge.addTransitionRules(rules);
        
        
        // Start the experiment
        this.startExperiment();

        this.service.scheduleAtFixedRate(this::triggerAdaptationCycle, Constants.FEEDBACK_LOOP_POLLING_FREQUENCY, 
            Constants.FEEDBACK_LOOP_POLLING_FREQUENCY, TimeUnit.SECONDS);
        
        this.isActive = true;
    }


    public Optional<Setup> getDeployedSetup() {
        return this.knowledge.getSetup();
    }

    public Optional<Experiment<?>> getCurrentExperiment() {
        return this.knowledge.getCurrentExperiment();
    }

    public Optional<UserProfile> getCurrentUserProfile() {
        return this.knowledge.getCurrentExperiment().map(Experiment::getUserProfile);
    }


    public List<URLRequest> getRequestsA() {
        return this.knowledge.getRequestsA();
    }

    public List<URLRequest> getRequestsB() {
        return this.knowledge.getRequestsB();
    }

    /**
     * Prepare the feedback loop and the underlying MAPE components for a new experiment
     */
    public void startExperiment() {
        var currentExperiment = this.knowledge.getCurrentExperiment().get();
        this.knowledge.addToHistory(new WorkflowStep("Experiment", currentExperiment.getName()));
        this.knowledge.clearSamples();

        // Make sure that the locust process is not running anymore
        this.stopLocustRunners();

        // Stop the polling of the AB component
        this.monitor.stopPolling();


        // Set the AB routing of the AB-component according to the current experiment
        this.effector.setABRouting(this.knowledge.getABComponentName(), 
            currentExperiment.getABSetting().getWeightA(), 
            currentExperiment.getABSetting().getWeightB());


        // Clear the history in the AB component
        this.effector.clearABComponentHistory(this.knowledge.getABComponentName());
        
        // Restart the polling of the AB component
        this.monitor.startPolling();



        var profiles = currentExperiment.getUserProfile().getProfiles();
        var locustProperties = currentExperiment.getUserProfile().getExtraProperties();
        int totalAmountUsers = currentExperiment.getUserProfile().getNumberOfUsers();
        int weightA = currentExperiment.getABSetting().getWeightA();
        int middlePoint = (int) (weightA * totalAmountUsers / 100.0);
        int currentIndex = 1;


        for (var entry : profiles.entrySet()) {
            int nrUsersA = (int) Math.floor(entry.getValue() * (weightA / 100.0));

            this.locustRunners.add(new LocustRunner(entry.getKey(), nrUsersA, currentIndex, middlePoint, locustProperties));
            currentIndex += nrUsersA;
        }
        for (var entry : profiles.entrySet()) {
            int nrUsersB = (int) Math.ceil(entry.getValue() * ((100 - weightA) / 100.0));

            this.locustRunners.add(new LocustRunner(entry.getKey(), nrUsersB, currentIndex, middlePoint, locustProperties));
            currentIndex += nrUsersB;
        }

        // Start the running of the users testing the application
        this.locustRunners.forEach(r -> {
            try {
                r.startLocust();
            } catch (IOException e) {
                this.monitor.stopPolling();
                logger.severe(String.format("Failure to start the user profiles.\nException message (%s): %s", 
                    e.getClass().getName(), e.getMessage()));
            }
        });
    }


    public void stopLocustRunners() {
        this.locustRunners.stream()
            .filter(LocustRunner::isRunning)
            .forEach(LocustRunner::stopLocust);

        this.locustRunners.clear();
    }


    public void stopFeedbackLoop() {
        this.stopFeedbackLoop(false);
    }

    public void stopFeedbackLoop(boolean keepKnowledge) {
        this.service.shutdown();
        this.service = Executors.newSingleThreadScheduledExecutor();
        this.stopLocustRunners();
        this.monitor.stopPolling();
        this.isActive = false;
        
        knowledge.getSetup()
            .ifPresent(s -> this.effector.removeSetup(s.getName()));

        if (!keepKnowledge) {
            this.knowledge.reset();
        }

    }


    public Knowledge getKnowledge() {
        return this.knowledge;
    }

    public IProbe getProbe() {
        return this.probe;
    }

    public IEffector getEffector() {
        return this.effector;
    }


    public Optional<String> getPlotDataCurrentExperiment() {
        return this.getCurrentExperiment()
            .map(Experiment::getStatisticalTest)
            .map(t -> {
                var requestsA = this.knowledge.getRequestsA();
                var requestsB = this.knowledge.getRequestsB();

                return Optional.of(t.getMetric().getPlotData(requestsA, requestsB));
            })
            .orElse(Optional.empty());
    }


    public void triggerAdaptationCycle() {
        monitor.monitor();

        boolean shouldAdapt = analyzer.analyze();

        if (shouldAdapt) {
            // If the system should adapt the required amount of data samples is reached
            //  ---> stop the user profiles from running
            this.stopLocustRunners();

            planner.plan();
            executor.execute();
        }
    }
}
