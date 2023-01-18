
import os
import json
from typing import Any, Sequence
from urllib import request


AVAILABLE_CLIENT_IDS: list[int] = list(range(int(os.environ.get('startId', '0')), 
                                  int(os.environ.get('startId', '0')) + int(os.environ.get('numberOfUsers', '0'))))
ITEMS: Sequence[Any] = json.load(request.urlopen('http://localhost:8080/items'))

