package dashboard.controller;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Logger;

import com.google.gson.JsonParser;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import adaptation.FeedbackLoop;
import adaptation.Knowledge.WorkflowStep;
import dashboard.model.ABRepository;
import domain.experiment.Experiment;
import domain.experiment.StatisticalTest;
import domain.experiment.UserProfile;
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
        String setupName = root.get("setup").getAsString();

        List<String> experiments = new ArrayList<>();
        root.get("experiments").getAsJsonArray()
            .forEach(e -> experiments.add(e.getAsString()));

        List<String> rules = new ArrayList<>();
        root.get("transitionRules").getAsJsonArray()
            .forEach(e -> rules.add(e.getAsString()));

        String initialExperiment = root.get("initialExperiment").getAsString();

        this.feedbackLoop.initializeFeedbackLoop(repository.getSetup(setupName),
            // Hacky way to get around the type system in java
            (List<Experiment<?>>)(List<?>) experiments.stream().map(repository::getExperiment).toList(), 
            rules.stream().map(repository::getTransitionRule).toList(),
            initialExperiment);

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
