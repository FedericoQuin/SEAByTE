package domain.experiment.metric;

import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Stream;

import domain.URLRequest;

public abstract class Metric<T, U> {
    private String name;
    
    protected Metric(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

    public List<T> extractRelevantData(List<URLRequest> requests) {
        synchronized (requests) {
            return this.extractRelevantDataAsStream(requests).toList();
        }
    }
    
    
    public String getPlotData(List<URLRequest> requestsA, List<URLRequest> requestsB) {

        // TODO limit the data in the graph such that each variant displays equal samples?
        //  Currently deliberately not limited as to not create any confusion between the summary data
        //  at the top of the page compared to the data present in the graphs

        // List<T> dataA = this.extractRelevantData(requestsA);
        // List<T> dataB = this.extractRelevantData(requestsB);
        // var samples = Math.min(dataA.size(), dataB.size());
        // return this.getPlotDataTyped(dataA.subList(0, samples), dataB.subList(0, samples));
        return this.getPlotDataTyped(this.extractRelevantData(requestsA), this.extractRelevantData(requestsB));
    }
    

    /*
        Ideally these would be static, but we want to enforce implementation of these in subclasses
    */
    public abstract Predicate<URLRequest> filterFunction();
    public abstract Stream<T> extractRelevantDataAsStream(List<URLRequest> requests);
    public abstract Map<String, U> getExtraMetrics(List<URLRequest> requestsA, List<URLRequest> requestsB);
    public abstract List<String> getExtraMetricNames();
    public abstract String getPlotDataTyped(List<T> requestsA, List<T> requestsB);

    public abstract String getInfoSummary(List<URLRequest> requests);
}
