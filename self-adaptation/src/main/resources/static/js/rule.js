
import {TransitionRule, Condition} from './modules/domain-classes.js'
import {updateStatus} from './modules/status.js'
import {COLORS} from './modules/constants.js'




function sendTransitionRuleToServer(transitionRule, form=null) {
    fetch("/rule/newRule", {
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

    setTimeout(() => {updateStatus('');}, 10000);

    return false;
}


window.addTransitionRule = () => {
    let form = document.getElementById('form-transition-rule');
    const formData = new FormData(document.forms['form-transition-rule']);

    sendTransitionRuleToServer(TransitionRule.constructFromForm(formData), form);
    
    return false;
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
            new Condition('mean(ResponseTime_A)', '>=', 'mean(ResponseTime_B)')]
    ));


    sendTransitionRuleToServer(new TransitionRule(
        'Upgrade-fail',
        'Upgrade v1.0.0 - v1.1.0',
        'end',
        [new Condition('result-welsh-t-test', '==', 'reject'), 
            new Condition('mean(ResponseTime_A)', '<', 'mean(ResponseTime_B)')]
    ));


    sendTransitionRuleToServer(new TransitionRule(
        'Clicks-succes-reject',
        'Clicks v1.0.0 - v1.1.0',
        'Purchases v1.0.0 - v1.1.0',
        [new Condition('result-clicks', '==', 'reject'),
            new Condition('mean(Clicks_A)', '<=', 'mean(Clicks_B)')]
    ));

    sendTransitionRuleToServer(new TransitionRule(
        'Clicks-succes-inconclusive',
        'Clicks v1.0.0 - v1.1.0',
        'Purchases v1.0.0 - v1.1.0',
        [new Condition('result-clicks', '==', 'inconclusive'),
            new Condition('mean(Clicks_A)', '<=', 'mean(Clicks_B)')]
    ));

    sendTransitionRuleToServer(new TransitionRule(
        'Clicks-fail-inconclusive',
        'Clicks v1.0.0 - v1.1.0',
        'end',
        [new Condition('result-clicks', '==', 'inconclusive'),
            new Condition('mean(Clicks_A)', '>', 'mean(Clicks_B)')]
    ));

    sendTransitionRuleToServer(new TransitionRule(
        'Clicks-fail-reject',
        'Clicks v1.0.0 - v1.1.0',
        'end',
        [new Condition('result-clicks', '==', 'reject'),
            new Condition('mean(Clicks_A)', '>', 'mean(Clicks_B)')]
    ));


    sendTransitionRuleToServer(new TransitionRule(
        'Purchases-succes-reject',
        'Purchases v1.0.0 - v1.1.0',
        'end',
        [new Condition('result-purchases', '==', 'reject'),
            new Condition('mean(Purchases_A)', '<=', 'mean(Purchases_B)')]
    ));

    sendTransitionRuleToServer(new TransitionRule(
        'Purchases-succes-inconclusive',
        'Purchases v1.0.0 - v1.1.0',
        'end',
        [new Condition('result-purchases', '==', 'inconclusive'),
            new Condition('mean(Purchases_A)', '<=', 'mean(Purchases_B)')]
    ));

    sendTransitionRuleToServer(new TransitionRule(
        'Purchases-fail-reject',
        'Purchases v1.0.0 - v1.1.0',
        'end',
        [new Condition('result-purchases', '==', 'reject'),
            new Condition('mean(Purchases_A)', '>', 'mean(Purchases_B)')]
    ));

    sendTransitionRuleToServer(new TransitionRule(
        'Purchases-fail-inconclusive',
        'Purchases v1.0.0 - v1.1.0',
        'end',
        [new Condition('result-purchases', '==', 'inconclusive'),
            new Condition('mean(Purchases_A)', '>', 'mean(Purchases_B)')]
    ));
}
