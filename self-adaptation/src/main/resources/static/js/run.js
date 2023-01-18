
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
               'Target value 1:', `${split['targetValue1']}`,
               'Target value 2:', `${split['targetValue2']}`,
               'Next component:', `${split['nextComponent']}`,
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


import { addDefaultPipelineSeams2022 } from './modules/seams2022_pipeline.js'

window.experimentation = async () => {

    await addDefaultPipelineSeams2022();

    location.reload();
    // startPipeline('Default_scenario');
}



