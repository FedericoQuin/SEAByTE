package domain.setup;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Logger;
import java.util.stream.Collectors;
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

    // Extra parameters that should be passed onto the AB component
    private Map<String, String> extraParamsAB;

    @JsonIgnore
    private NewService removedService;

    @JsonIgnore
    private final Logger logger = Logger.getLogger(Setup.class.getName());


    public Setup(String name, NewService versionA, NewService versionB, NewService abComponent, String toRemove) {
        this(name, versionA, versionB, abComponent, toRemove, Setup.findServiceToBeRemoved(toRemove).orElse(null), new HashMap<>());
    }
    
    public Setup(Setup other, Map<String, String> extraParamsAB) {
        this(other.name, other.versionA, other.versionB, other.abComponent, other.serviceToRemove, other.removedService,
            Stream.concat(other.extraParamsAB.entrySet().stream(), extraParamsAB.entrySet().stream())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));
    }
    
    public Setup(String name, NewService versionA, NewService versionB, NewService abComponent, String toRemove, 
            NewService removedService, Map<String, String> extraParamsAB) {
        this.name = name;
        this.versionA = versionA;
        this.versionB = versionB;
        this.abComponent = abComponent;
        this.serviceToRemove = toRemove;

        this.removedService = removedService;
        this.extraParamsAB = extraParamsAB;
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

    private static Optional<NewService> findServiceToBeRemoved(String removeService) {
        // Look for the service that corresponds to the service that will be removed
        for (String service : new RetrieveRunningServices().execute().get().split("\n")) {
            if (!service.contains("\t")) {
                continue;
            }

            String name = service.split("\t")[0].replace(String.format("%s%s", Constants.STACK_PREFIX, Constants.STACK_PREFIX_SEPARATOR), ""); // .replace("WS_", "")
            String image = service.split("\t")[1];

            if (name.equals(removeService)) {
                Logger.getLogger(Setup.class.getName()).info(String.format("Found a match of service that will be removed: %s | %s", name, image));
                return Optional.of(new NewService(removeService, image));
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
        commands.add(new InitiateService(Constants.generateStackName(serviceA.serviceName()), 
            serviceA.serviceName(), serviceA.imageName(), 80, Constants.WS_NETWORK));
        commands.add(new InitiateService(Constants.generateStackName(serviceB.serviceName()),
            serviceB.serviceName(), serviceB.imageName(), 80, Constants.WS_NETWORK));

        // Decommission the existing service
        commands.add(new RemoveService(Constants.generateStackName(decommission)));
        
        // Add the new service that overlapped with the older one
        // In our case this is the AB-component service
        commands.add(
            new InitiateExposedServiceBuilder(Constants.generateStackName(serviceAB.serviceName()),
                    serviceAB.serviceName(), serviceAB.imageName(), Constants.WS_NETWORK)
                .withPort(80)
                .withExposedPort(networkPort, Constants.AB_COMPONENT_ADAPTATION_SERVER_PORT)
                .withEnvironmentVariable("AB_COMPONENT_NAME", this.getName())
                .withEnvironmentVariable("VERSIONA", serviceA.serviceName())
                .withEnvironmentVariable("VERSIONB", serviceB.serviceName())
                .withEnvironmentVariables(this.extraParamsAB)
                .build()
        );
        
        return commands;
    }
    
    
    
    public List<Command> generateReverseCommands() {
        // Remove all spawned services
        return List.of(
            new RemoveService(Constants.generateStackName(this.getVersionA().serviceName())),
            new RemoveService(Constants.generateStackName(this.getVersionB().serviceName())),
            new RemoveService(Constants.generateStackName(this.getABComponent().serviceName()))
        );
    }



    public List<Command> generateReverseCommandsWithReboot() {
        return Stream.concat(this.generateReverseCommands().stream(), this.getRemovedService()
            .map(s -> Stream.of(new InitiateService(Constants.generateStackName(s.serviceName()),
                s.serviceName(), s.imageName(), 80, Constants.WS_NETWORK)))
            .orElseGet(() -> {
                this.logger.warning("Could not restore removed service: removed service not stored or found during startup.");
                return Stream.empty();
            })).toList();
    }
    
 
    public static record NewService(String serviceName, String imageName) {}
}
