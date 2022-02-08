package adaptation;

import java.util.Optional;

import util.Weight;

public class ABSystem {
    // Contains two models of the system which are being monitored
    SystemModel model1;
    SystemModel model2;


    // The configuration for the AB system (high level router in this case)
    ABConfiguration configuration; 



    public ABSystem() {
        this.model1 = null;
        this.model2 = null;
        this.configuration = new ABConfiguration(Weight.fromPercentage(100), Weight.fromPercentage(0));
    }



    public Optional<SystemModel> getModel1() {
        return Optional.ofNullable(this.model1);
    }

    public Optional<SystemModel> getModel2() {
        return Optional.ofNullable(this.model2);
    }

    public static class ABConfiguration {
        public Weight weightA;
        public Weight weightB;


        public ABConfiguration(Weight weightA, Weight weightB) {
            this.weightA = weightA;
            this.weightB = weightB;
        }
    }
}
