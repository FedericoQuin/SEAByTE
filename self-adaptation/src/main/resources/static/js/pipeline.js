import {COLORS} from './modules/constants.js'
import {Pipeline} from './modules/domain-classes.js'
import {updateStatus} from './modules/status.js'



window.init = () => {
    // TODO fetch all experiments, transition rules, pipelines, and population splits

    // FIXME placeholders for now
    fetch("/experiment/retrieve", {method: "get"})
        .then(response => response.json())
        .then(data => {
            data.forEach(experiment => addExperimentOption(experiment));
        })
        .catch(error => console.log(error));


    fetch("/rule/retrieve", {method: "get"})
        .then(response => response.json())
        .then(data => {
            data.forEach(rule => addTransitionRuleOption(rule));
        })
        .catch(error => console.log(error));

    fetch("/pipeline/retrieve", {method: "get"})
        .then(response => response.json())
        .then(data => {
            data.forEach(pipeline => addPipelineOption(pipeline));
        })
        .catch(error => console.log(error));

    fetch("/split/retrieve", {method: "get"})
        .then(response => response.json())
        .then(data => {
            data.forEach(split => addPopulationSplitOption(split));
        })
        .catch(error => console.log(error));
}



export async function sendPipelineToServer(pipeline, form=null) {
    setTimeout(() => {updateStatus('');}, 10000);

    return fetch("/pipeline/newPipeline", {
        method: 'post', 
        headers: {'Content-Type': 'application/json'}, 
        body: JSON.stringify(pipeline)
    }).then(response => {
        if (response.status == 200) {
            updateStatus('Pipeline succesfully added.', COLORS.STATUS_LABEL_COLOR_SUCCES);
            
            if (form) {
                form.reset();
            }
        } else {
            updateStatus('Could not add pipeline.', COLORS.STATUS_LABEL_COLOR_FAIL);
        }
    })
    .catch(error => updateStatus('Could not add pipeline.', COLORS.STATUS_LABEL_COLOR_FAIL));
}
