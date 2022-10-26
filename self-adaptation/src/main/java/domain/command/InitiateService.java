package domain.command;

import java.io.IOException;
import java.util.Optional;
import java.util.logging.Logger;


public class InitiateService extends Command {
    private static String COMMAND_TEMPLATE = 
        "docker service create --name %s --replicas %d -p %d --network %s %s";
    
    private String serviceName;
    private int amtInstances;
    private int networkPort;
    private String networkName;
    private String baseDockerImage;

    private Logger logger = Logger.getLogger(InitiateService.class.getName());


    public InitiateService(String serviceName, String baseDockerImage, int networkPort, String networkName, int amtInstances) {
        this.serviceName = serviceName;
        this.amtInstances = amtInstances;
        this.networkPort = networkPort;
        this.networkName = networkName;
        this.baseDockerImage = baseDockerImage;
    }


    public InitiateService(String serviceName, String baseDockerImage, int networkPort, String networkName) {
        this(serviceName, baseDockerImage, networkPort, networkName, 1);
    }

    public Optional<String> execute() {
        try {
            String command = String.format(COMMAND_TEMPLATE, 
                this.serviceName, this.amtInstances, this.networkPort, this.networkName, this.baseDockerImage);
            logger.info(command);
            Command.executeCommand(command);
        } catch (IOException | InterruptedException e) {
            logger.severe(e.getMessage());
        }
        return Optional.empty();
    }
}
