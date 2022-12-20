package dashboard.controller;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.google.gson.JsonParser;

import adaptation.FeedbackLoop;
import adaptation.Knowledge.WorkflowStep;
import dashboard.model.ABRepository;
import domain.experiment.Experiment;
import domain.experiment.StatisticalTest;
import domain.experiment.UserProfile;
import domain.pipeline.Pipeline;
import domain.setup.Setup;


@RestController
@RequestMapping(value="/adaptation")
public class AdaptationController {
    
    private Logger logger = Logger.getLogger(AdaptationController.class.getName());

    @Autowired
    private FeedbackLoop feedbackLoop;

    @Autowired
    private ABRepository repository;


    @GetMapping("status")
    public AdaptationStatus getStatus() {
        var statTest = this.feedbackLoop.getCurrentExperiment().map(Experiment::getStatisticalTest);

        return new AdaptationStatus(
            statTest.map(t -> t.getMetric().getInfoSummary(this.feedbackLoop.getRequestsA())).orElse("N/A"),
            statTest.map(t -> t.getMetric().getInfoSummary(this.feedbackLoop.getRequestsB())).orElse("N/A"),
            this.feedbackLoop.getDeployedSetup().map(Setup::getName).orElse("N/A"), 
            this.feedbackLoop.getCurrentExperiment().map(Experiment::getName).orElse("N/A"),
            this.feedbackLoop.getCurrentUserProfile().map(UserProfile::getName).orElse("N/A"),
            this.feedbackLoop.getCurrentExperiment()
                .map(Experiment::getStatisticalTest)
                .map(StatisticalTest::toHtmlString).orElse("N/A"),
            this.feedbackLoop.getKnowledge().getHistory()
        );
    }


    @GetMapping("plotData")
    public String getPlotData() {
        return this.feedbackLoop.getPlotDataCurrentExperiment().orElse("{}");
    }


    @PostMapping("start")
    public void startFeedbackLoop(@RequestBody String data) {
        this.logger.info("Starting the feedback loop.");

        var root = JsonParser.parseString(data).getAsJsonObject();

        Set<String> experimentNames = new HashSet<>();
        root.get("experiments").getAsJsonArray()
            .forEach(e -> experimentNames.add(e.getAsString()));

        Set<String> rules = new HashSet<>();
        root.get("transitionRules").getAsJsonArray()
            .forEach(e -> rules.add(e.getAsString()));

        String initialComponent = root.get("initialComponent").getAsString();

        // Hacky way to get around the type system in java
        var experiments = (Set<Experiment<?>>)(Set<?>) experimentNames.stream()
            .map(repository::getExperiment).collect(Collectors.toSet());

            
        // TODO fill in population splits and pipelines later, empty sets for now
        this.feedbackLoop.initializeFeedbackLoop(
            experiments.stream()
                .map(s -> repository.getSetup(s.getName()))
                .collect(Collectors.toSet()),
            experiments, 
            rules.stream().map(repository::getTransitionRule).collect(Collectors.toSet()),
            Set.of(),
            Set.of(),
            initialComponent);

        this.logger.info("Feedback loop initialized");
    }

    @PostMapping("startPipeline") 
    public void startFeedbackLoopPipeline(@RequestParam(name="pipelineName") String pipelineName) {
        this.logger.info(String.format("Starting the feedback loop with pipeline '%s'.", pipelineName));

        Pipeline pipeline = repository.getPipeline(pipelineName);
        this.feedbackLoop.initializeFeedbackLoop(pipeline, this.repository);
        this.logger.info("Feedback loop initialized");
    }


    @PostMapping("stop")
    public void stopFeedbackLoop() {
        this.feedbackLoop.stopFeedbackLoop();
    }

    public record AdaptationStatus(String messageA, String messageB, 
        String deployedSetup, String currentExperiment, String userProfile,
        String currentTest, Collection<WorkflowStep> history) {}
}
