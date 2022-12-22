
import {COLORS} from './modules/constants.js'
import {updateStatus as baseUpdateStatus} from './modules/status.js'




function updateStatusWithDefaultLoader(message, color='#000000') {
    let loader = document.createElement('img');
    loader.src = '/svg/loading.svg';
    loader.style.marginLeft = '5px';

    updateStatus(message, color, loader);
}

function updateStatus(message, color='#000000', loader=null) {
    // let div = document.getElementById('div-status');
    // div.style.borderColor = color;

    let label = document.getElementById('status-label');
    baseUpdateStatus(message, color)
    
    let old_loader = document.getElementById('status-label-loader');

    if (old_loader) {
        old_loader.parentNode.removeChild(old_loader);
    }
    
    if (loader) {
        label.after(loader);
        loader.id = 'status-label-loader';
    }
}







window.populateSetups = () => {
    (async () => {
        let setups = document.getElementById('list-setups');

        let response = await fetch('/setup/retrieve');

        if (response.status == 200) {
            let data = await response.json();

            // TODO order alphabetically
            for (let setup of data) {
                addSetupCollapsible(setups, setup);
            }
        }
        
    })();
}


function addSetupCollapsible(rootElement, setup) {
    rootElement.innerHTML += `\n<div class="grid-item-margin">
        <details>
          <summary>
            <div class="steps bg-light-blue">
              <svg width="22" height="22"><image xlink:href="/svg/info.svg"/></svg>
              <div class="name bg-light-blue">${setup['name']}<div>
            </div>
          </summary>
          <div class="grid-container" style="grid-template-columns: 1fr 5fr; padding-bottom: 10px;">${['Name:', `${setup['name']}`, 
          'Version A:', `${setup['versionA']['serviceName']} (image: ${setup['versionA']['imageName']})`, 
          'Version B:', `${setup['versionB']['serviceName']} (image: ${setup['versionB']['imageName']})`,
          'AB component:', `${setup['abcomponent']['serviceName']} (image: ${setup['abcomponent']['imageName']})`,
          'Decommission:', `${setup['removeService']}`].map(x => `<div>${x}</div>`).join('\n')}</div>
        </details></div>`;
}





window.populateExperiments = () => {
    (async () => {
        let experiments = document.getElementById('list-experiments');

        let response = await fetch('/experiment/retrieve');

        if (response.status == 200) {
            let data = await response.json();

            // TODO order alphabetically?
            for (let experiment of data) {
                const experimentName = experiment['name'];
                addExperimentCollapsible(experiments, experiment);
            }
        }
        
    })();
}


function addExperimentCollapsible(rootElement, experiment) {
    rootElement.innerHTML += `\n<div class="grid-item-margin">
        <details>
        <summary>
            <div class="steps bg-light-blue">
            <svg width="22" height="22"><image xlink:href="/svg/info.svg"/></svg>
            <div class="name bg-light-blue">${experiment['name']}<div>
            </div>
        </summary>
        <div class="grid-container" style="grid-template-columns: 1fr 5fr; padding-bottom: 10px;">
         ${['Name:', `${experiment['name']}`, 
            'Setup:', `${experiment['setup']}`, 
            'AB weights:', `${experiment['absetting']['weightA']} (A) - ${experiment['absetting']['weightB']} (B)`,
            'User profile:', `${experiment['userProfile']['name']} (${(experiment['userProfile']['locustUsers']).map(x => x['numberOfUsers']).reduce((v1, v2) => v1 + v2, 0)} users in total)`,
            'Metrics:', `${experiment['metrics'].join(', ')}`,
            'Statistical test:', `${experiment['statisticalTest']['type']}`,
            'P value: ', `${experiment['statisticalTest']['pvalue']}`,
            'Samples: ', `${experiment['statisticalTest']['samples']}`]
                .map(x => `<div>${x}</div>`).join('\n')}</div>
        </details></div>`;
}





window.populateRules = () => {
    (async () => {
        let rulesList = document.getElementById('list-rules');

        let response = await fetch('/rule/retrieve');

        if (response.status == 200) {
            let data = await response.json();

            // TODO order alphabetically
            for (let rule of data) {
                addRuleCollapsible(rulesList, rule);
            }
        }
        
    })();
}



function addRuleCollapsible(rootElement, rule) {
    rootElement.innerHTML += `\n<div class="grid-item-margin">
        <details>
            <summary>
                <div class="steps bg-light-blue">
                    <svg width="22" height="22"><image xlink:href="/svg/info.svg"/></svg>
                    <div class="name bg-light-blue">${rule['name']}<div>
                </div>
            </summary>
            <div class="grid-container" style="grid-template-columns: 1fr 5fr; padding-bottom: 10px;">
            ${['Name:', `${rule['name']}`, 
               'From experiment:', `${rule['fromExperiment']}`, 
               'To component:', `${rule['toComponent']}`,
               'Conditions:', `${rule['conditions'].join('<br>')}`]
                   .map(x => `<div>${x}</div>`).join('\n')}</div>
        </details></div>`;
}




window.populateSplits = () => {
    (async () => {
        let splitList = document.getElementById('list-splits');

        let response = await fetch('/split/retrieve');

        if (response.status == 200) {
            let data = await response.json();

            // TODO order alphabetically
            for (let split of data) {
                addSplitCollapsible(splitList, split);
            }
        }

    })();
    }

function addSplitCollapsible(rootElement, split) {
    rootElement.innerHTML += `\n<div class="grid-item-margin">
        <details>
            <summary>
                <div class="steps bg-light-blue">
                    <svg width="22" height="22"><image xlink:href="/svg/info.svg"/></svg>
                    <div class="name bg-light-blue">${split['name']}<div>
                </div>
            </summary>
            <div class="grid-container" style="grid-template-columns: 1fr 5fr; padding-bottom: 10px;">
            ${['Name:', `${split['name']}`, 
               'Pipeline 1:', `${split['pipelineName1']}`, 
               'Pipeline 2:', `${split['pipelineName2']}`,
               'Component:', `${split['splitComponent']['imageName']}`]
                   .map(x => `<div>${x}</div>`).join('\n')}</div>
        </details></div>`;
}



window.populatePipelines = () => {
    (async () => {

        let pipelines = document.getElementById('list-pipelines');
        let response = await fetch('/pipeline/retrieve');

        if (response.status == 200) {
            let data = await response.json();

            // TODO order alphabetically?
            for (let pipeline of data) {
                const pipelineName = pipeline['name'];
                addPipelineCollapsible(pipelines, pipeline);
                addInitialPipelineForm(pipelineName);
            }
        }
        
    })();
}

function addPipelineCollapsible(rootElement, pipeline) {
    rootElement.innerHTML += `\n<div class="grid-item-margin">
        <details>
        <summary>
            <div class="steps bg-light-blue">
            <svg width="22" height="22"><image xlink:href="/svg/info.svg"/></svg>
            <div class="name bg-light-blue">${pipeline['name']}<div>
            </div>
        </summary>
        <div class="grid-container" style="grid-template-columns: 1fr 5fr; padding-bottom: 10px;">
        ${['Name:', `${pipeline['name']}`, 
            'Experiments:', `${pipeline['experiments'].map(x => `'${x}'`).join(', ')}`, 
            'Transition rules:', `${pipeline['transitionRules'].map(x => `'${x}'`).join(', ')}`,
            'Population splits:', `${pipeline['populationSplits'].map(x => `'${x}'`).join(', ')}`,
            'Pipelines:', `${pipeline['pipelines'].map(x => `'${x}'`).join(', ')}`,
            'Starting component:', `${pipeline['startingComponent']}`]
                .map(x => `<div>${x}</div>`).join('\n')}</div>
        </details></div>`;
}


function addInitialPipelineForm(pipelineName) {
    let pipelineToRun = document.getElementById('pipelineToRun');
    
    pipelineToRun.insertAdjacentHTML('beforeend', 
        `<option value="${pipelineName}">${pipelineName}</option>`);
}





function startPipeline(pipelineName) {
    (async () => {
        updateStatusWithDefaultLoader(`Starting feedback loop with pipeline '${pipelineName}'...`, COLORS.STATUS_LABEL_COLOR_PENDING);
        const f = await fetch(`adaptation/startPipeline?` + new URLSearchParams({pipelineName: pipelineName}), 
            {method: 'post'});
        const response = f.status;

        if (response == 200) {
            updateStatus('Feedback loop succesfully started.', COLORS.STATUS_LABEL_COLOR_SUCCES);
        } else {
            updateStatus('Failed to start feedback loop.', COLORS.STATUS_LABEL_COLOR_FAIL);
        }
    })();
}

window.startFeedbackLoop = () => {
    const pipeline = document.getElementById('pipelineToRun').value;

    if (!pipeline) {
        updateStatus('Make sure a pipeline is specified.');
        return;
    }

    startPipeline(pipeline);
}



window.stopFeedbackLoop = () => {
    (async () => {
        updateStatusWithDefaultLoader(`Stopping the feedback loop...`, COLORS.STATUS_LABEL_COLOR_PENDING);
        const f = await fetch('adaptation/stop', 
            {method: 'post'});
        const response = f.status;

        if (response == 200) {
            updateStatus('Feedback loop succesfully stopped.', COLORS.STATUS_LABEL_COLOR_SUCCES);
        } else {
            updateStatus('Failed to stop the feedback loop.', COLORS.STATUS_LABEL_COLOR_FAIL);
        }
    })();
}


import {sendSetupToServer} from './setup.js'
import {sendUserProfileToServer} from './profile.js'
import {sendExperimentToServer} from './experiment.js'
import {sendTransitionRuleToServer} from './rule.js'
import {Setup, Experiment, UserProfile, TransitionRule, DockerService, LocustUser, EnvironmentVariable, ABAssignment, StatisticalTest, Condition, Pipeline, PopulationSplit} from './modules/domain-classes.js'
import { sendPipelineToServer } from './pipeline.js';
import { sendPopulationSplitToServer } from './split.js';

window.experimentation = async () => {

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
        ['ResponseTime_A', 'ResponseTime_B'],
        new StatisticalTest(
            new Condition('ResponseTime_A', '==', 'ResponseTime_B'),
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
        ['Clicks_A', 'Clicks_B'],
        new StatisticalTest(
            new Condition('Clicks_A', '==', 'Clicks_B'),
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
        ['Purchases_A', 'Purchases_B'],
        new StatisticalTest(
            new Condition('Purchases_A', '==', 'Purchases_B'),
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
            new Condition('mean(ResponseTime_A)', '>=', 'mean(ResponseTime_B)')]
    ));


    await sendTransitionRuleToServer(new TransitionRule(
        'Upgrade-fail',
        'Upgrade v1.0.0 - v1.1.0',
        'end',
        [new Condition('result-welsh-t-test', '==', 'reject'), 
            new Condition('mean(ResponseTime_A)', '<', 'mean(ResponseTime_B)')]
    ));


    await sendTransitionRuleToServer(new TransitionRule(
        'Clicks-succes-reject',
        'Clicks v1.0.0 - v1.1.0',
        'Purchases v1.0.0 - v1.1.0',
        [new Condition('result-clicks', '==', 'reject'),
            new Condition('mean(Clicks_A)', '<=', 'mean(Clicks_B)')]
    ));

    await sendTransitionRuleToServer(new TransitionRule(
        'Clicks-succes-inconclusive',
        'Clicks v1.0.0 - v1.1.0',
        'Purchases v1.0.0 - v1.1.0',
        [new Condition('result-clicks', '==', 'inconclusive'),
            new Condition('mean(Clicks_A)', '<=', 'mean(Clicks_B)')]
    ));

    await sendTransitionRuleToServer(new TransitionRule(
        'Clicks-fail-inconclusive',
        'Clicks v1.0.0 - v1.1.0',
        'end',
        [new Condition('result-clicks', '==', 'inconclusive'),
            new Condition('mean(Clicks_A)', '>', 'mean(Clicks_B)')]
    ));

    await sendTransitionRuleToServer(new TransitionRule(
        'Clicks-fail-reject',
        'Clicks v1.0.0 - v1.1.0',
        'end',
        [new Condition('result-clicks', '==', 'reject'),
            new Condition('mean(Clicks_A)', '>', 'mean(Clicks_B)')]
    ));


    await sendTransitionRuleToServer(new TransitionRule(
        'Purchases-succes-reject',
        'Purchases v1.0.0 - v1.1.0',
        'end',
        [new Condition('result-purchases', '==', 'reject'),
            new Condition('mean(Purchases_A)', '<=', 'mean(Purchases_B)')]
    ));

    await sendTransitionRuleToServer(new TransitionRule(
        'Purchases-succes-inconclusive',
        'Purchases v1.0.0 - v1.1.0',
        'end',
        [new Condition('result-purchases', '==', 'inconclusive'),
            new Condition('mean(Purchases_A)', '<=', 'mean(Purchases_B)')]
    ));

    await sendTransitionRuleToServer(new TransitionRule(
        'Purchases-fail-reject',
        'Purchases v1.0.0 - v1.1.0',
        'end',
        [new Condition('result-purchases', '==', 'reject'),
            new Condition('mean(Purchases_A)', '>', 'mean(Purchases_B)')]
    ));

    await sendTransitionRuleToServer(new TransitionRule(
        'Purchases-fail-inconclusive',
        'Purchases v1.0.0 - v1.1.0',
        'end',
        [new Condition('result-purchases', '==', 'inconclusive'),
            new Condition('mean(Purchases_A)', '>', 'mean(Purchases_B)')]
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



