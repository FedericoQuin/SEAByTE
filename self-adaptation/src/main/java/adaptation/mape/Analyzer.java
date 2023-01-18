package adaptation.mape;

import java.util.logging.Logger;

import adaptation.FeedbackLoop;
import adaptation.Knowledge;
import domain.experiment.Experiment;
import domain.experiment.statistic.StatisticalTest;
import domain.experiment.statistic.StatisticalTest.StatisticalResultWithPValue;

public class Analyzer {

    private static Logger logger = Logger.getLogger(Analyzer.class.getName());
    
    private Knowledge knowledge;
    
    public Analyzer(FeedbackLoop feedbackLoop) {
        this.knowledge = feedbackLoop.getKnowledge();
    }


    public boolean analyze() {
        // Check if ample samples are available for both variants before we make further decisions
        int requiredSamples = this.knowledge.getCurrentExperiment().get().getStatisticalTest().getSamples();
        Experiment<?> experiment = this.knowledge.getCurrentExperiment().get();
        StatisticalTest<?> test = experiment.getStatisticalTest();

        // Logger.getLogger(Analyzer.class.getName()).info(
        //     Long.toString(this.knowledge.getRequestsA().stream().filter(test.getMetric().filterFunction()).count()));

        if (test.getMetric().extractRelevantDataAsStream(this.knowledge.getRequestsA()).count() >= requiredSamples 
                && test.getMetric().extractRelevantDataAsStream(this.knowledge.getRequestsB()).count() >= requiredSamples) {
    
            StatisticalResultWithPValue result = test.validateNullHypothesis(this.knowledge.getRequestsA(), this.knowledge.getRequestsB());
    
            logger.info(String.format("Observed p value: %.6f", result.pvalue()));
            logger.info(String.format("Result of test: %s", result.result().toString()));
            this.knowledge.setStatisticalResult(result.result());

            return true;
        }

        return false;
    }
}
