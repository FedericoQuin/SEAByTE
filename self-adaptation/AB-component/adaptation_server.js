const http = require('http');
const url = require('url');
var now = require("performance-now");


const PORT = 80;
const AB_COMPONENT_NAME = process.env.AB_COMPONENT_NAME;
const ML_CONTAINER_ADRESS = process.env.ML_CONTAINER_ADRESS;



class Variant {
    static A = new Variant('A');
    static B = new Variant('B');

    constructor(name) {
        this.name = name;
    }
}


class ClientID {
    constructor(id, group) {
        this.id = id;
        // The group signifies either A or B in the case of AB testing
        this.group = group;
    }
}



class ABState {
    constructor() {
        this.clients = [];
        this.assignmentFunction = ABState.MLbasedAssignmentFunction(ML_CONTAINER_ADRESS,this.variant_received);
        this.waitingForVariant = false;
        this.selectedVariant = "A";
    }

    static defaultAssignmentFunction(a, b) {
        return {
            weightA: a,
            weightB: b,
            progress: 0,

            determineAssignment(_) {
                let variant = (this.progress % 100) < this.weightA ? Variant.A : Variant.B;
                this.progress += Math.min(this.weightA, this.weightB);
                return variant;
            }
        }
    }

    static MLbasedAssignmentFunction(ml_container_adress, callback_function){
        return {

            determineAssignment(id){
                return new Promise(function(resolve, reject) {
                    let data = '';
                    let h = [];
                    h['cookie'] = `client-id=${id}`

                    const ml_container_request = http.request(ml_container_adress+"/variant", {headers: {'Cookie': `client-id=${id}`}}, (r) => {

                
                        r.on('data', (chunk) => {data += chunk;
                        });
                        r.on('end', () => {
                        console.log("callback_end");
                        resolve(data.toString());
                        });
                    });
    
                    ml_container_request.on('error', (error) => {
                        console.log("callback_error");
                        console.error(error);
                        res.end(error.toString());
                        reject("A");
                    
                        });

                    ml_container_request.end();
                
                    });
            }

                
        }
    }

    addClient(id) {

        let state =  this;
        return new Promise(function(resolve, reject) {
        let variant = state.assignmentFunction.determineAssignment(id);
        variant.then((value) => {state.clients.push(new ClientID(id,  value));
            console.log("add done");
            resolve();}).catch(error => {console.info(error.message)
            reject(error)});
        })
        
    }

    hasClient(clientId) {
        return this.clients.filter(client => client.id == clientId).length > 0;
    }


    getGroup(clientId) {
        return this.clients.filter(client => client.id == clientId)[0].group;
    }

    setWeightA(weight) {
        this.weightA = weight;
    }

    setWeightB(weight) {
        this.weightB = weight;
    }

    clearClients() {
        this.clients.length = 0;
    }


    adjustWeights(a, b) {
        
        if (this.assignmentFunction.weightA < a) {
            // Increased traffic to variant A ---> adjust clients of group B
            const difference_weight = a - this.weightA;
            const amtToConvert = Math.ceil(this.clients.length * (difference_weight / 100.0));

            this.clients
                .filter(c => c.group == Variant.B)
                .map(c => ({c, random: Math.random() }))
                .sort((a,b) => a.random - b.random)
                .map(({c}) => c)
                .slice(amtToConvert-1)
                .forEach(c => c.group = Variant.A);
        } else {
            // Other way around: increased traffic to variant B ---> switch clients from A to B
            const difference_weight = b - this.weightB;
            const amtToConvert = Math.ceil(this.clients.length * (difference_weight / 100.0));

            this.clients
                .filter(c => c.group == Variant.A)
                .map(c => ({c, random: Math.random() }))
                .sort((a,b) => a.random - b.random)
                .map(({c}) => c)
                .slice(amtToConvert-1)
                .forEach(c => c.group = Variant.B);
        }

        // Adjust the final weights
        this.assignmentFunction = ABState.defaultAssignmentFunction(a, b);
    }

    adjustWeightsWithCustomAssignment(a, b, userLimit) {
        this.assignmentFunction = {
            weightA: a,
            weightB: b,
            userLimit: userLimit,

            determineAssignment(id) {
                return (id <= Math.floor(this.userLimit * this.weightA / 100.0)) ?
                    Variant.A : Variant.B;
            }
        }

        this.clients.forEach(c => c.group = this.assignmentFunction.determineAssignment(c.id));
        // A and B assigment happens based on the receiver limit of users that are going to connect to the system
    }

}


class TimingRequest {
    constructor(startTime, duration, clientId, url) {
        this.start = startTime;
        this.duration = duration;
        this.clientId = clientId;
        this.requestedUrl = url;
    }
}


// in memory for now
let state = new ABState();
let historyA = [];
let historyB = [];
let last_client_id = 0;


function parseCookies(raw) {
    if (!raw) {
        return [];
    }
    
    let result = [];
    for (cookie of raw.split('; ')) {
        result.push(cookie.split(/=(.+)/));
    }

    return result;
}


function handleAdaptationFunctions(req, res, requested_url) {

    if (requested_url.pathname.includes('/adaptation/history')) {
        const params = new URLSearchParams(requested_url.query);

        if (!params.has('variant')) {
            res.end('No variant specified.');
            res.statusCode = 400;
        }
        
        const variant = params.get('variant');
        let history = 
            variant == Variant.A.name ? historyA : 
            variant == Variant.B.name ? historyB : undefined;

        res.end(JSON.stringify(history));
        if (params.has('removeAfter')) {
            history.length = 0;
        }
        // samples = history.filter(x => x.variant == );
        
        // res.end(history.map(x => `[id ${x.clientId} on requested url '${x.requestedUrl}']: ${(x.duration).toFixed(4)} ms`).join('\n'));
        // res.end(`name = ${AB_COMPONENT_NAME}`);
    } else if (requested_url.pathname.includes('/adaptation/change')) {
        const params = new URLSearchParams(requested_url.query);
        const weight_a = parseInt(params.get(Variant.A.name));
        const weight_b = parseInt(params.get(Variant.B.name));

        if (weight_a + weight_b != 100) {
            res.end(`Invalid weights for A and B: does not add up to a total sum of 100 (sum=${weight_a+weight_b}).`);
            response.statusCode = 400;
            return;
        }
        // Adjust the weights
        if (params.has('userLimit')) {
            const userLimit = parseInt(params.get('userLimit'));
            state.adjustWeightsWithCustomAssignment(weight_a, weight_b, userLimit);
            res.end(`Adjusted A/B weights of this AB component to ${weight_a} and ${weight_b} with custom assignment function.`);
        } else {
            state.adjustWeights(weight_a, weight_b);
            res.end(`Adjusted A/B weights of this AB component to ${weight_a} and ${weight_b}.`);
        }

    } else if (requested_url.pathname.includes('/adaptation/reset')) {
        historyA.length = 0;
        historyB.length = 0;
        last_client_id = 0;
        state.clearClients();

        res.end('Done.')
    } else {
        res.end('Unsupported operation.');
        res.statusCode = 404;
    }

}




const requestListener = function (req, res) {
    // Make distinction between adaptation requests and regular requests
    const requested_url = url.parse(req.url);
    if (requested_url.pathname.includes('/adaptation')) { 
        handleAdaptationFunctions(req, res, requested_url);
        return;
    }

    const current_cookies = parseCookies(req.headers['cookie']);
    let h = req.headers;
    let client_id;

    const client_id_cookie = current_cookies.filter((cookie) => cookie[0] == 'client-id');
    const has_client_id = client_id_cookie.length > 0;

    if (!has_client_id) {
        do {
            last_client_id++;
        } while (state.hasClient(last_client_id));

        client_id = last_client_id;

        // Append the newly created client ID to the cookie header such that the client stores and uses it in subsequent requests
        const old_cookies = h['cookie']
        h['cookie'] = (old_cookies == undefined ? '' : old_cookies + '; ') + `client-id=${client_id}`;
    } else {
        client_id = client_id_cookie[0][1];
    }

    const client_group =""

    if (!state.hasClient(client_id)) {
        // Add the client-id to one of the scenarios
        let promise = state.addClient(client_id);
        promise.then(()=>{pass_internal_request(req, client_id);}).catch(error => console.info(error.message));
    }
    else{
        pass_internal_request(req, client_id);
    }

    



    
}

function pass_internal_request(req, client_id){
    const client_group = state.getGroup(client_id);
        h['cookie'] += `; scenario${client_group.name}_${AB_COMPONENT_NAME}=true`;
    // `http://localhost:${PORT}${req.url}`,

    
        if (!has_client_id) {
            const current_cookies = res.getHeader('set-cookie'); 
            res.setHeader('set-cookie', (current_cookies == undefined ? '' : current_cookies) + `client-id=${client_id}`);
        }
        const starting_time = now();
    
    
        const internal_request = http.request(`http://localhost:${PORT}${req.url}`, {headers: h, method: req.method}, (r) => {

        

            const response_headers = r.headers;

            for (const [key, value] of Object.entries(response_headers)) {
                res.setHeader(key, value);
            }

            let data = '';
            r.on('data', (chunk) => {data += chunk;});
            r.on('end', () => {
                const ending_time = now();
                const duration = ending_time - starting_time;

                if (client_group == Variant.A) {
                    historyA.push(new TimingRequest(starting_time, duration, client_id, requested_url.pathname));
                } else if (client_group == Variant.B) {
                    historyB.push(new TimingRequest(starting_time, duration, client_id, requested_url.pathname));
                }
                res.end(data);
            });
        });
    
        internal_request.on('error', (error) => {
            console.error(error);
            res.end(error.toString());
        });

        internal_request.end();

}




const server = http.createServer(requestListener);
server.listen(5000);
