import {COLORS} from './modules/constants.js'
import {UserProfile} from './modules/domain-classes.js'
import {updateStatus} from './modules/status.js'







function sendUserProfileToServer(userProfile, form=null) {
    fetch("/profile/newProfile", {
        method: 'post', 
        headers: {'Content-Type': 'application/json'}, 
        body: JSON.stringify(userProfile)
    }).then(response => {
        if (response.status == 200) {
            updateStatus('User profile succesfully added.', COLORS.STATUS_LABEL_COLOR_SUCCES);
            
            if (form) {
                form.reset();
            }
        } else {
            updateStatus('Could not add user profile.', COLORS.STATUS_LABEL_COLOR_FAIL);
        }
    })
    .catch(error => updateStatus('Could not add user profile.', COLORS.STATUS_LABEL_COLOR_FAIL));

    setTimeout(() => {updateStatus('');}, 10000);

    return false;
}


window.addUserProfile = () => {
    let form = document.getElementById('form-user-profile');
    const formData = new FormData(document.forms['form-user-profile']);

    let variables = {};

    for (let entry of document.getElementsByName('profile-variable')) {
        let key = entry.querySelector('[name=profile-variable-name]').value;
        let value = entry.querySelector('[name=profile-variable-value]').value;

        if (key && value) {
            variables[key] = value; 
        }
    }

    let users = {};
    for (let entry of document.getElementsByName('profile-user')) {
        let key = entry.querySelector('[name=profile-user-name]').value;
        let value = entry.querySelector('[name=profile-user-number]').value;

        if (key && value) {
            users[key] = value;
        }
    }

    sendUserProfileToServer(new UserProfile(formData.get('nameProfile'), users, variables), form);
    return false;
}


    
window.addDefaultUserProfiles = () => {

    sendUserProfileToServer(new UserProfile(
        'Standard',
        {
            RegularUser: 80,
        },   
        {
            clickChanceA: 0.1,
            clickChanceB: 0.2,
            purchaseChanceA: 0.05,
            purchaseChanceB: 0.15
        }
    ));
}
