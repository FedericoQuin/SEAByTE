package domain.experiment.metric;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import domain.URLRequest;

public class PurchasesCheckout extends Metric<Boolean, Double> {

    private static final String BUYING_URL = "/checkout/buy";
    private static final String CLEARING_URL = "/checkout/clear";

    public PurchasesCheckout() {
        super("PurchasesCheckout");
    }

    @Override
    public Predicate<URLRequest> filterFunction() {
        return x -> x.getTarget().startsWith(PurchasesCheckout.BUYING_URL) ||
            x.getTarget().startsWith(PurchasesCheckout.CLEARING_URL);
    }

    @Override
    public Stream<Boolean> extractRelevantDataAsStream(List<URLRequest> requests) {
        Map<String, List<URLRequest>> requestsPerOrigin = requests.stream()
            .collect(Collectors.groupingBy(URLRequest::getOrigin));

        return requestsPerOrigin.entrySet().stream()
            .map(Map.Entry::getValue)
            .flatMap(l -> l.stream()
                .map(r -> {
                    if (r.getTarget().startsWith(PurchasesCheckout.BUYING_URL)) {
                        return Optional.of(true);
                    } else if (r.getTarget().startsWith(PurchasesCheckout.CLEARING_URL)) {
                        return Optional.of(false);
                    }
                    return Optional.<Boolean>empty();
                })
                .filter(Optional::isPresent)
                .map(Optional::get)
            );
    }

    @Override
    public Map<String, Double> getExtraMetrics(List<URLRequest> requestsA, List<URLRequest> requestsB) {
        var relA = this.extractRelevantData(requestsA);
        var relB = this.extractRelevantData(requestsB);

        return Map.of(
            String.format("mean(%s_A)", this.getName()),
            relA.stream().filter(b -> b).count() / (double) relA.size(), 
            String.format("mean(%s_B)", this.getName()),
            relB.stream().filter(b -> b).count() / (double) relB.size()
        );
    }

    
    @Override
    public List<String> getExtraMetricNames() {
        return List.of(String.format("mean(%s_A)", this.getName()), String.format("mean(%s_B)", this.getName()));
    }


    @Override
    public String getPlotDataTyped(List<Boolean> requestsA, List<Boolean> requestsB) {
        return String.format("{\"positivesA\": %d, \"positivesB\": %d, \"negativesA\": %d, \"negativesB\": %d, \"positiveName\": \"%s\", \"negativeName\": \"%s\", \"title\": \"%s\", \"type\": \"bar\"}", 
            requestsA.stream().mapToInt(b -> b ? 1 : 0).sum(),
            requestsB.stream().mapToInt(b -> b ? 1 : 0).sum(),
            requestsA.stream().mapToInt(b -> b ? 0 : 1).sum(),
            requestsB.stream().mapToInt(b -> b ? 0 : 1).sum(),
            "Made purchase",
            "Made no purchase",
            "Purchases (purchase vs. no purchase in the web store)");
    }

    @Override
    public String getInfoSummary(List<URLRequest> requests) {
        var relevant = this.extractRelevantData(requests);
        return String.format("%d (average %s%% purchases)", relevant.size(), 
            relevant.isEmpty() ? "..." :
                String.format("%.2f", relevant.stream().filter(b -> b).count() * 100 / (double) relevant.size()));
    }



    public record URLRequestPair(URLRequest req1, URLRequest req2) {}


}
