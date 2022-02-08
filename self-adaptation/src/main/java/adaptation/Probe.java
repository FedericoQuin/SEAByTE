package adaptation;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import com.google.gson.JsonParser;

import domain.URLRequest;

public class Probe implements IProbe {

    private Knowledge knowledge;
    private Logger logger = Logger.getLogger(Probe.class.getName());


    public Probe(FeedbackLoop feedbackLoop) {
        this.knowledge = feedbackLoop.getKnowledge();
    }

    @Override
    public List<URLRequest> getRequestHistory(String ABComponentName, String variant) throws MalformedURLException, IOException {
        // logger.info(Integer.toString(knowledge.getABComponentPort()));
        logger.info(String.format("Getting requests for variant %s.", variant));
        String url = String.format(
            "http://localhost:%d/adaptation/history?variant=%s&removeAfter=true",
            knowledge.getABComponentPort(knowledge.getABComponentName()), variant);
        // logger.info(url);
        InputStream response = new URL(url).openConnection().getInputStream();
        String data = new String(response.readAllBytes(), StandardCharsets.UTF_8);
        var jArr = JsonParser.parseString(data).getAsJsonArray();

        List<URLRequest> result = new ArrayList<>();

        for (var element : jArr) {
            var obj = element.getAsJsonObject();
            result.add(new URLRequest(
                obj.get("requestedUrl").getAsString(),
                obj.get("clientId").getAsString(),
                obj.get("duration").getAsDouble()
            ));
        }
        // logger.info(data);
        logger.fine(String.format("Retrieved %d requests.", result.size()));

        return result;
    }

    
}
