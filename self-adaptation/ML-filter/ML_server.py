from flask import Flask
from flask import request
from flask import Response
from flask import render_template
import math
import requests
import random
from enum import Enum

PORT = 80
AB_COMPONENT_NAME = 'test' #process.env.AB_COMPONENT_NAME;


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


state = ABState()


@app.route("/recommendation")
def select_ab_test():
    try:
        client_id = request.cookies['client-id']
    except:
        last_client_id = 1
        while (state.hasClient(last_client_id)):
            last_client_id += 1
        client_id = last_client_id
        ##Append the newly created client ID to the cookie header such that the client stores and uses it in subsequent requests
        
    if not state.hasClient(client_id):
        state.addClient(client_id)
    client_group = state.getGroup(client_id)
    response = Response()
    response.set_cookie(f"scenario{client_group.name}_{AB_COMPONENT_NAME}",'true')
    response.set_cookie('client-id',str(client_id))
    
    return response

    
        

@app.route("/recommendation/adaptation/")
def adaptation_listenen():
    try:
        test = request.cookies.get('userID')
    except:
        response= make_response()



    
    internal_request = requests.get("http://localhost:${PORT}${req.url}")


        