package domain.experiment.metric;

import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import domain.URLRequest;

public class FeedbackWebApp extends Metric<Double, Double> {
    
    public FeedbackWebApp() {
        super("FeedbackWebApp");
    }

    @Override
    public Predicate<URLRequest> filterFunction() {
        return x -> x.getTarget().startsWith("/feedback/");
    }

    @Override
    public Stream<Double> extractRelevantDataAsStream(List<URLRequest> requests) {
        return requests.stream()
            .map(URLRequest::getTarget)
            .map(t -> Double.parseDouble(t.substring(t.lastIndexOf("/") + 1)));
    }

    @Override
    public Map<String, Double> getExtraMetrics(List<URLRequest> requestsA, List<URLRequest> requestsB) {
        List<Double> valuesA = this.extractRelevantDataAsStream(requestsA).mapToDouble(Double::doubleValue).boxed().toList();
        List<Double> valuesB = this.extractRelevantDataAsStream(requestsB).mapToDouble(Double::doubleValue).boxed().toList();

        return Map.of(
            String.format("mean(%s_A)", this.getName()),
            valuesA.stream().mapToDouble(Double::doubleValue).average().orElse(0.0), 
            String.format("mean(%s_B)", this.getName()),
            valuesB.stream().mapToDouble(Double::doubleValue).average().orElse(0.0),
            String.format("median(%s_B)", this.getName()),
            valuesA.size() > 0 ? valuesA.get(valuesA.size() / 2) : 0.0,
            String.format("median(%s_B)", this.getName()),
            valuesB.size() > 0 ? valuesB.get(valuesB.size() / 2) : 0.0
        );
    }

    @Override
    public List<String> getExtraMetricNames() {
        return List.of(
            String.format("mean(%s_A)", this.getName()), 
            String.format("mean(%s_B)", this.getName()),
            String.format("median(%s_A)", this.getName()),
            String.format("median(%s_B)", this.getName())
        );
    }


    @Override
    public String getPlotDataTyped(List<Double> requestsA, List<Double> requestsB) {
        return String.format("{\"A\": [%s], \"B\": [%s], \"title\": \"%s\", \"type\": \"box\"}", 
            requestsA.stream()
                .map(d -> d.toString())
                .collect(Collectors.joining(",")),
            requestsB.stream()
                .map(d -> d.toString())
                .collect(Collectors.joining(",")),
            "User feedback rating (score out of 10)"
    );
    }

    @Override
    public String getInfoSummary(List<URLRequest> requests) {
        var relevant = this.extractRelevantData(requests);
        return String.format("%d (average %.2f/10 user rating)", relevant.size(), 
            relevant.stream().mapToDouble(d -> d).average().orElse(0));
    }
}
