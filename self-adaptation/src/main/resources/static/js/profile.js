import {COLORS} from './modules/constants.js'
import {EnvironmentVariable, LocustUser, UserProfile} from './modules/domain-classes.js'
import {updateStatus} from './modules/status.js'



let availableProfiles = [];

window.init = () => {
    fetch("/profile/locustProfiles", {method: "get"})
        .then(response => response.json())
        .then(data => {
            availableProfiles = data;
            resetLocustUsers();
        })
        .catch(error => console.log(error));

}



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
                resetLocustUsers();
            }
        } else {
            updateStatus('Could not add user profile.', COLORS.STATUS_LABEL_COLOR_FAIL);
        }
    })
    .catch(error => updateStatus('Could not add user profile.', COLORS.STATUS_LABEL_COLOR_FAIL));

    setTimeout(() => {updateStatus('');}, 10000);

    return false;
}


function renderLocustUserForm() {
    let locustProfiles = availableProfiles.map(value => `<option value="${value.name}" title="${value.description}">${value.name}</option>`).join('\n');

    return `
    <span class="grid-item">
        <div class="grid-container" style="grid-template-columns: 1fr 2fr; align-items: center; border: solid 1px; padding: 10px;" name="locust-user-profile">
            <span>Profile name:</span>
            <span><select name="locust-user-profile-name" onchange="addRequiredEnvironmentVariables(this)">
                <option value="" selected disabled hidden>Select a locust user profile</option>
                ${locustProfiles}
            </select></span>

            <span style="visibility: hidden; grid-column-start: 1; grid-column-end: 3; border-top: dashed 1px; margin-top: 5px; padding-top: 5px;"></span>
            <span style="visibility: hidden;" name="labelNumberOfUsers">Number of users:</span>
            <span style="visibility: hidden;" name="inputNumberOfUsers">
                <input type="number" name="profile-user-number" placeholder="number of users">
            </span>
        </div>
    </span>`;
}

function resetLocustUsers() {
    // let locustProfiles = availableProfiles.map(value => `<option value="${value.name}" title="${value.description}">${value.name}</option>`).join('\n');

    // TODO add ability to remove locust users from form
    document.getElementById('profiles').innerHTML = `
    <div class="grid-container" style="grid-template-columns: 1fr 5fr;" id="locust-users">
        <span class="grid-item">Locust users:</span>
        ${renderLocustUserForm()}
    </div>
    <button class="add-field" type="button" onclick="addLocustUser()" id="add-locust-user" style="visibility: hidden;">+</button>`;
}


window.addLocustUser = () => {
    let el = document.getElementById('locust-users');

    el.insertAdjacentHTML('beforeend', `<span></span>${renderLocustUserForm()}`);
}


window.addRequiredEnvironmentVariables = (element) => {
    let parent = element.parentElement.parentElement;

    for (const el of parent.children) {
        el.style.visibility = "visible";
    }

    // Add the required variables to the form
    const selectedProfileName = element.value;
    const requiredVariables = availableProfiles.find(x => x.name == selectedProfileName).requiredVariables.sort();
    
    for (let el of parent.querySelectorAll('[name*="EnvironmentVariable"]')) {
        el.remove();
    }
    parent.insertAdjacentHTML('beforeend', '<span style="grid-column-start: 1; grid-column-end: 3;" name="labelEnvironmentVariables">Environment variables:</span>');
    requiredVariables.forEach(x => parent.insertAdjacentHTML('beforeend', `<span name="labelEnvironmentVariable" style="margin-left: 20px; font-style: italic">${x}:</span>\n<span><input type="text" placeholder="environment variable value" name="inputEnvironmentVariable" target="${x}"></span>`));
    document.getElementById('add-locust-user').style.visibility = 'visible';
}


window.addUserProfile = () => {
    let form = document.getElementById('form-user-profile');
    const formData = new FormData(document.forms['form-user-profile']);

    let users = [];

    for (let entry of document.getElementsByName('locust-user-profile')) {

        let locustName = entry.querySelector('[name=locust-user-profile-name]').value;
        let numberOfUsers = entry.querySelector('[name=profile-user-number]').value;

        let vars = []
        for (let variable of entry.querySelectorAll('[name=inputEnvironmentVariable]')) {
            vars.push(new EnvironmentVariable(variable.getAttribute('target'), variable.value));
        }

        users.push(new LocustUser(locustName, numberOfUsers, vars));
    }

    sendUserProfileToServer(new UserProfile(formData.get('nameProfile'), users), form);
    return false;
}




window.addDefaultUserProfiles = () => {

    sendUserProfileToServer(
        new UserProfile('Standard', [
            new LocustUser('RegularUser', 80, [
                new EnvironmentVariable('clickChanceA', 0.1),
                new EnvironmentVariable('clickChanceB', 0.2),
                new EnvironmentVariable('purchaseChanceA', 0.05),
                new EnvironmentVariable('purchaseChanceB', 0.15),
            ])
        ]
    ));
}
