

import { sendSetupToServer } from '../setup.js'
import { sendUserProfileToServer } from '../profile.js'
import { sendExperimentToServer } from '../experiment.js'
import { sendTransitionRuleToServer } from '../rule.js'
import { Setup, Experiment, UserProfile, TransitionRule, DockerService, LocustUser, EnvironmentVariable, 
    ABAssignment, StatisticalTest, Condition, Pipeline } from './domain-classes.js'
import { sendPipelineToServer } from '../pipeline.js';


export async function addDefaultPipelineSeams2022() {

    // Default pipeline and its subcomponents
    await sendSetupToServer(new Setup(
        'Recommendation_upgrade',
        new DockerService('ws-recommendation-service-1-0-0', 'ws-recommendation-service-image:1.0.0'),
        new DockerService('ws-recommendation-service-1-1-0', 'ws-recommendation-service-image:1.1.0'),
        new DockerService('ws-recommendation-service', 'ab-component-image:latest'),
        'ws-recommendation-service'
    ));

    await sendUserProfileToServer(
        new UserProfile('Standard', [
            new LocustUser('RegularUser', 80, [
                new EnvironmentVariable('clickChanceA', 0.1),
                new EnvironmentVariable('clickChanceB', 0.2),
                new EnvironmentVariable('purchaseChanceA', 0.05),
                new EnvironmentVariable('purchaseChanceB', 0.15),
            ])
        ]
    ));

    await sendExperimentToServer(new Experiment(
        'Upgrade v1.0.0 - v1.1.0',
        'Recommendation_upgrade',
        'Standard',
        100,
        new ABAssignment(50, 50),
        ['ResponseTimeRecommendation_A', 'ResponseTimeRecommendation_B'],
        new StatisticalTest(
            new Condition('ResponseTimeRecommendation_A', '==', 'ResponseTimeRecommendation_B'),
            0.025,
            'welsh-t-test',
            'result-welsh-t-test'
        )
    ));


    await sendExperimentToServer(new Experiment(
        'Clicks v1.0.0 - v1.1.0',
        'Recommendation_upgrade',
        'Standard',
        100,
        new ABAssignment(30, 70),
        ['ClicksRecommendation_A', 'ClicksRecommendation_B'],
        new StatisticalTest(
            new Condition('ClicksRecommendation_A', '==', 'ClicksRecommendation_B'),
            0.025,
            'one-proportional-test',
            'result-clicks'
        )
    ));


    await sendExperimentToServer(new Experiment(
        'Purchases v1.0.0 - v1.1.0',
        'Recommendation_upgrade',
        'Standard',
        100,
        new ABAssignment(20, 80),
        ['PurchasesRecommendation_A', 'PurchasesRecommendation_B'],
        new StatisticalTest(
            new Condition('PurchasesRecommendation_A', '==', 'PurchasesRecommendation_B'),
            0.025,
            'one-proportional-test',
            'result-purchases'
        )
    ));

    await sendTransitionRuleToServer(new TransitionRule(
        'Upgrade-succes-inconclusive',
        'Upgrade v1.0.0 - v1.1.0',
        'Clicks v1.0.0 - v1.1.0',
        [new Condition('result-welsh-t-test', '!=', 'reject')]
    ));


    await sendTransitionRuleToServer(new TransitionRule(
        'Upgrade-succes-reject',
        'Upgrade v1.0.0 - v1.1.0',
        'Clicks v1.0.0 - v1.1.0',
        [new Condition('result-welsh-t-test', '==', 'reject'), 
            new Condition('mean(ResponseTimeRecommendation_A)', '>=', 'mean(ResponseTimeRecommendation_B)')]
    ));


    await sendTransitionRuleToServer(new TransitionRule(
        'Upgrade-fail',
        'Upgrade v1.0.0 - v1.1.0',
        'end',
        [new Condition('result-welsh-t-test', '==', 'reject'), 
            new Condition('mean(ResponseTimeRecommendation_A)', '<', 'mean(ResponseTimeRecommendation_B)')]
    ));


    await sendTransitionRuleToServer(new TransitionRule(
        'Clicks-succes-reject',
        'Clicks v1.0.0 - v1.1.0',
        'Purchases v1.0.0 - v1.1.0',
        [new Condition('result-clicks', '==', 'reject'),
            new Condition('mean(ClicksRecommendation_A)', '<=', 'mean(ClicksRecommendation_B)')]
    ));

    await sendTransitionRuleToServer(new TransitionRule(
        'Clicks-succes-inconclusive',
        'Clicks v1.0.0 - v1.1.0',
        'Purchases v1.0.0 - v1.1.0',
        [new Condition('result-clicks', '==', 'inconclusive'),
            new Condition('mean(ClicksRecommendation_A)', '<=', 'mean(ClicksRecommendation_B)')]
    ));

    await sendTransitionRuleToServer(new TransitionRule(
        'Clicks-fail-inconclusive',
        'Clicks v1.0.0 - v1.1.0',
        'end',
        [new Condition('result-clicks', '==', 'inconclusive'),
            new Condition('mean(ClicksRecommendation_A)', '>', 'mean(ClicksRecommendation_B)')]
    ));

    await sendTransitionRuleToServer(new TransitionRule(
        'Clicks-fail-reject',
        'Clicks v1.0.0 - v1.1.0',
        'end',
        [new Condition('result-clicks', '==', 'reject'),
            new Condition('mean(ClicksRecommendation_A)', '>', 'mean(ClicksRecommendation_B)')]
    ));


    await sendTransitionRuleToServer(new TransitionRule(
        'Purchases-succes-reject',
        'Purchases v1.0.0 - v1.1.0',
        'end',
        [new Condition('result-purchases', '==', 'reject'),
            new Condition('mean(PurchasesRecommendation_A)', '<=', 'mean(PurchasesRecommendation_B)')]
    ));

    await sendTransitionRuleToServer(new TransitionRule(
        'Purchases-succes-inconclusive',
        'Purchases v1.0.0 - v1.1.0',
        'end',
        [new Condition('result-purchases', '==', 'inconclusive'),
            new Condition('mean(PurchasesRecommendation_A)', '<=', 'mean(PurchasesRecommendation_B)')]
    ));

    await sendTransitionRuleToServer(new TransitionRule(
        'Purchases-fail-reject',
        'Purchases v1.0.0 - v1.1.0',
        'end',
        [new Condition('result-purchases', '==', 'reject'),
            new Condition('mean(PurchasesRecommendation_A)', '>', 'mean(PurchasesRecommendation_B)')]
    ));

    await sendTransitionRuleToServer(new TransitionRule(
        'Purchases-fail-inconclusive',
        'Purchases v1.0.0 - v1.1.0',
        'end',
        [new Condition('result-purchases', '==', 'inconclusive'),
            new Condition('mean(PurchasesRecommendation_A)', '>', 'mean(PurchasesRecommendation_B)')]
    ));

    await sendPipelineToServer(new Pipeline(
        'Default_scenario',
        ['Upgrade v1.0.0 - v1.1.0', 'Clicks v1.0.0 - v1.1.0', 'Purchases v1.0.0 - v1.1.0'],
        ['Purchases-fail-reject', 'Clicks-succes-inconclusive', 'Purchases-succes-reject', 'Clicks-fail-inconclusive', 'Upgrade-succes-reject', 'Clicks-fail-reject', 'Purchases-fail-inconclusive', 'Upgrade-fail', 'Upgrade-succes-inconclusive', 'Clicks-succes-reject', 'Purchases-succes-inconclusive'],
        [],
        [],
        'Upgrade v1.0.0 - v1.1.0'
    ));
}

