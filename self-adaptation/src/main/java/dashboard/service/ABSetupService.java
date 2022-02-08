package dashboard.service;

import java.util.Collection;
import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Autowired;

import dashboard.model.ABRepository;
import domain.setup.Setup;

public class ABSetupService {
    
    private static Logger logger = Logger.getLogger(ABSetupService.class.getName());

    @Autowired
    ABRepository repository;



    public void addSetup(Setup setup) {
        // if (this.repository.hasSetup(setup.getName())) {
        //     throw new IllegalArgumentException(String.format("Setup with name %s already exists!", setup.getName()));
        // }
        this.repository.addSetup(setup);
    }

    public Collection<Setup> getAllSetups() {
        return this.repository.getAllSetups();
    }
}
