package domain.experiment.statistic;

import java.util.List;

import org.apache.commons.math3.distribution.NormalDistribution;

public class ProportionalTest extends StatisticalTest<Boolean> {

    public ProportionalTest(NullHypothesis<Boolean> nullHypothesis, double pValue, int samples,
            String resultingVariableName, String type, List<String> metrics) {
        super(nullHypothesis, pValue, samples, resultingVariableName, type, metrics);
    }


    @Override
    public StatisticalResultWithPValue validateNullHypothesisTyped(List<Boolean> samples1, List<Boolean> samples2) {
        // Compare the proportion of instance B to the proportion of the 'claimed' proportion (here variant A)
        double proportionA = samples1.stream().limit(this.samples).filter(b -> b).count() / 
            (double) samples1.stream().limit(this.samples).filter(b -> !b).count();
        double proportionB = samples2.stream().limit(this.samples).filter(b -> b).count() / 
            (double) samples2.stream().limit(this.samples).filter(b -> !b).count();

        double testStatistic = (proportionB - proportionA) / 
            Math.sqrt(proportionA * (1 - proportionA)) *
            Math.sqrt(this.samples);

        double result = 2 * (1 - new NormalDistribution().cumulativeProbability(Math.abs(testStatistic)));

        // Logger.getLogger(ProportionalTest.class.getName()).info(String.format("Observed p-value: %f", result));
        
        // Reject in this context means that the proportion of instance B does not match the claimed proportion in our test
        //  i.e. the proportion of clicks in variant A is different from the proportion of clicks in variant B
        return result <= this.pValue ? new StatisticalResultWithPValue(StatisticalResult.Reject, result) 
            : new StatisticalResultWithPValue(StatisticalResult.Inconclusive, result);
    }


    @Override
    public String toHtmlString() {
        return String.format("One-proportional test: [<br>&nbsp;&nbsp;&nbsp;&nbsp;%s,<br>&nbsp;&nbsp;&nbsp;&nbsp;p=%.3f, n=%d<br>]", 
            this.nullHypothesis.toString(), this.pValue, this.samples);
    }
}
