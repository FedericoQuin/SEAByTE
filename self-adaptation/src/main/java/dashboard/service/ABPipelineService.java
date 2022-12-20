package dashboard.service;

import java.util.Collection;

import org.springframework.beans.factory.annotation.Autowired;

import dashboard.model.ABRepository;
import domain.pipeline.Pipeline;

public class ABPipelineService {
    
    @Autowired
    ABRepository repository;


    public void addPipeline(Pipeline pipeline) {
        repository.addPipeline(pipeline);
    }


    public Collection<Pipeline> getAllPipelines() {
        return repository.getAllPipelines();
    }

    public Pipeline getPipeline(String name) {
        return this.repository.getPipeline(name);
    }
}
