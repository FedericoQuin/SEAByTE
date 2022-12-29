from flask import Flask, Response, request, jsonify
import os
import random
from PurchasePrediction import PurchasePrediction


POPULATION_SPLIT_NAME = os.environ.get('POPULATION_SPLIT_NAME')


app = Flask(__name__)
ml_model = PurchasePrediction()


@app.before_first_request
def init():
    ml_model.train([[0, 0.2, 3], [0.5, 0.5, 2], [1, 0, 6], [0.1, 3, 10], [0.5, 2, 1]], 
                   [0, 0, 1, 0, 1])



@app.route('/predict', methods=['POST'])
def make_prediction():
    # Fetch the user cookie for the client ID
    client_id = request.cookies.get('client-id', -1)
    
    # Make prediction of (in this case purchasing likelihood) using the classification model
    # TODO (toy example for now)
    result = ml_model.predict([[random.uniform(0, 1), random.uniform(0, 5), random.uniform(0, 10)]])
    
    # Return the result
    return jsonify(result[0])


@app.route('/ping')
def ping():
    return Response('pong', mimetype='text/plain')


@app.route('/getName', methods=['GET'])
def name():
    return Response(POPULATION_SPLIT_NAME, mimetype='text/plain')


