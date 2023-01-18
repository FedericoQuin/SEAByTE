package adaptation.mape;

import java.util.HashMap;
import java.util.Map;

import adaptation.FeedbackLoop;
import adaptation.Knowledge;
import adaptation.Knowledge.WorkflowStep;
import adaptation.Knowledge.WorkflowStepType;
import domain.experiment.Experiment;
import domain.experiment.TransitionRule;
import domain.experiment.statistic.StatisticalTest;
import domain.experiment.statistic.StatisticalTest.StatisticalResult;

public class Planner {
    
    private Knowledge knowledge;
    
    public Planner(FeedbackLoop feedbackLoop) {
        this.knowledge = feedbackLoop.getKnowledge();
    }


    public void plan() {
        Experiment<?> experiment = this.knowledge.getCurrentExperiment().get();
        StatisticalTest<?> test = experiment.getStatisticalTest();
        StatisticalResult result = this.knowledge.getStatisticalResult();
        this.knowledge.clearStatisticalResult();

        Map<String, StatisticalResult> statVariables = new HashMap<>();
        statVariables.put(test.getResultingVariableName(), result);
        
        Map<String, Double> extraVariables = test.getMetric()
            .getExtraMetrics(this.knowledge.getRequestsA(), this.knowledge.getRequestsB());
        

        // Determine the next experiment to setup
        TransitionRule rule = knowledge.getTransitionRules().stream()
            .filter(r -> r.getFromExperiment().equals(experiment.getName()))
            .filter(r -> r.isSatisfied(statVariables, extraVariables))
            // .map(TransitionRule::getToExperiment)
            .findFirst().orElseThrow();
        
        this.knowledge.addToHistory(new WorkflowStep(WorkflowStepType.Rule, rule.getName()));
        
        this.knowledge.setNextComponentName(rule.getToComponent());
    }    
}
