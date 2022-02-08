


export class DockerService {
    constructor(serviceName, imageName) {
        this.serviceName = serviceName;
        this.imageName = imageName;
    }
}

export class Setup {
    constructor(name, versionA, versionB, abComponent, decommission) {
        this.name = name;
        this.versionA = versionA;
        this.versionB = versionB;
        this.abComponent = abComponent;
        this.decommission = decommission;
    }

    static constructFromForm(formData) {
        return new Setup(
            formData.get('nameSetup'),
            new DockerService(formData.get('commission-A-container'), formData.get('commission-A-image')),
            new DockerService(formData.get('commission-B-container'), formData.get('commission-B-image')),
            new DockerService(formData.get('ab-component-container'), formData.get('ab-component-image')),
            formData.get('decommission')
        );
    }
}




export class Condition {
    constructor(leftOperand, operator, rightOperand) {
        this.leftOperand = leftOperand;
        this.operator = operator;
        this.rightOperand = rightOperand;
    }
}

export class StatisticalTest {
    constructor(nullHypothesis, pValue, type, resultingVariable) {
        this.nullHypothesis = nullHypothesis;
        this.pValue = pValue;
        this.type = type;
        this.resultingVariable = resultingVariable;
    }
}

export class ABAssignment {
    constructor(wA, wB) {
        this.weightA = wA;
        this.weightB = wB;
    }
}


export class Experiment {
    constructor(name, variantA, variantB, userProfile, samples, abAssignment, metrics, statisticalTest) {
        this.name = name;
        this.variantA = variantA;
        this.variantB = variantB;
        this.userProfile = userProfile;
        this.samples = samples;
        this.abAssignment = abAssignment;
        this.metrics = metrics;
        this.statisticalTest = statisticalTest;
    }


    static constructFromForm(formData) {
        return new Experiment(
            formData.get('nameExperiment'),
            formData.get('variant-A'),
            formData.get('variant-B'),
            formData.get('user-profile'),
            formData.get('samples'),
            new ABAssignment(formData.get('a-assignment'), formData.get('b-assignment')),
            formData.get('metrics').split(',').map(x => x.trim()),
            new StatisticalTest(
                new Condition(formData.get('null-hypothesis-left'), formData.get('null-hypothesis-operator'), formData.get('null-hypothesis-right')),
                formData.get('p-value'),
                formData.get('type-statistical-test'),
                formData.get('resulting-variable-name')
            )
        );
    }
}


export class UserProfile {
    constructor(name, locustUsers, extraVars) {
        this.name = name;
        this.locustUsers = locustUsers;
        this.extraVars = extraVars;
    }

    // static constructFromForm(formData, extraVariables) {
    //     return new UserProfile(
    //         formData.get('nameProfile'),
    //         formData.get('locustUser'),
    //         Number.parseInt(formData.get('numberOfUsers')),
    //         extraVariables
    //     );
    // }
}







export class TransitionRule {
    constructor(name, fromExperiment, toExperiment, conditions) {
        this.name = name;
        this.fromExperiment = fromExperiment;
        this.toExperiment = toExperiment;
        this.conditions = conditions;
    }

    static constructFromForm(formData) {
        return new TransitionRule(
            formData.get('nameRule'),
            formData.get('fromExperiment'),
            formData.get('toExperiment'),
            [new Condition(
                formData.get('conditionLeftOperand'),
                formData.get('conditionOperator'),
                formData.get('conditionRightOperand')
            )]
        );
    }
}

