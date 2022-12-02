package dashboard.service;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;

import adaptation.FeedbackLoop;
import domain.setup.Setup;

public class AdaptationService {
    

    @Autowired
    FeedbackLoop feedbackLoop;

    public Optional<Setup> getDeployedSetup() {
        if (!feedbackLoop.isActive()) {
            return Optional.empty();
        }
        var knowledge = feedbackLoop.getKnowledge();

        return knowledge.getCurrentSetup();
    }

}
