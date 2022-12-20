from flask import Flask
from flask import request
from flask import Response
from flask import render_template
import math
import requests
import random
from enum import Enum
import os
import sys

PORT = 80
AB_COMPONENT_NAME = os.environ.get('AB_COMPONENT_NAME')
historyA = []
historyB = []
last_client_id = 0


app = Flask(__name__)
class Variant(Enum):
    A = 'A'
    B = 'B'


class ClientID :
    
    def __init__(self, id, group):
        self.id = id
        # The group signifies either A or B in the case of AB testing
        self.group = group
    


class ABState:
    def __init__(self):
        self.clients = []
        self.progress = 0
        self.weightA =50
        self.weightB =50
        

    def determineAssignment(self,id):

            variant =  Variant.A if (self.progress % 100) < self.weightA else Variant.B
            self.progress += min(self.weightA, self.weightB)
            return variant

    def addClient(self,id):
        variant = self.determineAssignment(id)
        self.clients.append(ClientID(id, variant))
    

    def hasClient(self, clientId):
        def checkClient(client):
            return client.id == clientId

        return len(list(filter(checkClient,self.clients))) > 0
    
    


    def getGroup(self, clientId):
        def checkClient(client):
            return client.id == clientId
        filtered = list(filter(checkClient,self.clients))
        return filtered[0].group

    def setWeightA(self, weight):
        self.weightA = weight


    def WeightB(self,weight):
        self.weightB = weight
    

    def clearClients(self):
        self.clients= list()


    def adjustWeights(self, a, b):
        
        if (self.assignmentFunction.weightA < a):
            # Increased traffic to variant A ---> adjust clients of group B
            difference_weight = a - self.weightA
            amtToConvert = math.ceil(len(self.clients) * (difference_weight / 100.0))

            filtered = filter(lambda c:  c.group == Variant.B,self.clients)
            mapped = map(lambda c :  [c, random.random() ],filtered)
            sorted = list(mapped.sort(lambda a,b :a[1] - b[1]))


            for c in sorted[:amtToConvert-1]:
                c.group = Variant.A 
        else:
            # Other way around: increased traffic to variant B ---> switch clients from A to B
            difference_weight = b - self.weightB
            amtToConvert = math.ceil(self.clients.length * (difference_weight / 100.0))

            filtered = filter(lambda c:  c.group == Variant.A,self.clients)
            mapped = map(lambda c :  [c, random.random() ],filtered)
            sorted = list(mapped.sort(lambda a,b :a[1] - b[1]))


            for c in sorted[:amtToConvert-1]:
                c.group = Variant.B
        

        # Adjust the final weights
        self.assignmentFunction = ABState.defaultAssignmentFunction(a, b)

    def adjustWeightsWithCustomAssignment(self, a, b, userLimit):
        class assignmentFunction:
            weightA = a
            weightB = b,
            userLimit = userLimit

            def determineAssignment(id):
                return Variant.A if id <= math.floor(self.userLimit * self.weightA / 100.0) else Variant.B


        for c in self.clients:
            c.group = assignmentFunction.determineAssignment(c.id)
        # A and B assigment happens based on the receiver limit of users that are going to connect to the system

class TimingRequest():
    def __init__(self, duration, clientId, url):
        self.duration = duration
        self.clientId = clientId
        self.requestedUrl = url   



state = ABState()

@app.route("/variant")
def select_variant():
    global last_client_id
    while (state.hasClient(last_client_id)):
        last_client_id += 1
    client_id = last_client_id
    ##Append the newly created client ID to the cookie header such that the client stores and uses it in subsequent requests
        
    if not state.hasClient(client_id):
        state.addClient(client_id)
    client_group = state.getGroup(client_id)
    
    response = Response(client_group.name, mimetype="text/plain")
    return response




"""@app.route("/recommendation")
def select_ab_test():
    try:
        client_id = request.cookies['client-id']
    except:
        global last_client_id
        while (state.hasClient(last_client_id)):
            last_client_id += 1
        client_id = last_client_id
        ##Append the newly created client ID to the cookie header such that the client stores and uses it in subsequent requests
        
    if not state.hasClient(client_id):
        state.addClient(client_id)
    client_group = state.getGroup(client_id)
    
    new_url = f'http://localhost:{PORT}{request.path}'
    new_method= request.method
    new_headers =request.headers
    new_headers['cookie']
    internal_response = requests.request(new_method, new_url, headers=new_headers)
    
    data = ''
    global historyA
    global historyB
    
    if client_group == Variant.A :
                historyA.append(TimingRequest(internal_response.elapsed, client_id, internal_response.url))
    elif client_group == Variant.B :
                historyB.append(TimingRequest(internal_response.elapsed, client_id, internal_response.url))
        

    response = Response(internal_response.json)
    response.set_cookie(f"scenario{client_group.name}_{AB_COMPONENT_NAME}",'true')
    response.set_cookie('client-id',str(client_id))
    
    return response

    
        

@app.route("/recommendation/adaptation/history")
def adaptation_history_listenen():
    variant = request.args.get('variant')
    if variant is None:
        response = Response()
        response.end('No variant specified.')
        response.statusCode = 400
        return response
    history = historyA if variant == Variant.A.name else historyB if variant == Variant.B.name else undefined

    response = Response(str(history))
    if request.args.get('removeAfter') is not None:
        history.clear()
    return response
    

    
@app.route("/recommendation/adaptation/change")
def adaptation_change_listenen():
    
    weight_a = int(request.args.get(Variant.A.name))
    weight_b = int(request.args.get(Variant.B.name))

    if (weight_a + weight_b != 100):
        response = Response(f'Invalid weights for A and B: does not add up to a total sum of 100 (sum={weight_a+weight_b}).', status=400)
        return response
    userLimit = request.args.get('userLimit')
    if userLimit is not None:
        userLimit = int(userLimit)
        state.adjustWeightsWithCustomAssignment(weight_a, weight_b, userLimit)
        response = Response(f'Adjusted A/B weights of this AB component to {weight_a} and {weight_b} with custom assignment function.')
        return response
    else:
        state.adjustWeights(weight_a, weight_b)
        response = Response(f'Adjusted A/B weights of this AB component to {weight_a} and {weight_b}.')
        return response
        

@app.route("/recommendation/adaptation/reset")
def adaptation_reset_listenen():
    historyA.clear()
    historyB.clear()
    last_client_id = 0
    state.clearClients()
    response = Response('Done.')
    return response
"""

        