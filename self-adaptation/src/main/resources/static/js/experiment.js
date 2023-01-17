
import {Experiment, ABAssignment, Condition, StatisticalTest} from './modules/domain-classes.js'
import {updateStatus} from './modules/status.js'
import { COLORS } from './modules/constants.js';




export async function sendExperimentToServer(experiment, form=null) {
    setTimeout(() => {updateStatus('');}, 10000);

    return fetch("/experiment/newExperiment", {
        method: 'post', 
        headers: {'Content-type': 'application/json'}, 
        body: JSON.stringify(experiment)
    })
    .then(response => {
        if (response.status == 200) {
            updateStatus('Experiment succesfully added.', COLORS.STATUS_LABEL_COLOR_SUCCES);
            
            if (form) {
                form.reset();
            }
        } else {
            updateStatus('Could not add experiment.', COLORS.STATUS_LABEL_COLOR_FAIL);
        }
    })
    .catch(error => updateStatus('Could not add experiment.', COLORS.STATUS_LABEL_COLOR_FAIL));
}



window.publishExperiment = () => {
    let form = document.getElementById('form-experiment');
    const formData = new FormData(document.forms['form-experiment']);

    // Quick check to make sure a user profile is specified
    if (!formData.get("user-profile")) {
        window.alert('Make sure a proper user profile is selected.')
        return;
    }
    
    // Similar check for the setup
    if (!formData.get("setup")) {
        window.alert('Make sure a proper setup is selected.');
        return;
    }

    sendExperimentToServer(Experiment.constructFromForm(formData), form);
    return false;
}

window.getProfiles = () => {
    fetch('/profile/retrieve')
        .then(response => response.json())
        .then(data => {
            let elem = document.getElementById('user-profile');
            data.forEach(p => elem.insertAdjacentHTML('beforeend', `<option value="${p.name}">${p.name}</option>`));
        })
        .catch((error) => console.log(error));
}



window.getSetups = () => {
    fetch('/setup/retrieve')
        .then(response => response.json())
        .then(data => {
            let elem = document.getElementById('setup');
            data.forEach(p => elem.insertAdjacentHTML('beforeend', `<option value="${p.name}">${p.name}</option>`));
        })
        .catch((error) => console.log(error));
}





window.publishDefaultExperiments = () => {
    sendExperimentToServer(new Experiment(
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


    sendExperimentToServer(new Experiment(
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


    sendExperimentToServer(new Experiment(
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
}
