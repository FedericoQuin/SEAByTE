const http = require('http');
const url = require('url');
var now = require("performance-now");
const fetch = require('node-fetch');


const PORT = 80;
const AB_COMPONENT_NAME = process.env.AB_COMPONENT_NAME;
const POPULATION_SPLIT_NAME = process.env.POPULATION_SPLIT_NAME;
const POPULATION_SPLIT_TARGET = process.env.POPULATION_SPLIT_TARGET;


class Variant {
    static A = new Variant('A');
    static B = new Variant('B');

    // Not part of the AB test
    static NULL = new Variant('A');

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




class AssignmentFunction {
    async determineAssignment(id) {
        throw('Method "determineAssignment" not implemented!');
    }

    getWeightA() {
        throw('Method "getWeightA" not implemented!');
    }

    getWeightB() {
        throw('Method "getWeightB" not implemented!');
    }

    reassignUsers(newA, newB, clients) {
        throw('Method "reassignUsers" not implemented!');
    }
}

class DefaultAssignmentFunction extends AssignmentFunction {
    constructor(a, b) {
        super();
        this.weightA = a;
        this.weightB = b;
        this.progress = 0;
    }

    async determineAssignment(_) {
        let variant = (this.progress % 100) < this.weightA ? Variant.A : Variant.B;
        this.progress += Math.min(this.weightA, this.weightB);
        return variant;
    }

    getWeightA() {
        return this.weightA;
    }

    getWeightB() {
        return this.weightB;
    }

    reassignUsers(newA, newB, clients) {
        if (this.getWeightA() < newA) {
            // Increased traffic to variant A ---> adjust clients of group B
            const difference_weight = newA - this.getWeightA();
            const amtToConvert = Math.ceil(clients.length * (difference_weight / 100.0));

            clients
                .filter(c => c.group == Variant.B)
                .map(c => ({c, random: Math.random() }))
                .sort((newA, newB) => newA.random - newB.random)
                .map(({c}) => c)
                .slice(amtToConvert-1)
                .forEach(c => c.group = Variant.A);
        } else {
            // Other way around: increased traffic to variant B ---> switch clients from A to B
            const difference_weight = newB - this.getWeightB();
            const amtToConvert = Math.ceil(clients.length * (difference_weight / 100.0));

            clients
                .filter(c => c.group == Variant.A)
                .map(c => ({c, random: Math.random() }))
                .sort((newA, newB) => newA.random - newB.random)
                .map(({c}) => c)
                .slice(amtToConvert-1)
                .forEach(c => c.group = Variant.B);
        }

        this.progress = 0;
        this.weightA = newA;
        this.weightB = newB;
    }
}

class LimitedAssignmentFunction extends AssignmentFunction {
    constructor(a, b, userLimit) {
        super();
        this.weightA = a;
        this.weightB = b;
        this.userLimit = userLimit;
    }

    async determineAssignment(id) {
        return (id <= Math.floor(this.userLimit * this.weightA / 100.0)) ?
            Variant.A : Variant.B;
    }

    getWeightA() {
        return this.weightA;
    }

    getWeightB() {
        return this.weightB;
    }

    reassignUsers(newA, newB, clients) {
        this.weightA = newA;
        this.weightB = newB;
        clients.forEach(c => c.group = this.determineAssignment(c.id));
    }
}

class PopulationSplitAssignmentFunction extends AssignmentFunction {
    constructor(populationSplitName, targetValue, assignmentFunction) {
        super();
        this.populationSplitName = populationSplitName;
        this.targetValue = targetValue;
        this.assignmentFunction = assignmentFunction;
    }

    async determineAssignment(id) {
        let header = [];
        header['cookie'] = `client-id=${id}`

        return await fetch(`http://${this.populationSplitName}/predict`, 
                {method: 'post', headers: {'Cookie': `client-id=${id}`}})
            .then((response) => response.json())
            .then((res) => {
                // If the predicted value is not the value we desire, the user should not participate in the A/B test
                return res === this.targetValue ? this.assignmentFunction.determineAssignment(id) : Variant.NULL;
            })
            .catch(_ => {return this.assignmentFunction.determineAssignment(id);});
    }

    getWeightA() {
        return this.assignmentFunction.getWeightA();
    }

    getWeightB() {
        return this.assignmentFunction.getWeightB();
    }

    reassignUsers(newA, newB, clients) {
        this.assignmentFunction.reassignUsers(newA, newB, clients);
    }
}




class ABState {
    constructor() {
        this.clients = [];
        this.assignmentFunction = POPULATION_SPLIT_NAME === undefined ? 
            new DefaultAssignmentFunction(50, 50) : 
            new PopulationSplitAssignmentFunction(POPULATION_SPLIT_NAME, POPULATION_SPLIT_TARGET, new DefaultAssignmentFunction(50, 50));
    }



    async addClient(id) {
        const variant = await this.assignmentFunction.determineAssignment(id);
        this.clients.push(new ClientID(id, variant));
    }

    hasClient(clientId) {
        return this.clients.filter(client => client.id == clientId).length > 0;
    }


    getGroup(clientId) {
        return this.clients.filter(client => client.id == clientId)[0].group;
    }

    clearClients() {
        this.clients.length = 0;
    }


    adjustWeights(a, b) {
        this.assignmentFunction.reassignUsers(a, b, this.clients);
    }

    adjustWeightsWithCustomAssignment(a, b, userLimit) {
        this.assignmentFunction = new LimitedAssignmentFunction(a, b, userLimit);
        this.assignmentFunction.reassignUsers(a, b, clients);
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
            return;
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
        if (params.has('userLimit') && POPULATION_SPLIT_NAME === undefined) {
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




const requestListener = async function (req, res) {
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

    if (!state.hasClient(client_id)) {
        // Add the client-id to one of the scenarios
        await state.addClient(client_id);
    }
    pass_internal_request(req, h, has_client_id, client_id, res, requested_url);
}



function pass_internal_request(req, headers, has_client_id, client_id, res, requested_url) {
    const client_group = state.getGroup(client_id);
    headers['cookie'] += `; scenario${client_group.name}_${AB_COMPONENT_NAME}=true`;

    if (!has_client_id) {
        const current_cookies = res.getHeader('set-cookie'); 
        res.setHeader('set-cookie', (current_cookies == undefined ? '' : current_cookies) + `client-id=${client_id}`);
    }
    const starting_time = now();


    const internal_request = http.request(`http://localhost:${PORT}${req.url}`, {
            headers: headers, method: req.method}, 
            (response) => {
        const response_headers = response.headers;

        for (const [key, value] of Object.entries(response_headers)) {
            res.setHeader(key, value);
        }

        let data = '';
        response.on('data', (chunk) => {data += chunk;});
        response.on('end', () => {
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
