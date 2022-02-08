package domain.setup;

import java.util.ArrayList;
import java.util.List;

import domain.Constants;
import domain.command.Command;
import domain.command.InitiateExposedService.InitiateExposedServiceBuilder;
import domain.command.InitiateService;
import domain.command.RemoveService;
import util.Networking;

public class Setup {
    private String name;
    private NewService versionA;
    private NewService versionB;
    private NewService abComponent;
    private String serviceToRemove;



    public Setup(String name, NewService versionA, NewService versionB, NewService abComponent, String toRemove) {
        this.name = name;
        this.versionA = versionA;
        this.versionB = versionB;
        this.abComponent = abComponent;
        this.serviceToRemove = toRemove;
    }

    public String getName() {
        return this.name;
    }

    public NewService getVersionA() {
        return this.versionA;
    }
    
    public NewService getVersionB() {
        return this.versionB;
    }

    public NewService getABComponent() {
        return this.abComponent;
    }
    
    public String getRemoveService() {
        return this.serviceToRemove;
    }



    public List<Command> generateCommands() {
        return this.generateCommands(Networking.generateAvailableNetworkPort());
    }
    
    public List<Command> generateCommands(int networkPort) {
        NewService serviceA = this.getVersionA();
        NewService serviceB = this.getVersionB();
        NewService serviceAB = this.getABComponent();
        var decommission = this.getRemoveService();
        
        List<Command> commands = new ArrayList<>();


        // Add services that do not overlap with the decommissioned service first
        commands.add(new InitiateService(serviceA.serviceName(), serviceA.imageName(), 80, Constants.WS_NETWORK));
        commands.add(new InitiateService(serviceB.serviceName(), serviceB.imageName(), 80, Constants.WS_NETWORK));

        // Decommission the existing service
        commands.add(new RemoveService(String.format("WS-1-0-0_%s", decommission)));
        
        // Add the new service that overlapped with the older one
        // In our case this is the AB-component service
        commands.add(
            new InitiateExposedServiceBuilder(serviceAB.serviceName(), serviceAB.imageName(), Constants.WS_NETWORK)
                .withPort(80)
                .withExposedPort(networkPort, Constants.AB_COMPONENT_ADAPTATION_SERVER_PORT)
                .withEnvironmentVariable("AB_COMPONENT_NAME", this.getName())
                .withEnvironmentVariable("VERSIONA", serviceA.serviceName())
                .withEnvironmentVariable("VERSIONB", serviceB.serviceName())
                .build()
        );
        
        return commands;
    }

    public List<Command> generateReverseCommands() {
        // TODO also respawn the originally removed service --> requires the original image
        // Remove all the spawned services

        return List.of(
            new RemoveService(this.getVersionA().serviceName()),
            new RemoveService(this.getVersionB().serviceName()),
            new RemoveService(this.getABComponent().serviceName())
        );
    }
    
 
    public static record NewService(String serviceName, String imageName) {}
}
