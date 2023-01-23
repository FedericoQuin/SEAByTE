package adaptation.mape;

import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import adaptation.FeedbackLoop;
import domain.Constants;
import domain.URLRequest.ABInstance;

public class Monitor {

    private FeedbackLoop feedbackLoop;
    
    
    private ScheduledExecutorService pollingService;


    public Monitor(FeedbackLoop feedbackLoop) {
        this.feedbackLoop = feedbackLoop;
        this.pollingService = null;
    }


    public void monitor() {
        // Update monitored samples in the knowledge
        
    }


    public void startPolling() {
        // Start a periodic function which polls the underlying system (here: our web-store)
        this.pollingService = Executors.newSingleThreadScheduledExecutor();
        this.pollingService.scheduleAtFixedRate(this::poll, Constants.MONITOR_POLLING_FREQUENCY, 
            Constants.MONITOR_POLLING_FREQUENCY, TimeUnit.SECONDS);
    }

    public void stopPolling() {
        if (this.pollingService == null) {
            return;
        }
        this.pollingService.shutdown();
        this.pollingService = null;
    }


    public void poll() {
        var probe = this.feedbackLoop.getProbe();
        var knowledge = this.feedbackLoop.getKnowledge();
        var filter = this.feedbackLoop.getCurrentExperiment().get().getStatisticalTest().getMetric().filterFunction();

        try {
            for (ABInstance instance : ABInstance.values()) {
                knowledge.addRequests(probe.getRequestHistory(knowledge.getABComponentName(), instance.name())
                    .stream().filter(filter).toList(), instance);
            }
        } catch (IOException e) {
            Logger.getLogger(Monitor.class.getName()).severe("Failed to poll AB server " + e.getClass().getName());
            e.printStackTrace();
            this.feedbackLoop.stopFeedbackLoop();
        }
    }
}
