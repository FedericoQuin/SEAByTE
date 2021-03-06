import {COLORS} from './modules/constants.js'
import {DockerService, Setup} from './modules/domain-classes.js'



    
function updateStatus(message, color='#000000') {
    let label = document.getElementById('status-label');
    label.innerHTML = message;
    label.style.color = color;
}



function sendSetupToServer(setup, form=null) {
    fetch("/setup/newSetup", {
        method: 'post', 
        headers: {'Content-Type': 'application/json'}, 
        body: JSON.stringify(setup)
    }).then(response => {
        if (response.status == 200) {
            updateStatus('AB setup succesfully added.', COLORS.STATUS_LABEL_COLOR_SUCCES);
            if (form) {
                form.reset();
            }
        } else {
            updateStatus('Could not add AB setup.', COLORS.STATUS_LABEL_COLOR_FAIL);
        }
    })
    .catch(error => updateStatus('Could not add AB setup.', COLORS.STATUS_LABEL_COLOR_FAIL));

    setTimeout(() => {updateStatus('');}, 10000);

    return false;
}


window.sendCustomSetup = () => {
    let form = document.getElementById('form-setup');
    const formData = new FormData(document.forms['form-setup']);
    sendSetupToServer(Setup.constructFromForm(formData), form);
}


    
window.addDefaultSetup = () => {
    sendSetupToServer(new Setup(
        'Recommendation_upgrade',
        new DockerService('ws-recommendation-service-1-0-0', 'ws-recommendation-service-image:1.0.0'),
        new DockerService('ws-recommendation-service-1-1-0', 'ws-recommendation-service-image:1.1.0'),
        new DockerService('ws-recommendation-service', 'ab-component-image:latest'),
        'ws-recommendation-service'
    ));
}
