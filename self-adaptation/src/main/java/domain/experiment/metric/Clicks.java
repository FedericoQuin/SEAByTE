package domain.experiment.metric;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import domain.URLRequest;

public class Clicks extends Metric<Boolean, Double> {

    public Clicks() {
        super("Clicks");
    }

    @Override
    public Predicate<URLRequest> filterFunction() {
        return x -> x.getTarget().equals("/recommendation") || 
            x.getTarget().startsWith("/recommendation/info");
    }

    @Override
    public Stream<Boolean> extractRelevantDataAsStream(List<URLRequest> requests) {
        Map<String, List<URLRequest>> requestsPerOrigin = requests.stream()
            .collect(Collectors.groupingBy(URLRequest::getOrigin));

        return requestsPerOrigin.entrySet().stream()
            .map(Map.Entry::getValue)
            .flatMap(l -> IntStream.range(0, l.size() - 1)
                .mapToObj(i -> new URLRequestPair(l.get(i), l.get(i + 1)))
                .map(p -> {
                    // Three options
                    String t1 = p.req1.getTarget();
                    String t2 = p.req2.getTarget();
    
                    if (t1.equals("/recommendation") && t2.startsWith("/recommendation/info")) {
                        // Recommendation followed by info -> positive sample
                        return Optional.of(true);
                    } else if (t1.startsWith("/recommendation/info") && t2.equals("/recommendation")) {
                        // Info followed by recommendation -> sample that is already processed
                        return Optional.<Boolean>empty();
                    } else {
                        // Two recommendations following eachother -> negative sample (ignored info)
                        return Optional.of(false);
                    }
                })
                .filter(Optional::isPresent)
                .map(Optional::get)
            );
    }

    @Override
    public Map<String, Double> getExtraMetrics(List<URLRequest> requestsA, List<URLRequest> requestsB) {
        var relA = this.extractRelevantData(requestsA);
        var relB = this.extractRelevantData(requestsB);

        Logger.getLogger(Clicks.class.getName()).info(String.format("mean(%s_A)", this.getName()));
        Logger.getLogger(Clicks.class.getName()).info(String.format("%f", relA.stream().filter(b -> b).count() / (double) relA.size()));

        return Map.of(
            String.format("mean(%s_A)", this.getName()),
            relA.stream().filter(b -> b).count() / (double) relA.size(), 
            String.format("mean(%s_B)", this.getName()),
            relB.stream().filter(b -> b).count() / (double) relB.size()
        );
    }

    @Override
    public String getPlotDataTyped(List<Boolean> requestsA, List<Boolean> requestsB) {
        return String.format("{\"positivesA\": %d, \"positivesB\": %d, \"negativesA\": %d, \"negativesB\": %d, \"positiveName\": \"%s\", \"negativeName\": \"%s\", \"title\": \"%s\", \"type\": \"bar\"}", 
            requestsA.stream().mapToInt(b -> b ? 1 : 0).sum(),
            requestsB.stream().mapToInt(b -> b ? 1 : 0).sum(),
            requestsA.stream().mapToInt(b -> b ? 0 : 1).sum(),
            requestsB.stream().mapToInt(b -> b ? 0 : 1).sum(),
            "Requested info",
            "Ignored recommendation",
            "Clicks (clicked vs. ignored recommendation)");
    }

    @Override
    public String getInfoSummary(List<URLRequest> requests) {
        var relevant = this.extractRelevantData(requests);
        return String.format("%d (average %s%% clicks)", relevant.size(), 
            relevant.isEmpty() ? "..." :
                String.format("%.2f", relevant.stream().filter(b -> b).count() * 100 / (double) relevant.size()));
    }



    public record URLRequestPair(URLRequest req1, URLRequest req2) {}


}
