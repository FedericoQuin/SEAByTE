
import os
import json
import urllib


AVAILABLE_CLIENT_IDS = list(range(int(os.environ.get('startId')), 
                                  int(os.environ.get('startId')) + int(os.environ.get('numberOfUsers'))))
ITEMS = json.load(urllib.request.urlopen('http://localhost:8080/items'))

