
// const constants = require('./modules/constants.js')
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







window.runDefaultSetup = () => {
    (async () => {
        updateStatusWithDefaultLoader('Running the default AB setup...', COLORS.STATUS_LABEL_COLOR_PENDING);
        let f = await fetch('run/runDefaultSetup', {method: 'post'});
        let response = f.status;

        if (response == 200) {
            updateStatus('AB setup succesfully finished.', COLORS.STATUS_LABEL_COLOR_SUCCES);
        } else {
            updateStatus('AB setup failed to run.', COLORS.STATUS_LABEL_COLOR_FAIL);
        }
    })();
}



window.runSetup = (setupName) => {
    (async () => {
        updateStatusWithDefaultLoader(`Running AB setup named ${setupName}...`, COLORS.STATUS_LABEL_COLOR_PENDING);
        let f = await fetch('run/runSetup?' + new URLSearchParams({name: setupName}), 
            {method: 'post'});
        let response = f.status;

        if (response == 200) {
            updateStatus('AB setup succesfully finished.', COLORS.STATUS_LABEL_COLOR_SUCCES);
        } else {
            updateStatus('AB setup failed to run.', COLORS.STATUS_LABEL_COLOR_FAIL);
        }
    })();
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
                addSetupForm(setup);
            }
        }
        
    })();
}

// function addSetupCollapsible(rootElement, setup) {
//     rootElement.innerHTML += `\n<div class="grid-item-margin">
//         <details>
//             <summary>${setup['name']}</summary>
//             <p>Information about the setup in question</p>
//         </details></div>`;
// }


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

function addSetupForm(setup) {
    var formElem = document.getElementById('setupName');

    formElem.insertAdjacentHTML('beforeend', 
        `<option value="${setup['name']}">${setup['name']}</option>`);
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
                addExperimentForm(experimentName);
                addInitialExperimentForm(experimentName);
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
            'Variant A:', `${experiment['variantA']}`, 
            'Variant B:', `${experiment['variantB']}`,
            'AB weights:', `${experiment['absetting']['weightA']} (A) - ${experiment['absetting']['weightB']} (B)`,
            'User profile:', `${experiment['userProfile']['name']} (${(experiment['userProfile']['locustUsers']).map(x => x['numberOfUsers']).reduce((v1, v2) => v1 + v2, 0)} users in total)`,
            'Metrics:', `${experiment['metrics'].join(', ')}`,
            'Statistical test:', `${experiment['statisticalTest']['type']}`,
            'P value: ', `${experiment['statisticalTest']['pvalue']}`,
            'Samples: ', `${experiment['statisticalTest']['samples']}`]
                .map(x => `<div>${x}</div>`).join('\n')}</div>
        </details></div>`;
}


function addExperimentForm(experimentName) {
    let expElem = document.getElementById('selectExperiments');
    // <input type="checkbox" id="${experimentName}" name="experiments" value="${experimentName}"></input>

    expElem.insertAdjacentHTML('beforeend', `<div>
        <label for="${experimentName}" class="container">${experimentName}
            <input type="checkbox" id="${experimentName}" name="experiments" value="${experimentName}"></input>
            <span class="checkmark"></span>
        </label>
    </div>`);
}

function addInitialExperimentForm(experimentName) {
    let initExpElem = document.getElementById('initialExperiment');
    
    initExpElem.insertAdjacentHTML('beforeend', 
        `<option value="${experimentName}">${experimentName}</option>`);
}



window.populateRules = () => {
    (async () => {
        let rulesList = document.getElementById('list-rules');

        let response = await fetch('/rule/retrieve');

        if (response.status == 200) {
            let data = await response.json();

            // TODO order alphabetically
            for (let rule of data) {
                const ruleName = rule['name'];
                addRuleCollapsible(rulesList, rule);
                addRuleForm(ruleName);
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
               'To experiment:', `${rule['toExperiment']}`,
               'Conditions:', `${rule['conditions'].join('<br>')}`]
                   .map(x => `<div>${x}</div>`).join('\n')}</div>
        </details></div>`;
}


function addRuleForm(ruleName) {
    let expElem = document.getElementById('selectRules');

    expElem.insertAdjacentHTML('beforeend', `<div>
        <label for="${ruleName}" class="container">${ruleName}
            <input type="checkbox" id="${ruleName}" name="rules" value="${ruleName}"></input>
            <span class="checkmark"></span>
        </label>
    </div>`);
}




window.startFeedbackLoop = () => {
    const setupName = document.getElementById('setupName').value;
    const selectedExperiments = Array.from(document.getElementsByName('experiments'))
        .filter(c => c.checked)
        .map(c => c.value);
    const rules = Array.from(document.getElementsByName('rules'))
        .filter(c => c.checked)
        .map(c => c.value);
    const initialExperimentName = document.getElementById('initialExperiment').value;

    if (!setupName) {
        updateStatus('Make sure a setup name is specified.');
        return;
    }
    (async () => {
        updateStatusWithDefaultLoader(`Starting feedback loop with setup '${setupName}'...`, COLORS.STATUS_LABEL_COLOR_PENDING);
        const f = await fetch('adaptation/start', 
            {method: 'post', body: JSON.stringify({
                setup: setupName,
                experiments: selectedExperiments,
                transitionRules: rules,
                initialExperiment: initialExperimentName
            })});
        const response = f.status;

        if (response == 200) {
            updateStatus('Feedback loop succesfully started.', COLORS.STATUS_LABEL_COLOR_SUCCES);
        } else {
            updateStatus('Failed to start feedback loop.', COLORS.STATUS_LABEL_COLOR_FAIL);
        }
    })();
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


// export {populateSetups, populateExperiments};

