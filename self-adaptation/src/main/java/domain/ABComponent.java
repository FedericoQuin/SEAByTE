package domain;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

import adaptation.FeedbackLoop;
import domain.command.Command;


// A component that needs setup and dissolution of containers in the underlying application
public interface ABComponent {

    String getName();

    // List<Command> getStartCommands();
    @JsonIgnore
    List<Command> getStopCommands();

    void handleComponentInPipeline(FeedbackLoop feedbackLoop);
}
