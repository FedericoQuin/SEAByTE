import { COLORS } from "./modules/constants.js";
import { updateStatus } from "./modules/status.js";




export async function sendPopulationSplitToServer(split, form=null) {
    setTimeout(() => {updateStatus('');}, 10000);

    return fetch("/split/newSplit", {
        method: 'post', 
        headers: {'Content-Type': 'application/json'}, 
        body: JSON.stringify(split)
    }).then(response => {
        if (response.status == 200) {
            updateStatus('Population split succesfully added.', COLORS.STATUS_LABEL_COLOR_SUCCES);
            
            if (form) {
                form.reset();
            }
        } else {
            updateStatus('Could not add population split.', COLORS.STATUS_LABEL_COLOR_FAIL);
        }
    })
    .catch(error => updateStatus('Could not add population split.', COLORS.STATUS_LABEL_COLOR_FAIL));
}
