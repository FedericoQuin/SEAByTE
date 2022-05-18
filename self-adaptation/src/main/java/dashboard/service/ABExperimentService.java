package dashboard.service;

import java.util.Collection;

import org.springframework.beans.factory.annotation.Autowired;

import dashboard.model.ABRepository;
import domain.experiment.Experiment;

public class ABExperimentService {
    @Autowired
    ABRepository repository;


    public void addExperiment(Experiment<?> experiment) {
        repository.addExperiment(experiment);
    }


    public Collection<Experiment<?>> getAllExperiments() {
        return repository.getAllExperiments();
    }

    public Experiment<?> getExperiment(String name) {
        return this.repository.getExperiment(name);
    }
}
