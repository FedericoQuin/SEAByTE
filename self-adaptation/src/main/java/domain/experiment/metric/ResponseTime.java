package domain.experiment.metric;

import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import domain.URLRequest;

public class ResponseTime extends Metric<Double, Double> {
    public ResponseTime() {
        super("ResponseTime");
    }

    @Override
    public Predicate<URLRequest> filterFunction() {
        return x -> x.getTarget().equals("/recommendation");
    }

    @Override
    public Stream<Double> extractRelevantDataAsStream(List<URLRequest> requests) {
        return requests.stream()
            .map(URLRequest::getResponseTime);
    }

    @Override
    public Map<String, Double> getExtraMetrics(List<URLRequest> requestsA, List<URLRequest> requestsB) {
        return Map.of(
            String.format("mean(%s_A)", this.getName()),
            this.extractRelevantDataAsStream(requestsA).mapToDouble(Double::doubleValue).average().orElse(0.0), 
            String.format("mean(%s_B)", this.getName()),
            this.extractRelevantDataAsStream(requestsB).mapToDouble(Double::doubleValue).average().orElse(0.0)
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
            "Response time (ms)"
    );
    }

    @Override
    public String getInfoSummary(List<URLRequest> requests) {
        var relevant = this.extractRelevantData(requests);
        return String.format("%d (average %.2fms reponse time)", relevant.size(), 
            relevant.stream().mapToDouble(d -> d).average().orElse(0));
    }

}
