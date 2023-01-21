package domain.pipeline;

import java.util.ArrayList;
import java.util.List;

public class Pipeline {
    private final String name;
    // Starting component can either be an experiment or a population split
    private final String startingComponent;

    private final List<String> experiments;
    private final List<String> transitionRules;
    private final List<String> populationSplits;
    private final List<String> pipelines;


    public Pipeline(String name, String startingComponent, List<String> experiments, List<String> transitionRules,
            List<String> pipelines, List<String> populationSplits) {
        this.name = name;
        this.startingComponent = startingComponent;
        this.experiments = experiments;
        this.transitionRules = transitionRules;
        this.pipelines = pipelines;
        this.populationSplits = populationSplits;
    }


    public Pipeline(Pipeline other) {
        this(other.name, other.startingComponent, new ArrayList<>(other.experiments), new ArrayList<>(other.transitionRules),
            new ArrayList<>(other.pipelines), new ArrayList<>(other.populationSplits));
    }


    public String getName() {
        return this.name;
    }


    public String getStartingComponent() {
        return this.startingComponent;
    }


    public List<String> getExperiments() {
        return this.experiments;
    }


    public List<String> getTransitionRules() {
        return this.transitionRules;
    }


    public List<String> getPipelines() {
        return this.pipelines;
    }


    public List<String> getPopulationSplits() {
        return this.populationSplits;
    }

}
