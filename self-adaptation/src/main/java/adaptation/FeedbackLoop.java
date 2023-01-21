package adaptation;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import adaptation.Knowledge.WorkflowStep;
import adaptation.Knowledge.WorkflowStepType;
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
import domain.experiment.UserProfile.ABRoutingMode;
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

    private boolean blockDuringExecution;

    private final int id;
    private static int nextId = 0;



    public FeedbackLoop() {
        this(false);
    }

    public FeedbackLoop(boolean blockDuringExecution) {
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

        this.blockDuringExecution = blockDuringExecution;
        this.id = FeedbackLoop.nextId++;
    }


    public boolean isActive() {
        return this.isActive;
    }

    public void initializeFeedbackLoop(Pipeline pipeline, ABRepository repository) {
        Set<Experiment<?>> experiments = pipeline.getExperiments().stream()
            .map(e -> repository.getExperiment(e))
            .collect(Collectors.toSet());

        this.initializeFeedbackLoop(
            experiments.stream()
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
            pipeline.getStartingComponent()
        );
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

        var currentComponent = this.knowledge.getComponent(startingComponent).get();
        currentComponent.handleComponentInPipeline(this);

        this.service.scheduleAtFixedRate(this::triggerAdaptationCycle, Constants.FEEDBACK_LOOP_POLLING_FREQUENCY, 
            Constants.FEEDBACK_LOOP_POLLING_FREQUENCY, TimeUnit.SECONDS);
        
        if (this.blockDuringExecution) {
            try {
                this.service.awaitTermination(Long.MAX_VALUE, TimeUnit.SECONDS);
            } catch (InterruptedException e) {} // TODO handle this exception?
        }
    }

    

    public void handleComponent(Experiment<?> experiment) {
        this.knowledge.clearSamples();

        // Make sure that the locust process is not running anymore
        this.stopLocustRunners();

        // Stop the polling of the AB component
        this.monitor.stopPolling();


        // Start the experiment
        this.knowledge.setCurrentExperiment(experiment);

        Setup experimentSetup = this.knowledge.getSetups().stream()
            .filter(s -> s.getName().equals(experiment.getSetup()))
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


        // Set the AB routing of the AB-component according to the current experiment
        this.effector.setABRouting(this.knowledge.getABComponentName(), 
            experiment.getABSetting().getWeightA(), 
            experiment.getABSetting().getWeightB());


        // Clear the history in the AB component
        this.effector.clearABComponentHistory(this.knowledge.getABComponentName());
        
        this.knowledge.addToHistory(new WorkflowStep(WorkflowStepType.Experiment, experiment.getName()));

        // Restart the polling of the AB component
        this.monitor.startPolling();



        var locustUsers = experiment.getUserProfile().getLocustUsers();
        int totalAmountUsers = experiment.getUserProfile().getNumberOfUsers();
        int weightA = experiment.getABSetting().getWeightA();
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
                r.startLocust(this.knowledge.getABComponentPort(this.knowledge.getABComponentName()));
            } catch (IOException e) {
                this.monitor.stopPolling();
                logger.severe(String.format("Failure to start the user profiles.\nException message (%s): %s", 
                    e.getClass().getName(), e.getMessage()));
            }
        });
        
        this.isActive = true;
    }


    public void handleComponent(PopulationSplit split) {
        this.monitor.stopPolling();
        this.isActive = false;
        
        // Stop any currently deployed setup before starting the pipelines
        this.knowledge.getCurrentSetup().ifPresent(s -> this.stopSetup());
        this.knowledge.setCurrentExperiment(null);

        // Start the split component that will be used by the sub-pipelines
        this.effector.deploySplitComponent(split.getName());
        this.knowledge.addToHistory(new WorkflowStep(WorkflowStepType.Split, split.getName()));

        Thread t1 = this.generatePipelineThread(split.getSplitComponent().serviceName(), split.getPipelineName1(), split.getTargetValue1());
        Thread t2 = this.generatePipelineThread(split.getSplitComponent().serviceName(), split.getPipelineName2(), split.getTargetValue2());

        this.logger.info("Created both threads for the split component.");

        t1.start();
        t2.start();

        this.logger.info("Starting both threads.");

        try {
            t1.join();
            t2.join();
        } catch (InterruptedException e) {
            // Should not get here, otherwise problem for cleaning up
            Logger.getLogger(Planner.class.getName()).severe("Thread interupted during parallel pipeline execution.");
            throw new RuntimeException("Sub-pipeline interrupted during execution, cannot clean up deployed components.");
        } finally {
            this.effector.removeSplitComponent(split.getName());
            // TODO other cleanup necessary?
        }

        this.knowledge.getComponent(split.getNextComponent()).ifPresentOrElse(
            c -> c.handleComponentInPipeline(this), 
            () -> this.stopFeedbackLoop());
    }

    private Thread generatePipelineThread(String splitServiceName, String pipelineName, double targetValue) {
        // Add the configuration parameters for the split component to the setups in the pipeline 
        //  --> adjust the setups to take the split component into account
        Map<String, String> newParam = Map.of(
            "POPULATION_SPLIT_NAME", splitServiceName,
            "POPULATION_SPLIT_TARGET", Double.toString(targetValue)
        );
        Set<Setup> customSetups = this.knowledge.getSetups().stream()
            .map(s -> new Setup(s, newParam))
            .collect(Collectors.toSet());

        // Adjust the assignment functions of the user profiles used in the pipeline
        Set<UserProfile> customUserProfiles = this.knowledge.getExperiments().stream()
            .map(Experiment::getUserProfile)
            .map(p -> new UserProfile(p, ABRoutingMode.Split))
            .collect(Collectors.toSet());

        this.logger.info("Retrieving pipeline from the knowledge.");
        Pipeline pipeline = this.knowledge.getPipeline(pipelineName);

        this.logger.info("Creating thread for the parallel A/B pipeline.");
        return this.createThreadPipelineExecution(pipeline, this.retrievePipelineComponents(pipeline, customSetups, customUserProfiles));
    }

    private Thread createThreadPipelineExecution(Pipeline pipeline, PipelineComponents components) {
        return new Thread(() -> {
            Logger.getAnonymousLogger().info("[Thread started]");
            if (pipeline == null) {
                return;
            }

            // Construct a new Feedbackloop with the first pipeline to be executed
            FeedbackLoop feedbackLoop = new FeedbackLoop(true);
            feedbackLoop.initializeFeedbackLoop(components.setups(), components.experiments(), components.rules(), 
                components.populationSplits(), components.pipelines(), pipeline.getStartingComponent());

            feedbackLoop.stopFeedbackLoop(true);
            // TODO add history somewhere
        });
    }


    private PipelineComponents retrievePipelineComponents(Pipeline pipeline, Set<Setup> customSetups, Set<UserProfile> customProfiles) {
        if (pipeline == null) {
            return null;
        }

        Set<Experiment<?>> experiments = pipeline.getExperiments().stream()
            .map(e -> this.knowledge.getExperiment(e))
            .collect(Collectors.toSet());
        var setupNames = experiments.stream().map(Experiment::getSetup).toList();
        
        // NOTE cheating the java type system again
        @SuppressWarnings({"unchecked", "rawtypes"})
        Set<Experiment<?>> customExperiments = (Set<Experiment<?>>) (Object) experiments.stream()
            .map(e -> new Experiment(e, customProfiles.stream()
                .filter(p -> e.getUserProfile().getName().equals(p.getName()))
                .findFirst().orElseThrow())
            )
            .collect(Collectors.toSet());

        return new PipelineComponents(
            customSetups.stream().filter(s -> setupNames.contains(s.getName())).collect(Collectors.toSet()), 
            customExperiments, 
            pipeline.getTransitionRules().stream()
                .map(r -> this.knowledge.getTransitionRule(r))
                .collect(Collectors.toSet()), 
            pipeline.getPopulationSplits().stream()
                .map(s -> this.knowledge.getPopulationSplit(s))
                .collect(Collectors.toSet()), 
            pipeline.getPipelines().stream()
                .map(p -> this.knowledge.getPipeline(p))
                .collect(Collectors.toSet())
        );
    }


    
    public void handleComponent(ABComponent component) {
        throw new RuntimeException(String.format("'handleComponent' method not implemented yet for component of type %s", 
            component.getClass().getName()));
    }



    private void startSetup(Setup setup) {
        // Handling the setup
        knowledge.setCurrentSetup(setup);
        knowledge.setABComponentName(setup.getABComponent().serviceName());

        int port = this.effector.deploySetup(setup.getName());
        knowledge.setExposedPort(port, setup.getABComponent().serviceName());
        knowledge.addToHistory(new WorkflowStep(WorkflowStepType.Setup, setup.getName()));
    }

    private void stopSetup() {
        this.knowledge.getCurrentExperiment().ifPresent(s -> {
            this.effector.removeSetup(s.getName());
            this.knowledge.setCurrentSetup(null);
            this.knowledge.setABComponentName(null);
            this.knowledge.removeExposedPort(s.getName());
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
        
        this.stopSetup();

        if (!keepKnowledge) {
            this.knowledge.reset();
        }

    }



    public int getId() {
        return this.id;
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
        if (!this.isActive()) {
            return;
        }

        try {
            monitor.monitor();
    
            boolean shouldAdapt = analyzer.analyze();
    
            if (shouldAdapt) {
                // If the system should adapt the required amount of data samples is reached
                //  ---> stop the user profiles from running
                this.stopLocustRunners();
    
                planner.plan();
                executor.execute();
            }
        } catch(Exception e) {
            this.logger.severe("Exception thrown in MAPE task: " + e.toString());
            this.logger.log(Level.SEVERE, e.getMessage(), e);
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
