package domain.experiment.statistic;

import java.util.List;

import org.apache.commons.math3.stat.inference.TTest;

public class WelshStudentTest extends StatisticalTest<Double> {

    public WelshStudentTest(NullHypothesis<Double> nullHypothesis, double pValue, int samples, String resultingVariableName,
            String type, List<String> metrics) {
        super(nullHypothesis, pValue, samples, resultingVariableName, type, metrics);
    }

    @Override
    public StatisticalResultWithPValue validateNullHypothesisTyped(List<Double> samples1, List<Double> samples2) {
        double result = new TTest().tTest(
                samples1.stream().limit(this.getSamples()).mapToDouble(Double::doubleValue).toArray(), 
                samples2.stream().limit(this.getSamples()).mapToDouble(Double::doubleValue).toArray()
        );

        // Logger.getLogger(WelshStudentTest.class.getName()).info(String.format("P value observed: %f", result));
        
        // True means reject, false is inconclusive
        return result <= this.getPValue() ? new StatisticalResultWithPValue(StatisticalResult.Reject, result) 
            : new StatisticalResultWithPValue(StatisticalResult.Inconclusive, result);
    }

    @Override
    public String toHtmlString() {
        return String.format("Welsh t-test: [<br>&nbsp;&nbsp;&nbsp;&nbsp;%s,<br>&nbsp;&nbsp;&nbsp;&nbsp;p=%.3f, n=%d<br>]", 
            this.nullHypothesis.toString(), this.pValue, this.samples);
    }
    
}
