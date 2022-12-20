package adaptation;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import adaptation.Knowledge.WorkflowStep;
import adaptation.mape.Analyzer;
import adaptation.mape.Executor;
import adaptation.mape.Monitor;
import adaptation.mape.Planner;
import dashboard.model.ABRepository;
import domain.ABComponent;
import domain.Constants;
import domain.URLRequest;
import domain.experiment.Experiment;
import domain.experiment.TransitionRule;
import domain.experiment.UserProfile;
import domain.locust.LocustRunner;
import domain.pipeline.Pipeline;
import domain.setup.Setup;
import domain.split.PopulationSplit;

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

    public void initializeFeedbackLoop(Pipeline pipeline, ABRepository repository) {
        
        Set<Experiment<?>> experiments = pipeline.getExperiments().stream()
            .map(e -> repository.getExperiment(e))
            .collect(Collectors.toSet());

        this.initializeFeedbackLoop(experiments.stream()
                .map(Experiment::getSetup)
                .map(s -> repository.getSetup(s))
                .collect(Collectors.toSet()), 
            experiments, 
            pipeline.getTransitionRules().stream()
                .map(r -> repository.getTransitionRule(r))
                .collect(Collectors.toSet()), 
            pipeline.getPopulationSplits().stream()
                .map(s -> repository.getPopulationSplit(s))
                .collect(Collectors.toSet()), 
            pipeline.getPipelines().stream()
                .map(p -> repository.getPipeline(p))
                .collect(Collectors.toSet()), 
            pipeline.getStartingComponent());
    }

    public void initializeFeedbackLoop(
            Set<Setup> setups, 
            Set<Experiment<?>> experiments, 
            Set<TransitionRule> rules, 
            Set<PopulationSplit> populationSplits,
            Set<Pipeline> pipelines, 
            String startingComponent) {
                
        
        knowledge.addSetups(setups);
        knowledge.addExperiments(experiments);
        knowledge.addTransitionRules(rules);
        knowledge.addPopulationSplits(populationSplits);
        knowledge.addPipelines(pipelines);
        
        
        var currentComponent = this.knowledge.getComponent(startingComponent);
        currentComponent.handleComponentInPipeline(this);
    }

    
    public void handleComponent(Experiment<?> experiment) {

        // When this method is called, no previous experiment has been started yet
        // TODO verify this is still true at the end of the implementation
        this.knowledge.setCurrentExperiment(experiment);

        // Start the experiment
        this.startExperiment();

        // Do not deploy new setup if the previous component is an experiment with the same setup
        // if (this.currentComponent instanceof Experiment && 
        //         ((Experiment<?>) (this.currentComponent)).getSetup().equals(experiment.getSetup())) {
        //     // Do nothing
        // } else {
        //     int networkPort = Networking.generateAvailableNetworkPort();
        //     var commands = experiment.getStartCommands(this.pipelineComponents.setups.stream()
        //         .filter(s -> s.getName().equals(experiment.getSetup())).findFirst().orElseThrow(), networkPort);
        
        //     PipelineRunner.executeCommands(commands);
        // }
    }



    public void handleComponent(ABComponent component) {
        throw new RuntimeException(String.format("'handleComponent' method not implemented yet for component of type %s", 
            component.getClass().getName()));
    }



    
    public void setActiveExperiment(Experiment<?> experiment) {
        this.knowledge.setCurrentExperiment(experiment);
    }


    public void startExperiment(Experiment<?> experiment) {
        this.startExperiment(experiment, false);    
    }


    public void startExperiment(Experiment<?> experiment, boolean blockThread) {
        // Start the experiment
        this.startExperiment();

        this.service.scheduleAtFixedRate(this::triggerAdaptationCycle, Constants.FEEDBACK_LOOP_POLLING_FREQUENCY, 
            Constants.FEEDBACK_LOOP_POLLING_FREQUENCY, TimeUnit.SECONDS);
        
        this.isActive = true;

        if (blockThread) {
            try {
                this.service.awaitTermination(Long.MAX_VALUE, TimeUnit.SECONDS);
            } catch (InterruptedException e) {} // TODO handle this exception?
        }
    }



    private void startSetup(Setup setup) {
        // Handling the setup
        knowledge.setCurrentSetup(setup);
        knowledge.setABComponentName(setup.getABComponent().serviceName());

        int port = this.effector.deploySetup(setup.getName());
        knowledge.setExposedPort(port, setup.getABComponent().serviceName());
        knowledge.addToHistory(new WorkflowStep("Setup", setup.getName()));
    }

    private void stopSetup() {
        this.knowledge.getCurrentExperiment().ifPresent(s -> {
            this.effector.removeSetup(s.getName());
            this.knowledge.setCurrentSetup(null);
            this.knowledge.setABComponentName(null);
            this.knowledge.removeExposedPort(s.getName());
        });
    }

    /**
     * Prepare the feedback loop and the underlying MAPE components for a new experiment
     */
    public void startExperiment() {
        var currentExperiment = this.knowledge.getCurrentExperiment().get();

        Setup experimentSetup = this.knowledge.getSetups().stream()
            .filter(s -> s.getName().equals(currentExperiment.getSetup()))
            .findFirst().orElseThrow();


        this.knowledge.getCurrentSetup()
            // .filter(s -> s.equals(experimentSetup))
            .ifPresentOrElse(s -> {
                // A setup is deployed, check if it matches the one we need for the new experiment
                if (!s.equals(experimentSetup)) {
                    // Remove the setup and deploy the new one
                    this.stopSetup();
                    this.startSetup(experimentSetup);
                }
                // Otherwise no action needs to be taken - correct setup is already deployed
            }, () -> {
                // No setup deployed yet --> deploy the new one
                this.startSetup(experimentSetup);
            });



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



        var locustUsers = currentExperiment.getUserProfile().getLocustUsers();
        int totalAmountUsers = currentExperiment.getUserProfile().getNumberOfUsers();
        int weightA = currentExperiment.getABSetting().getWeightA();
        int middlePoint = (int) (weightA * totalAmountUsers / 100.0);
        int currentIndex = 1;


        for (var user : locustUsers) {
            int nrUsersA = (int) Math.floor(user.numberOfUsers() * (weightA / 100.0));

            this.locustRunners.add(new LocustRunner(user.name(), nrUsersA, currentIndex, middlePoint, 
                    user.extraProperties()));
            currentIndex += nrUsersA;
        }
        for (var user : locustUsers) {
            int nrUsersB = (int) Math.ceil(user.numberOfUsers() * ((100 - weightA) / 100.0));

            this.locustRunners.add(new LocustRunner(user.name(), nrUsersB, currentIndex, middlePoint, 
                    user.extraProperties()));
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
        
        this.service.scheduleAtFixedRate(this::triggerAdaptationCycle, Constants.FEEDBACK_LOOP_POLLING_FREQUENCY, 
            Constants.FEEDBACK_LOOP_POLLING_FREQUENCY, TimeUnit.SECONDS);
        
        this.isActive = true;
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
        
        this.stopSetup();

        if (!keepKnowledge) {
            this.knowledge.reset();
        }

    }



    public Optional<Setup> getDeployedSetup() {
        return this.knowledge.getCurrentSetup();
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







    public record PipelineComponents(
        Set<Setup> setups,
        Set<Experiment<?>> experiments,
        Set<TransitionRule> rules,
        Set<PopulationSplit> populationSplits,
        Set<Pipeline> pipelines
    ) {
        public ABComponent getComponent(String name) {
            return Stream.concat(experiments.stream(), populationSplits.stream())
                .filter(c -> c.getName().equals(name))
                .findFirst().orElseThrow();
        }
    } 
}
