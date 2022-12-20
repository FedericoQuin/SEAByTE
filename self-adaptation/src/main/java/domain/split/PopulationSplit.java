package domain.split;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

import adaptation.FeedbackLoop;
import domain.ABComponent;
import domain.Constants;
import domain.command.Command;
import domain.command.InitiateExposedService.InitiateExposedServiceBuilder;
import domain.setup.Setup.NewService;


public class PopulationSplit implements ABComponent {

    private final String name;
    private final String pipelineName1;
    private final String pipelineName2;
    
    private final NewService splitComponent;


    public PopulationSplit(String name, String pipelineName1, String pipelineName2, NewService splitComponent) {
        this.name = name;
        this.splitComponent = splitComponent;
        this.pipelineName1 = pipelineName1;
        this.pipelineName2 = pipelineName2;
    }


    @Override
    public String getName() {
        return this.name;
    }

    public String getPipelineName1() {
        return this.pipelineName1;
    }

    public String getPipelineName2() {
        return this.pipelineName2;
    }

    public NewService getSplitComponent() {
        return this.splitComponent;
    }



    @JsonIgnore
    public List<Command> getStartCommands() {
        // TODO revise
        
        return List.of(new InitiateExposedServiceBuilder(this.splitComponent.serviceName(), 
                String.format("%s%s", Constants.STACK_NAME, this.splitComponent.serviceName()),
                this.splitComponent.imageName(), Constants.WS_NETWORK)
            .withPort(80)
            .withInstances(1)
            .withEnvironmentVariable("COMPONENT_NAME", this.name)
            // .withEnvironmentVariable("SPLIT_SERVICE_1", routing1)
            // .withEnvironmentVariable("SPLIT_SERVICE_2", routing2)
            .build());
    }

    @JsonIgnore
    public List<Command> getStartCommands(int networkPort) {
        // TODO revise
        return List.of(new InitiateExposedServiceBuilder(this.splitComponent.serviceName(), 
                String.format("%s%s", Constants.STACK_NAME, this.splitComponent.serviceName()),
                this.splitComponent.imageName(), Constants.WS_NETWORK)
            .withExposedPort(networkPort, 80)
            .withInstances(1)
            .withEnvironmentVariable("COMPONENT_NAME", this.name)
            // .withEnvironmentVariable("SPLIT_SERVICE_1", routing1)
            // .withEnvironmentVariable("SPLIT_SERVICE_2", routing2)
            .build());
    }

    @Override
    @JsonIgnore
    public List<Command> getStopCommands() {
        // TODO
        return List.of();
    }


    @Override
    public void handleComponentInPipeline(FeedbackLoop feedbackLoop) {
        feedbackLoop.handleComponent(this);
    }
}
