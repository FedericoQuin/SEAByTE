
import os
import json
from typing import Any, Sequence
from urllib import request


def retrieve_purchasing_users() -> Sequence[int]:
    if not os.path.exists(os.path.join('data', 'purchasing_user_ids.txt')):
        return []
    
    with open(os.path.join('data', 'purchasing_user_ids.txt'), 'r') as f:
        return [int(i) for i in f.read().split('\n') if i != '']


AVAILABLE_CLIENT_IDS: list[int] = list(range(int(os.environ.get('startId', '0')), 
                                  int(os.environ.get('startId', '0')) + int(os.environ.get('numberOfUsers', '0'))))
ITEMS: Sequence[Any] = json.load(request.urlopen('http://localhost:8080/items'))

PURCHASING_USER_IDS: Sequence[int] = retrieve_purchasing_users()

