package dashboard.controller;

import java.util.Collection;
import java.util.List;
import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import com.google.gson.JsonParser;

import dashboard.model.ABRepository;
import domain.experiment.StatisticalTest.StatisticalResult;
import domain.experiment.TransitionRule;
import domain.experiment.TransitionRule.ConditionTransitionRule.Operator;
import domain.experiment.TransitionRule.NormalConditionTransitionRule;
import domain.experiment.TransitionRule.StatisticalConditionTransitionRule;
import domain.experiment.TransitionRule.TransitionRuleBuilder;

@Controller
@RequestMapping("/rule")
public class RuleController {
    private Logger logger = Logger.getLogger(RuleController.class.getName());

    @Autowired
    private ABRepository repository;


    @RequestMapping("")
    public String index() {
        this.logger.info("Requested transition rule web page.");
        return "rule.html";
    }


    
    @PostMapping(value="/newRule")
    @ResponseStatus(value=HttpStatus.OK)
    public void addNewRule(@RequestBody String data) {
        this.logger.info("Adding new rule.");

        var root = JsonParser.parseString(data).getAsJsonObject();

        TransitionRuleBuilder builder = new TransitionRuleBuilder(root.get("name").getAsString(), 
            root.get("fromExperiment").getAsString(), 
            root.get("toComponent").getAsString());
        

        // Reserved keywords for transition rules: [reject, inconclusive]
        List<String> keywords = List.of("reject", "inconclusive");

        root.get("conditions").getAsJsonArray().forEach(e -> {
            var obj = e.getAsJsonObject();

            var operator = Operator.getOperator(obj.get("operator").getAsString());
            String leftOperand = obj.get("leftOperand").getAsString();
            String rightOperand = obj.get("rightOperand").getAsString();

            if (keywords.stream().anyMatch(s -> s.equals(rightOperand.toLowerCase()))) {
                // Statistical condition
                builder.withCondition(new StatisticalConditionTransitionRule(
                    leftOperand, operator, StatisticalResult.getStatisticalResult(rightOperand)));
            } else {
                builder.withCondition(new NormalConditionTransitionRule(leftOperand, operator, rightOperand));
            }
        });
        
        repository.addTransitionRule(builder.build());
    }

    @GetMapping(value="/retrieve")
    public @ResponseBody Collection<TransitionRule> getRules() {
        return repository.getAllTransitionRules();
    } 

}
