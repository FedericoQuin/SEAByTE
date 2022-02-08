package domain.experiment;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import domain.URLRequest;
import domain.experiment.metric.Metric;

public abstract class StatisticalTest<T> {
    protected NullHypothesis<T> nullHypothesis;
    
    protected double pValue;
    protected int samples;
    protected String resultingVariableName;
    protected String type;
    protected List<String> metrics;
    protected Metric<T, Double> metric;


    public StatisticalTest(NullHypothesis<T> nullHypothesis, double pValue, int samples, 
            String resultingVariableName, String type, List<String> metrics) {
        this.nullHypothesis = nullHypothesis;
        this.pValue = pValue;
        this.samples = samples;
        this.resultingVariableName = resultingVariableName;
        this.type = type;
        this.metrics = metrics;

        Set<String> metricsSplit = metrics.stream()
                .map(m -> m.split("_")[0])
                .collect(Collectors.toSet());
        if (metricsSplit.size() != 1) {
            throw new RuntimeException("Statistical tests currently only support one single type of metric.");
        }


        String metric = metricsSplit.stream().findFirst().orElseThrow();
        try {
            Class<?> clazz = Class.forName(String.format("domain.experiment.metric.%s", metric));
            if (!Metric.class.isAssignableFrom(clazz)) {
                throw new RuntimeException(String.format(
                    "The class that supports the metric '%s' does not inherit from base class '%s'", 
                    metric, Metric.class.getName())
                );
            }

            // Exception caught further down
            this.metric = (Metric<T, Double>) clazz.getConstructor().newInstance();

        } catch (ClassNotFoundException e) {
            throw new RuntimeException(String.format("No class found that supports the metric '%s'", metric));
        } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | 
                InvocationTargetException | NoSuchMethodException | SecurityException e) {
            throw new RuntimeException(String.format(
                "Error instantiating class domain.experiment.metric.%s, thrown exception:\n%s", 
                metric, e.getStackTrace())
            );
        } catch (ClassCastException e) {
            throw new RuntimeException("The specified metrics for this statistical test do not match with the " +  
                "required data type used in the statistical test.");
        }
    }
    
    public String getType() {
        return this.type;
    }

    public double getPValue() {
        return this.pValue;
    }

    public int getSamples() {
        return this.samples;
    }

    public String getResultingVariableName() {
        return this.resultingVariableName;
    }

    public List<String> getMetrics() {
        return this.metrics;
    }

    public Metric<T, Double> getMetric() {
        return this.metric;
    }



    public StatisticalResult validateNullHypothesis(List<URLRequest> samples1, List<URLRequest> samples2) {
        return this.validateNullHypothesisTyped(this.metric.extractRelevantData(samples1), 
            this.metric.extractRelevantData(samples2));
    }
    public abstract StatisticalResult validateNullHypothesisTyped(List<T> samples1, List<T> samples2);


    public abstract String toHtmlString();
    


    public static class NullHypothesis<T> {
        private Operator operator;
        private String leftOperand;
        private String rightOperand;

        public NullHypothesis(Operator op, String leftOperand, String rightOperand) {
            this.operator = op;
            this.leftOperand = leftOperand;
            this.rightOperand = rightOperand;
        }

        public Operator getOperator() {
            return this.operator;
        }

        public String getLeftOperand() {
            return this.leftOperand;
        }

        public String getRightOperand() {
            return this.rightOperand;
        }

        public enum Operator {
            Equal("=="),
            NonEqual("!=");
            
            private String rep;

            private Operator(String rep) {
                this.rep = rep;
            }

            public String toString() {
                return this.rep;
            }
        }

        public String toString() {
            return String.format("'%s %s %s'", this.leftOperand, this.operator.toString(), this.rightOperand);
        }
    }
    
    public enum StatisticalResult {
        Reject,
        Inconclusive;
    
        public static StatisticalResult getStatisticalResult(String name) {
            return Arrays.stream(StatisticalResult.values())
                .filter(r -> r.toString().toLowerCase().equals(name))
                .findFirst().orElseThrow();
        }

    }
}
