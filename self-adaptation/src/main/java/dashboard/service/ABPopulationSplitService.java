package dashboard.service;

import java.util.Collection;

import org.springframework.beans.factory.annotation.Autowired;

import dashboard.model.ABRepository;
import domain.split.PopulationSplit;

public class ABPopulationSplitService {

    @Autowired
    ABRepository repository;


    public void addPopulationSplit(PopulationSplit populationSplit) {
        repository.addPopulationSplit(populationSplit);
    }


    public Collection<PopulationSplit> getAllPopulationSplits() {
        return repository.getAllPopulationSplits();
    }

    public PopulationSplit getPopulationSplit(String name) {
        return this.repository.getPopulationSplit(name);
    }
    
}
