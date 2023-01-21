package adaptation.mape;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.logging.Logger;

import adaptation.FeedbackLoop;
import adaptation.Knowledge;
import domain.Constants;
import domain.URLRequest;
import domain.experiment.Experiment;
import domain.experiment.statistic.StatisticalTest;
import domain.experiment.statistic.StatisticalTest.StatisticalResultWithPValue;

public class Analyzer {

    private static Logger logger = Logger.getLogger(Analyzer.class.getName());
    
    private Knowledge knowledge;
    
    public Analyzer(FeedbackLoop feedbackLoop) {
        this.knowledge = feedbackLoop.getKnowledge();
    }


    public boolean analyze() {
        // Check if ample samples are available for both variants before we make further decisions
        int requiredSamples = this.knowledge.getCurrentExperiment().get().getStatisticalTest().getSamples();
        Experiment<?> experiment = this.knowledge.getCurrentExperiment().get();
        StatisticalTest<?> test = experiment.getStatisticalTest();

        // Logger.getLogger(Analyzer.class.getName()).info(
        //     Long.toString(this.knowledge.getRequestsA().stream().filter(test.getMetric().filterFunction()).count()));

        if (test.getMetric().extractRelevantDataAsStream(this.knowledge.getRequestsA()).count() >= requiredSamples 
                && test.getMetric().extractRelevantDataAsStream(this.knowledge.getRequestsB()).count() >= requiredSamples) {
    
            StatisticalResultWithPValue result = test.validateNullHypothesis(this.knowledge.getRequestsA(), this.knowledge.getRequestsB());
    
            logger.info(String.format("Observed p value: %.6f", result.pvalue()));
            logger.info(String.format("Result of test: %s", result.result().toString()));
            this.knowledge.setStatisticalResult(result.result());

            // Store results for later use
            storeExperimentResultsToFile(result);

            return true;
        }

        return false;
    }

    private void storeExperimentResultsToFile(StatisticalResultWithPValue result) {
        this.storeExperimentResultsToFile(
            result, 
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")
                .withZone(ZoneId.systemDefault())
                .format(Instant.now())
        );
    }

    private void storeExperimentResultsToFile(StatisticalResultWithPValue result, String date) {
        List<URLRequest> requestsA = this.knowledge.getRequestsA();
        List<URLRequest> requestsB = this.knowledge.getRequestsB();

        String experimentName = this.knowledge.getCurrentExperiment().get().getName();
        String setupName = this.knowledge.getCurrentSetup().get().getName();
        int requiredSamples = this.knowledge.getCurrentExperiment().get().getStatisticalTest().getSamples();
        StatisticalTest<?> test = this.knowledge.getCurrentExperiment().get().getStatisticalTest();

        List<?> relevantRequestsA = test.getMetric().extractRelevantData(requestsA);
        List<?> relevantRequestsB = test.getMetric().extractRelevantData(requestsB);

        // Create output folder for the experiment that just finished
        String folderName = "";
        int addendum = 0;

        do {
            folderName = String.format("%s_%s%s", date, experimentName, addendum == 0 ? "" : String.format(" (%d)", addendum));
            addendum++;
        } while (Path.of(Constants.OUTPUT_DIRECTORY, folderName).toFile().isDirectory());

        File outputDir = Path.of(Constants.OUTPUT_DIRECTORY, folderName).toFile();
        outputDir.mkdir();


        // Write away Metadata
        try {
            File file = Path.of(outputDir.getPath(), "metadata.txt").toFile();
            file.createNewFile();

            FileWriter writer = new FileWriter(file);
            writer.write(String.format("Setup name = \"%s\"\n", setupName));
            writer.write(String.format("Experiment name = \"%s\"\n", experimentName));
            writer.write(String.format("Statistical test used = \"%s\"\n", test.getClass().getName()));
            writer.write(String.format("Required samples for statistical test = %d\n", requiredSamples));
            writer.write(String.format("Resulting measured p value statistical test = %.6f\n", result.pvalue()));
            writer.write(String.format("Result of application statistical test (required p value of %.4f) = \"%s\"\n", test.getPValue(), result.result().name()));
            writer.write(String.format("Number of raw samples variant A = %d\n", requestsA.size()));
            writer.write(String.format("Number of raw samples variant B = %d\n", requestsB.size()));
            writer.write(String.format("Number of relevant samples variant A = %d\n", relevantRequestsA.size()));
            writer.write(String.format("Number of relevant samples variant B = %d\n", relevantRequestsB.size()));

            writer.write("\nExtra metrics: \n");
            
            for (var entry : test.getMetric().getExtraMetrics(requestsA, requestsB).entrySet()) {
                writer.write(String.format("\t%s = %.4f\n", entry.getKey(), entry.getValue()));
            }

            writer.close();
        } catch (IOException e) {
            logger.severe(String.format("Failed to write metadata of the experiment to file. Exception thrown:"));
            logger.severe(e.toString());
        }

        // Write away all samples version A
        try {
            File file = Path.of(outputDir.getPath(), "allSamplesVariantA.csv").toFile();
            file.createNewFile();

            FileWriter writer = new FileWriter(file);
            writer.write(URLRequest.getCSVOutputHeader() + "\n");
            for (URLRequest request : requestsA) {
                writer.write(request.toCsvFormat() + "\n");
            }
            
            writer.close();
        } catch (IOException e) {
            logger.severe(String.format("Failed to write all url requests of variant A to file. Exception thrown:"));
            logger.severe(e.toString());
        }

        // Write away all samples version B
        try {
            File file = Path.of(outputDir.getPath(), "allSamplesVariantB.csv").toFile();
            file.createNewFile();

            FileWriter writer = new FileWriter(file);
            writer.write(URLRequest.getCSVOutputHeader() + "\n");
            for (URLRequest request : requestsB) {
                writer.write(request.toCsvFormat() + "\n");
            }
            
            writer.close();
        } catch (IOException e) {
            logger.severe(String.format("Failed to write all url requests of variant B to file. Exception thrown:"));
            logger.severe(e.toString());
        }

        // Write away all relevant samples version A
        try {
            File file = Path.of(outputDir.getPath(), "relevantDataVariantA.csv").toFile();
            file.createNewFile();

            FileWriter writer = new FileWriter(file);
            writer.write(String.format("\"%s\"\n", test.getMetric().getName()));
            for (Object sample : relevantRequestsA) {
                writer.write(sample.toString() + "\n");
            }
            
            writer.close();
        } catch (IOException e) {
            logger.severe(String.format("Failed to write relevant data of variant A to file. Exception thrown:"));
            logger.severe(e.toString());
        }
        
        // Write away all relevant samples version B
        try {
            File file = Path.of(outputDir.getPath(), "relevantDataVariantB.csv").toFile();
            file.createNewFile();

            FileWriter writer = new FileWriter(file);
            writer.write(String.format("\"%s\"\n", test.getMetric().getName()));
            for (Object sample : relevantRequestsB) {
                writer.write(sample.toString() + "\n");
            }
            
            writer.close();
        } catch (IOException e) {
            logger.severe(String.format("Failed to write relevant data of variant B to file. Exception thrown:"));
            logger.severe(e.toString());
        }


        // Write away plot data of both versions
        try {
            File file = Path.of(outputDir.getPath(), "plotData.txt").toFile();
            file.createNewFile();

            FileWriter writer = new FileWriter(file);
            writer.write(test.getMetric().getPlotData(requestsA, requestsB) + "\n");
            writer.close();
        } catch (IOException e) {
            logger.severe(String.format("Failed to write plot data of both variants to file. Exception thrown:"));
            logger.severe(e.toString());
        }
    }
}
