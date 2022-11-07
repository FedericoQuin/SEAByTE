package domain.command;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class InitiateExposedService extends Command {
    private static String COMMAND_TEMPLATE = 
        "docker service create --name %s --replicas %d %s %s --network %s %s";
    
    private String serviceName;
    private int amtInstances;
    private String networkName;
    private String baseDockerImage;
    private List<DockerPort> ports;
    private Map<String, String> environmentVariables;

    private Logger logger = Logger.getLogger(InitiateExposedService.class.getName());


    private InitiateExposedService(InitiateExposedServiceBuilder builder) {
        this.serviceName = builder.serviceName;
        this.amtInstances = builder.amtInstances;
        this.networkName = builder.networkName;
        this.baseDockerImage = builder.baseDockerImage;
        this.ports = builder.ports;
        this.environmentVariables = builder.environmentVariables;
    }


    public Optional<String> execute() {
        try {
            String command = String.format(COMMAND_TEMPLATE, 
                this.serviceName,
                this.amtInstances,
                this.ports.stream().map(DockerPort::generateDockerCmdFlag).collect(Collectors.joining(" ")),
                this.environmentVariables.entrySet().stream()
                    .map(e -> String.format("-e %s=%s", e.getKey(), e.getValue()))
                    .collect(Collectors.joining(" ")),
                this.networkName,
                this.baseDockerImage);
            logger.info(command);
            Command.executeCommand(command);
        } catch (IOException | InterruptedException e) {
            logger.severe(e.getMessage());
        }
        return Optional.empty();
    }


    private static class DockerPort {
        private int containerPort;
        private int hostPort;

        public DockerPort(int inside, int outside) {
            this.containerPort = inside;
            this.hostPort = outside;
        }

        public DockerPort(int inside) {
            this(inside, -1);
        }

        public String generateDockerCmdFlag() {
            return String.format("-p %s%d", (this.hostPort == -1 ? "" : String.format("%d:", this.hostPort)), this.containerPort);
        }
    }


    public static class InitiateExposedServiceBuilder {
        private String serviceName;
        private int amtInstances;
        private String networkName;
        private String baseDockerImage;
        private List<DockerPort> ports;
        private Map<String, String> environmentVariables;

        public InitiateExposedServiceBuilder(String serviceName, String baseDockerImage, String networkName) {
            this.serviceName = serviceName;
            this.baseDockerImage = baseDockerImage;
            this.networkName = networkName;
            
            this.amtInstances = 1;
            this.ports = new ArrayList<>();
            this.environmentVariables = new HashMap<>();
        }

        public InitiateExposedServiceBuilder withInstances(int amount) {
            this.amtInstances = amount;
            return this;
        }

        public InitiateExposedServiceBuilder withPort(int containerPort) {
            this.ports.add(new DockerPort(containerPort));
            return this;
        }


        public InitiateExposedServiceBuilder withExposedPort(int hostPort, int containerPort) {
            this.ports.add(new DockerPort(containerPort, hostPort));
            return this;
        }

        public InitiateExposedServiceBuilder withEnvironmentVariable(String name, String value) {
            this.environmentVariables.put(name, value);
            return this;
        }


        public InitiateExposedService build() {
            return new InitiateExposedService(this);
        }

    }

}
