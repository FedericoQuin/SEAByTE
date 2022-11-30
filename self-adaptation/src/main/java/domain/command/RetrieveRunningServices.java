package domain.command;

import java.io.IOException;
import java.util.Optional;
import java.util.logging.Logger;

public class RetrieveRunningServices extends Command {

    private static final String COMMAND_STRING = "docker service ls --format {{.Name}}\\t{{.Image}}";

    private Logger logger = Logger.getLogger(RetrieveRunningServices.class.getName());

    @Override
    public Optional<String> execute() {
        try {
            logger.info("Retrieving all running docker services (and their images) on this machine");
            return Optional.of(Command.executeCommand(RetrieveRunningServices.COMMAND_STRING));
        } catch (IOException | InterruptedException e) {
            logger.severe(e.getMessage());
        }

        return Optional.empty();
    }
    

}
