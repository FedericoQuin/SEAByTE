package domain.command;

import java.io.IOException;
import java.util.Optional;
import java.util.logging.Logger;

public class RetrieveAvailableImages extends Command {
    private static final String COMMAND_STRING = 
        "docker image ls --format '{{.Repository}}:{{.Tag}}'";

    private Logger logger = Logger.getLogger(RetrieveAvailableImages.class.getName());


    @Override
    public Optional<String> execute() {
        try {
            logger.info("Retrieving all available docker images on this machine");
            return Optional.of(Command.executeCommand(RetrieveAvailableImages.COMMAND_STRING));
        } catch (IOException | InterruptedException e) {
            logger.severe(e.getMessage());
        }

        return Optional.empty();
    }
    
}
