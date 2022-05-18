package domain.experiment;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import domain.experiment.StatisticalTest.StatisticalResult;

public class TransitionRule {
    private String name;
    
    private String fromExperiment;
    private String toExperiment;
    private List<ConditionTransitionRule> conditions;


    private TransitionRule(String name, String fromExperiment, String toExperiment, List<ConditionTransitionRule> conditions) {
        this.name = name;
        this.fromExperiment = fromExperiment;
        this.toExperiment = toExperiment;
        this.conditions = conditions;
    }


    public String getName() {
        return this.name;
    }

    public String getFromExperiment() {
        return this.fromExperiment;
    }

    public String getToExperiment() {
        return this.toExperiment;
    }


    public List<String> getConditions() {
        return this.conditions.stream().map(ConditionTransitionRule::toString).toList();
    }


    public <T extends Comparable<T>> boolean isSatisfied(Map<String, StatisticalResult> statVariables, 
            Map<String, T> extraVariables) {
        return conditions.stream()
            .map(c -> c.evaluate(statVariables, extraVariables))
            .reduce((b1, b2) -> b1 && b2).orElse(true);
    }



    public abstract static class ConditionTransitionRule {
        public abstract <T extends Comparable<T>> boolean evaluate(Map<String, StatisticalResult> statVariables, 
                Map<String, T> extraVariables);

        public enum Operator {
            Equal("=="),
            NonEqual("!="),
            LessThan("<"),
            LessThanOrEqual("<="),
            GreatherThan(">"),
            GreatherThanOrEqual(">=");

            private String rep;

            private Operator(String rep) {
                this.rep = rep;
            }

            public static Operator getOperator(String rep) {
                return Arrays.stream(Operator.values())
                    .filter(o -> o.rep.equals(rep))
                    .findFirst().orElseThrow();
            }

            public String getRep() {
                return this.rep;
            }

            public <T extends Comparable<T>> boolean execute(T first , T second) {
                return switch(this) {
                    case Equal -> first.compareTo(second) == 0;
                    case NonEqual -> first.compareTo(second) != 0;
                    case LessThan -> first.compareTo(second) < 0;
                    case LessThanOrEqual -> first.compareTo(second) <= 0;
                    case GreatherThan -> first.compareTo(second) > 0;
                    case GreatherThanOrEqual -> first.compareTo(second) >= 0;
                };
            }
        }
    }

    public static class NormalConditionTransitionRule extends ConditionTransitionRule {
        private String variableName1;
        private Operator operator;
        private String variableName2;

        public NormalConditionTransitionRule(String variableName1, Operator op, String variableName2) {
            this.variableName1 = variableName1;
            this.operator = op;
            this.variableName2 = variableName2;
        }

        @Override
        public <T extends Comparable<T>> boolean evaluate(Map<String, StatisticalResult> statVariables,
                Map<String, T> extraVariables) {
            if (extraVariables.containsKey(this.variableName1) && extraVariables.containsKey(this.variableName2)) {
                return this.operator.execute(extraVariables.get(this.variableName1), 
                    extraVariables.get(this.variableName2));
            }
            Logger.getLogger(NormalConditionTransitionRule.class.getName())
                .severe(String.format("Required variables '%s' and '%s' not present in the provided map of variables.", 
                this.variableName1, this.variableName2));
            return false;
        }


        @Override
        public String toString() {
            return String.format("%s %s %s", this.variableName1, this.operator.getRep(), this.variableName2);
        }
    }

    public static class StatisticalConditionTransitionRule extends ConditionTransitionRule {
        private String variableName;
        private Operator operator;
        private StatisticalResult result;

        public StatisticalConditionTransitionRule(String variableName, Operator op, StatisticalResult result) {
            this.variableName = variableName;
            this.operator = op;
            this.result = result;
        }

        @Override
        public <T extends Comparable<T>> boolean evaluate(Map<String, StatisticalResult> statVariables,
        Map<String, T> extraVariables) {
            if (statVariables.containsKey(this.variableName)) {
                return this.operator.execute(statVariables.get(this.variableName), this.result);
            }
            Logger.getLogger(StatisticalConditionTransitionRule.class.getName())
                .severe(String.format("Required statistical variable '%s' not present in the provided map of variables.", 
                this.variableName));
            return false;
        }

        @Override
        public String toString() {
            return String.format("%s %s %s", this.variableName, this.operator.getRep(), this.result.name());
        }
    }



    public static class TransitionRuleBuilder {
        private String name;
    
        private String fromExperiment;
        private String toExperiment;
        private List<ConditionTransitionRule> conditions;

        public TransitionRuleBuilder(String name, String fromExperiment, String toExperiment) {
            this.name = name;
            this.fromExperiment = fromExperiment;
            this.toExperiment = toExperiment;
            this.conditions = new ArrayList<>();
        }

        public TransitionRuleBuilder withCondition(ConditionTransitionRule condition) {
            this.conditions.add(condition);
            return this;
        }

        public TransitionRule build() {
            return new TransitionRule(name, fromExperiment, toExperiment, conditions);
        }
        
    }
}
