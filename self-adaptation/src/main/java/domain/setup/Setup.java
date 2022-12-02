package domain.setup;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;
import java.util.stream.Stream;

import com.fasterxml.jackson.annotation.JsonIgnore;

import domain.Constants;
import domain.command.Command;
import domain.command.InitiateExposedService.InitiateExposedServiceBuilder;
import domain.command.InitiateService;
import domain.command.RemoveService;
import domain.command.RetrieveRunningServices;
import util.Networking;

public class Setup {
    private String name;
    private NewService versionA;
    private NewService versionB;
    private NewService abComponent;
    private String serviceToRemove;

    @JsonIgnore
    private NewService removedService;

    @JsonIgnore
    private final Logger logger = Logger.getLogger(Setup.class.getName());


    public Setup(String name, NewService versionA, NewService versionB, NewService abComponent, String toRemove) {
        this.name = name;
        this.versionA = versionA;
        this.versionB = versionB;
        this.abComponent = abComponent;
        this.serviceToRemove = toRemove;

        this.removedService = this.findServiceToBeRemoved().orElse(null);
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

    private Optional<NewService> findServiceToBeRemoved() {
        // Look for the service that corresponds to the service that will be removed
        for (String service : new RetrieveRunningServices().execute().get().split("\n")) {
            if (!service.contains("\t")) {
                continue;
            }

            String name = service.split("\t")[0].replace(Constants.STACK_NAME, ""); // .replace("WS_", "")
            String image = service.split("\t")[1];

            if (name.equals(this.getRemoveService())) {
                this.logger.info(String.format("Found a match of service that will be removed: %s | %s", name, image));
                return Optional.of(new NewService(this.getRemoveService(), image));
            }
        }
        return Optional.empty();
    }

    private Optional<NewService> getRemovedService() {
        return Optional.ofNullable(this.removedService);
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
        commands.add(new InitiateService(String.format("%s%s", Constants.STACK_NAME, serviceA.serviceName()), 
            serviceA.serviceName(), serviceA.imageName(), 80, Constants.WS_NETWORK));
        commands.add(new InitiateService(String.format("%s%s", Constants.STACK_NAME, serviceB.serviceName()),
            serviceB.serviceName(), serviceB.imageName(), 80, Constants.WS_NETWORK));

        // Decommission the existing service
        commands.add(new RemoveService(String.format("%s%s", Constants.STACK_NAME, decommission)));
        
        // Add the new service that overlapped with the older one
        // In our case this is the AB-component service
        commands.add(
            new InitiateExposedServiceBuilder(String.format("%s%s", Constants.STACK_NAME, serviceAB.serviceName()),
                    serviceAB.serviceName(), serviceAB.imageName(), Constants.WS_NETWORK)
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
        // Remove all spawned services
        return List.of(
            new RemoveService(String.format("%s%s", Constants.STACK_NAME, this.getVersionA().serviceName())),
            new RemoveService(String.format("%s%s", Constants.STACK_NAME, this.getVersionB().serviceName())),
            new RemoveService(String.format("%s%s", Constants.STACK_NAME, this.getABComponent().serviceName()))
        );
    }



    public List<Command> generateReverseCommandsWithReboot() {
        return Stream.concat(this.generateReverseCommands().stream(), this.getRemovedService()
            .map(s -> Stream.of(new InitiateService(String.format("%s%s", Constants.STACK_NAME, s.serviceName()),
                s.serviceName(), s.imageName(), 80, Constants.WS_NETWORK)))
            .orElseGet(() -> {
                this.logger.warning("Could not restore removed service: removed service not stored or found during startup.");
                return Stream.empty();
            })).toList();
    }
    
 
    public static record NewService(String serviceName, String imageName) {}
}
