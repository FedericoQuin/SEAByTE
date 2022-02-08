
import {Experiment, ABAssignment, Condition, StatisticalTest} from './modules/domain-classes.js'




function addExperimentToServer(experiment) {
    fetch("/experiment/newExperiment", {
        method: 'post', 
        headers: {'Content-type': 'application/json'}, 
        body: JSON.stringify(experiment)
    });
}



window.publishExperiment = () => {
    let form = document.getElementById('form-experiment');
    const formData = new FormData(document.forms['form-experiment']);

    // Quick check to make sure a user profile is specified
    if (!formData.get("user-profile")) {
        window.alert('Make sure a proper user profile is selected.')
        return;
    }

    addExperimentToServer(Experiment.constructFromForm(formData));

    form.reset();
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



window.publishDefaultExperiments = () => {
    addExperimentToServer(new Experiment(
        'Upgrade v1.0.0 - v1.1.0',
        'ws-recommendation-service-1-0-0',
        'ws-recommendation-service-1-1-0',
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


    addExperimentToServer(new Experiment(
        'Clicks v1.0.0 - v1.1.0',
        'ws-recommendation-service-1-0-0',
        'ws-recommendation-service-1-1-0',
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


    addExperimentToServer(new Experiment(
        'Purchases v1.0.0 - v1.1.0',
        'ws-recommendation-service-1-0-0',
        'ws-recommendation-service-1-1-0',
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
}
