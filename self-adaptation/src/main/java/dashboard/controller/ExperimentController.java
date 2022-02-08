package dashboard.controller;

import java.util.Collection;
import java.util.List;
import java.util.logging.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import dashboard.model.ABRepository;
import dashboard.service.ABExperimentService;
import domain.ABSetting;
import domain.experiment.Experiment;
import domain.experiment.ProportionalTest;
import domain.experiment.StatisticalTest;
import domain.experiment.StatisticalTest.NullHypothesis;
import domain.experiment.StatisticalTest.NullHypothesis.Operator;
import domain.experiment.StudentTest;
import domain.experiment.WelshStudentTest;

@Controller
@RequestMapping("/experiment")
public class ExperimentController {
    
	protected Logger logger = Logger.getLogger(ExperimentController.class.getName());

    @Autowired
    private ABExperimentService experimentService;

    @Autowired
    private ABRepository repository;


    @GetMapping(value="")
    public String testPage() {
        logger.info("Requested AB test experiment page");
        return "experiment.html";
    }
    
    @PostMapping(value="/newExperiment")
    @ResponseStatus(value=HttpStatus.OK)
    public void addNewExperiment(@RequestBody String data) {
        logger.info("Adding new experiment.");

        var root = JsonParser.parseString(data).getAsJsonObject();

        var test = root.get("statisticalTest").getAsJsonObject();
        var hypo = test.get("nullHypothesis").getAsJsonObject();

        List<String> metrics = new Gson().fromJson(root.get("metrics").getAsJsonArray(), 
            new TypeToken<List<String>>() {}.getType());

        StatisticalTest<?> statTest = null;

        // Supported tests for now are limited
        String testName = root.get("statisticalTest").getAsJsonObject().get("type").getAsString().toLowerCase();
        switch(testName) {
            case "t-test" -> {
                if (!hypo.get("operator").getAsString().equals("==")) {
                    throw new RuntimeException("The operator for the student statistical test has to be '=='.");
                }
                statTest = new StudentTest(
                    new NullHypothesis<>(
                        Operator.Equal,
                        hypo.get("leftOperand").getAsString(),
                        hypo.get("rightOperand").getAsString()
                    ),
                    test.get("pValue").getAsDouble(),
                    root.get("samples").getAsInt(),
                    test.get("resultingVariable").getAsString(),
                    testName,
                    metrics);
            }
            case "welsh-t-test" -> {
                if (!hypo.get("operator").getAsString().equals("==")) {
                    throw new RuntimeException("The operator for the welsh student statistical test has to be '=='.");
                }
                statTest = new WelshStudentTest(
                    new NullHypothesis<>(
                        Operator.Equal,
                        hypo.get("leftOperand").getAsString(),
                        hypo.get("rightOperand").getAsString()
                    ),
                    test.get("pValue").getAsDouble(),
                    root.get("samples").getAsInt(),
                    test.get("resultingVariable").getAsString(),
                    testName,
                    metrics);
            }
            case "one-proportional-test" -> {
                if (!hypo.get("operator").getAsString().equals("==")) {
                    throw new RuntimeException("The operator for the one proportional statistical test has to be '=='.");
                }
                statTest = new ProportionalTest(
                    new NullHypothesis<>(
                        Operator.Equal,
                        hypo.get("leftOperand").getAsString(),
                        hypo.get("rightOperand").getAsString()
                    ),
                    test.get("pValue").getAsDouble(),
                    root.get("samples").getAsInt(),
                    test.get("resultingVariable").getAsString(),
                    testName,
                    metrics);
            }
            default -> throw new RuntimeException(String.format("Unsupported statistical test: %s", testName));
        }

        
        Experiment<?> experiment = new Experiment<>(
            root.get("name").getAsString(),
            root.get("variantA").getAsString(),
            root.get("variantB").getAsString(),
            new ABSetting(
                root.get("abAssignment").getAsJsonObject().get("weightA").getAsInt(),
                root.get("abAssignment").getAsJsonObject().get("weightB").getAsInt()
            ),
            repository.getUserProfile(root.get("userProfile").getAsString()),
            metrics,
            statTest
        );

        experimentService.addExperiment(experiment);
    }

    @GetMapping(value="/retrieve")
    public @ResponseBody Collection<Experiment<?>> getExperiments() {
        return experimentService.getAllExperiments();
    } 
}
