package adaptation.mape;

import adaptation.ABSystem.ABConfiguration;
import adaptation.FeedbackLoop;
import util.SoftwareVersion;

public class Executor {

    private FeedbackLoop feedbackLoop;

    public Executor(FeedbackLoop feedbackLoop) {
        this.feedbackLoop = feedbackLoop;
    }


    public void execute() {
        var knowledge = this.feedbackLoop.getKnowledge();

        knowledge.getNextExperiment().ifPresentOrElse(n -> {
            knowledge.setCurrentExperiment(n);
    
            feedbackLoop.startExperiment();
        }, () -> {
            knowledge.setCurrentExperiment(null);
            this.feedbackLoop.stopFeedbackLoop();
        });
    }


    public void adjustWeightsAB(ABConfiguration configuration) {
        // First adjust top-level weights in the local nodejs server
        // ...

        // Next update the stored sessions so that they correspond to the updated weights
        // These sessions are stored in the local nodejs server 
        // ...
    }


    public void deployNewVersion(SoftwareVersion version) {
        // Adjust the routing table in the top-level nginx server
        // to accomodate for the new version

        // Setup A/B test
        // For now: keep the initial traffic to the new version at 0% until later notice
    }
}
