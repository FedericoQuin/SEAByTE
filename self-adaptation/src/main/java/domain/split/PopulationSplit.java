package domain.split;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

import adaptation.FeedbackLoop;
import domain.ABComponent;
import domain.Constants;
import domain.command.Command;
import domain.command.InitiateExposedService.InitiateExposedServiceBuilder;
import domain.command.RemoveService;
import domain.setup.Setup.NewService;


public class PopulationSplit implements ABComponent {

    private final String name;
    private final String pipelineName1;
    private final String pipelineName2;
    private final double targetValue1;
    private final double targetValue2;
    
    private final String nextComponent;
    
    private final NewService splitComponent;


    public PopulationSplit(String name, String pipelineName1, String pipelineName2, double targetValue1, double targetValue2,
             String nextComponent, NewService splitComponent) {
        this.name = name;
        this.pipelineName1 = pipelineName1;
        this.pipelineName2 = pipelineName2;
        this.nextComponent = nextComponent;
        this.targetValue1 = targetValue1;
        this.targetValue2 = targetValue2;

        this.splitComponent = splitComponent;
    }

    public PopulationSplit(PopulationSplit other) {
        this(other.name, other.pipelineName1, other.pipelineName2, other.targetValue1, other.targetValue2,
            other.nextComponent, new NewService(other.splitComponent.serviceName(), other.splitComponent.imageName()));
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

    public String getNextComponent() {
        return this.nextComponent;
    }

    public double getTargetValue1() {
        return this.targetValue1;
    }

    public double getTargetValue2() {
        return this.targetValue2;
    }

    public NewService getSplitComponent() {
        return this.splitComponent;
    }



    @JsonIgnore
    public List<Command> getStartCommands() {
        return List.of(new InitiateExposedServiceBuilder(
                Constants.generateStackName(this.splitComponent.serviceName()),
                this.splitComponent.serviceName(),
                this.splitComponent.imageName(), Constants.WS_NETWORK)
            .withPort(80)
            .withInstances(1)
            .withEnvironmentVariable("POPULATION_SPLIT_NAME", this.name)
            .build());
    }

    @JsonIgnore
    public List<Command> getStartCommands(int networkPort) {
        return List.of(new InitiateExposedServiceBuilder(
                Constants.generateStackName(this.splitComponent.serviceName()),
                this.splitComponent.serviceName(),
                this.splitComponent.imageName(), Constants.WS_NETWORK)
            .withExposedPort(networkPort, 80)
            .withInstances(1)
            .withEnvironmentVariable("POPULATION_SPLIT_NAME", this.name)
            .build());
    }

    @Override
    @JsonIgnore
    public List<Command> getStopCommands() {
        return List.of(new RemoveService(Constants.generateStackName(this.splitComponent.serviceName())));
    }


    @Override
    public void handleComponentInPipeline(FeedbackLoop feedbackLoop) {
        feedbackLoop.handleComponent(this);
    }
}
