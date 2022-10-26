package domain.command;

import java.io.IOException;
import java.util.Optional;
import java.util.logging.Logger;

public class RemoveService extends Command {
    private static String COMMAND_TEMPLATE = "docker service rm %s";

    private String serviceName;

    private Logger logger = Logger.getLogger(RemoveService.class.getName());

    public RemoveService(String serviceName) {
        this.serviceName = serviceName;
    }


    public Optional<String> execute() { 
        try {
            String command = String.format(COMMAND_TEMPLATE, this.serviceName);
            logger.info(command);
            Command.executeCommand(command);
        } catch (IOException | InterruptedException e) {
            logger.severe(e.getMessage());
        }
        return Optional.empty();
    }
}
