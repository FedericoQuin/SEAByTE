package dashboard.service;

import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Autowired;

import dashboard.model.ABRepository;
import domain.command.Command;

public class ABRunService {
    @Autowired
    private ABRepository repository;

    private Logger logger = Logger.getLogger(ABRunService.class.getName());

    
    public void runSetup(String name) {
        if (!repository.hasSetup(name)) {
            throw new RuntimeException(String.format("Could not find a setup with name: %s", name));
        }
        var setup = repository.getSetup(name);

        var commands = setup.generateCommands();
        logger.info(String.format("Executing %d commands...", commands.size()));
        
        try {
            commands.forEach(Command::execute);
        } catch (Exception e) {
            logger.info(e.getMessage());
        }
        logger.info(String.format("Running of setup %s complete.", name));
    }
    
}
