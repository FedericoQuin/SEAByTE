
import {TransitionRule, Condition} from './modules/domain-classes.js'
import {updateStatus} from './modules/status.js'
import {COLORS} from './modules/constants.js'




export async function sendTransitionRuleToServer(transitionRule, form=null) {
    setTimeout(() => {updateStatus('');}, 10000);

    return fetch("/rule/newRule", {
        method: 'post', 
        headers: {'Content-Type': 'application/json'}, 
        body: JSON.stringify(transitionRule)
    }).then(response => {
        if (response.status == 200) {
            updateStatus('Transition rule succesfully added.', COLORS.STATUS_LABEL_COLOR_SUCCES);
            
            if (form) {
                form.reset();
            }
        } else {
            updateStatus('Could not add transition rule.', COLORS.STATUS_LABEL_COLOR_FAIL);
        }
    })
    .catch(error => updateStatus('Could not add transition rule.', COLORS.STATUS_LABEL_COLOR_FAIL));
}


window.addTransitionRule = () => {
    const name = document.getElementById('nameRule').value;
    const from = document.getElementById('from-experiment').value;
    const to = document.getElementById('to-component').value;


    if (!name) {
        alert('Make sure to provide a name for the rule.');
        return;
    }

    if (!from) {
        alert('Make sure to provide a valid from experiment.');
        return;
    }

    if (!to) {
        alert('Make sure to provide a valid to component.');
        return;
    }


    let conditions = [];

    for (let condition of document.getElementsByName('condition')) {
        const type = condition.querySelector('[name=rule-condition-type]').value;

        if (type) {
            const left = condition.querySelector('[name=conditionLeftOperand]').value;
            const operator = condition.querySelector('[name=conditionOperator]').value;
            const right = condition.querySelector('[name=conditionRightOperand]').value;

            if (!left || !operator || !right) {
                alert('Make sure all the provided conditions are complete.');
                return;
            }

            conditions.push(new Condition(left, operator, right));
        }
        // ignore rule otherwise (empty)
    }

    sendTransitionRuleToServer(new TransitionRule(name, from, to, conditions));



    let form = document.getElementById('form-transition-rule');
    form.reset();
    document.getElementById('conditions').innerHTML = '';

    return false;
}




// For now store this globally in memory here?
class ExperimentVariables {
    constructor(statisticalVariable, otherVariables) {
        this.statisticalVariable = statisticalVariable;
        this.otherVariables = otherVariables;
    }
}

let variables = undefined;


window.getExperimentVariables = (experimentName) => {
    fetch(`/experiment/retrieveVariables?name=${experimentName}`)
        .then(response => response.json())
        .then(data => {
            variables = new ExperimentVariables(data['statistical'], data['other']);
            resetConditions();

            // TODO add option to selection of transition rules with the appropriate type and available variables
        })
        .catch((error) => console.log(error));
}

function resetConditions() {
    document.getElementById('conditions').innerHTML = `
    <div class="grid-container" style="grid-template-columns: 4fr 4fr 2fr 4fr 1fr;" name="condition">
        <div class="grid-item">
            <select name="rule-condition-type" onchange="addAppropriateRuleVariablesForm(this)">
                <option value="" selected disabled hidden>Select a rule condition type</option>
                <option value="stat">Statistical</option>
                <option value="reg">Regular</option>
            </select>
        </div>
    </div>
    <button class="add-field" type="button" onclick="addConditionField()" id="add-condition-field" style="visibility: hidden;">+</button>`;
}


window.loadExperiments = () => {
    fetch('/experiment/retrieve')
        .then(response => response.json())
        .then(data => {
            const experimentNames = data.map(x => x['name']);
            let fromElement = document.getElementById('from-experiment');
            let toElement = document.getElementById('to-component');
            for (let name of experimentNames) {
                fromElement.insertAdjacentHTML('beforeend', `<option>${name}</option>`)
                toElement.insertAdjacentHTML('beforeend', `<option>${name}</option>`)
            }

            fetch('/split/retrieve')
                .then(response => response.json())
                .then(dataSplit => {
                    for (let name of dataSplit.map(x => x['name'])) {
                        toElement.insertAdjacentHTML('beforeend', `<option>${name}</option>`)
                    }
            toElement.insertAdjacentHTML('beforeend', '<option>end</option>');
                });
        })
        .catch(err => console.log(err));
}





window.addAppropriateRuleVariablesForm = (element) => {
    if (variables == null) {
        console.log("No experiment selected yet (or no variables found).");
        return;
    }

    // console.log(variables);

    // console.log(element);
    // console.log(element.parentElement.parentElement);
    const type = element.options[element.selectedIndex].value;
    // console.log(type);


    let varElement = element.parentElement.parentElement.querySelector('[name="conditionLeftOperand"]');
    let nbOfRules = document.getElementsByName('condition').length;

    if (!varElement) {
        element.parentElement.insertAdjacentHTML('afterend', `
        <div class="grid-item">
            <select name="conditionLeftOperand">
            <option selected disabled hidden>Select variable...</option>
            </select>
        </div>
        <div class="grid-item">
            <select name="conditionOperator">
                <option selected disabled hidden>Select op...</option>
            </select>
        </div>
        <div class="grid-item">
            <select name="conditionRightOperand">
                <option selected disabled hidden>Select value...</option>
            </select>
        </div>
        ${nbOfRules > 1 ? '<button class="add-field" type="button" onclick="removeConditionField(this)" id="remove-condition-field">-</button>' : ''}
        `);
        
        varElement = element.parentElement.parentElement.querySelector('[name="conditionLeftOperand"]');
    }

    let opElement = element.parentElement.parentElement.querySelector('[name="conditionOperator"]');
    let rightElement = element.parentElement.parentElement.querySelector('[name="conditionRightOperand"]');

    if (type == "stat") {
        varElement.innerHTML = `\n<option value="" selected hidden disabled>Select variable...</option>
        <option value="${variables.statisticalVariable}">${variables.statisticalVariable}</option>\n`;

        opElement.innerHTML = `\n<option value="" selected hidden disabled>Select op...</option>
        <option value="==">==</option>
        <option value="!=">!=</option>\n`;

        rightElement.innerHTML = `\n<option value="" selected hidden disabled>Select value...</option>
        <option value="reject">Reject</option>
        <option value="inconclusive">Inconclusive</option>\n`;
        // document.replaceChild

    } else if (type == "reg") {
        varElement.innerHTML = `\n<option value="" selected hidden disabled>Select variable...</option>\n`;
        rightElement.innerHTML = '\n<option value="" selected hidden disabled>Select value...</option>\n';

        for (let variable of variables.otherVariables) {
            varElement.insertAdjacentHTML('beforeend', `<option value="${variable}">${variable}</option>`);
            rightElement.insertAdjacentHTML('beforeend', `<option value="${variable}">${variable}</option>`);
        }

        opElement.innerHTML = `\n<option value="" selected hidden disabled>Select op...</option>
        <option value="==">==</option>
        <option value="!=">!=</option>
        <option value="<">&#60</option>
        <option value="<=">&#60=</option>
        <option value=">">&#62</option>
        <option value=">=">&#62=</option>\n`;
    }

    document.getElementById('add-condition-field').style.visibility = 'visible';
}


window.addConditionField = () => {
    document.getElementById('add-condition-field').insertAdjacentHTML('beforebegin', `
        <div></div>
          <div class="grid-container" style="grid-template-columns: 4fr 4fr 2fr 4fr 1fr;" name="condition">
            <div class="grid-item">
              <select name="rule-condition-type" onchange="addAppropriateRuleVariablesForm(this)">
                <option value="" selected disabled hidden>Select a rule condition type</option>
                <option value="stat">Statistical</option>
                <option value="reg">Regular</option>
              </select>
            </div>
        </div>
    `);
}

window.removeConditionField = (button) => {
    let parent = button.parentElement;
    let prevSibling = parent.previousElementSibling;

    parent.parentElement.removeChild(prevSibling);
    parent.parentElement.removeChild(parent);
}



    
window.addDefaultTransitionRules = () => {
    sendTransitionRuleToServer(new TransitionRule(
        'Upgrade-succes-inconclusive',
        'Upgrade v1.0.0 - v1.1.0',
        'Clicks v1.0.0 - v1.1.0',
        [new Condition('result-welsh-t-test', '!=', 'reject')]
    ));


    sendTransitionRuleToServer(new TransitionRule(
        'Upgrade-succes-reject',
        'Upgrade v1.0.0 - v1.1.0',
        'Clicks v1.0.0 - v1.1.0',
        [new Condition('result-welsh-t-test', '==', 'reject'), 
            new Condition('mean(ResponseTimeRecommendation_A)', '>=', 'mean(ResponseTimeRecommendation_B)')]
    ));


    sendTransitionRuleToServer(new TransitionRule(
        'Upgrade-fail',
        'Upgrade v1.0.0 - v1.1.0',
        'end',
        [new Condition('result-welsh-t-test', '==', 'reject'), 
            new Condition('mean(ResponseTimeRecommendation_A)', '<', 'mean(ResponseTimeRecommendation_B)')]
    ));


    sendTransitionRuleToServer(new TransitionRule(
        'Clicks-succes-reject',
        'Clicks v1.0.0 - v1.1.0',
        'Purchases v1.0.0 - v1.1.0',
        [new Condition('result-clicks', '==', 'reject'),
            new Condition('mean(ClicksRecommendation_A)', '<=', 'mean(ClicksRecommendation_B)')]
    ));

    sendTransitionRuleToServer(new TransitionRule(
        'Clicks-succes-inconclusive',
        'Clicks v1.0.0 - v1.1.0',
        'Purchases v1.0.0 - v1.1.0',
        [new Condition('result-clicks', '==', 'inconclusive'),
            new Condition('mean(ClicksRecommendation_A)', '<=', 'mean(ClicksRecommendation_B)')]
    ));

    sendTransitionRuleToServer(new TransitionRule(
        'Clicks-fail-inconclusive',
        'Clicks v1.0.0 - v1.1.0',
        'end',
        [new Condition('result-clicks', '==', 'inconclusive'),
            new Condition('mean(ClicksRecommendation_A)', '>', 'mean(ClicksRecommendation_B)')]
    ));

    sendTransitionRuleToServer(new TransitionRule(
        'Clicks-fail-reject',
        'Clicks v1.0.0 - v1.1.0',
        'end',
        [new Condition('result-clicks', '==', 'reject'),
            new Condition('mean(ClicksRecommendation_A)', '>', 'mean(ClicksRecommendation_B)')]
    ));


    sendTransitionRuleToServer(new TransitionRule(
        'Purchases-succes-reject',
        'Purchases v1.0.0 - v1.1.0',
        'end',
        [new Condition('result-purchases', '==', 'reject'),
            new Condition('mean(PurchasesRecommendation_A)', '<=', 'mean(PurchasesRecommendation_B)')]
    ));

    sendTransitionRuleToServer(new TransitionRule(
        'Purchases-succes-inconclusive',
        'Purchases v1.0.0 - v1.1.0',
        'end',
        [new Condition('result-purchases', '==', 'inconclusive'),
            new Condition('mean(PurchasesRecommendation_A)', '<=', 'mean(PurchasesRecommendation_B)')]
    ));

    sendTransitionRuleToServer(new TransitionRule(
        'Purchases-fail-reject',
        'Purchases v1.0.0 - v1.1.0',
        'end',
        [new Condition('result-purchases', '==', 'reject'),
            new Condition('mean(PurchasesRecommendation_A)', '>', 'mean(PurchasesRecommendation_B)')]
    ));

    sendTransitionRuleToServer(new TransitionRule(
        'Purchases-fail-inconclusive',
        'Purchases v1.0.0 - v1.1.0',
        'end',
        [new Condition('result-purchases', '==', 'inconclusive'),
            new Condition('mean(PurchasesRecommendation_A)', '>', 'mean(PurchasesRecommendation_B)')]
    ));
}
