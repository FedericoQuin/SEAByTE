
from typing import Sequence
from UserTemplate import UserTemplate
import locust
import random
import uuid
import time
import json
from Static import AVAILABLE_CLIENT_IDS, ITEMS


class RegularUser(UserTemplate):
    wait_time = locust.between(5, 15)

    def on_start(self):
        self.clientId: int = AVAILABLE_CLIENT_IDS.pop()
        self.cookies: dict[str, str] = {'client-id': str(self.clientId), 'user-id': str(uuid.uuid4())}
        
        version: str = 'A' if self.clientId <= int(self.getEnvironmentVariable('UserIdLimitA')) else 'B'
            
        self.chanceClickRec: float = float(self.getEnvironmentVariable(f'clickChance{version}'))
        self.chanceBuyRec: float = float(self.getEnvironmentVariable(f'purchaseChance{version}'))

        if len(ITEMS) == 0:
            raise RuntimeError('No items are present in the system, aborting...')
        self.items: Sequence[str] = [i['id'] for i in random.sample(ITEMS, min(len(ITEMS), 10))]
        

    @locust.task(3)
    def addItemToBasket(self):
        itemId: str = self.items[random.randrange(0, len(self.items))]
        amount: int = random.randrange(1, 50)
        
        self.client.post('/basket/add', cookies=self.cookies, 
                         json={'itemId': itemId, 'amount': str(amount)})

    
    @locust.task(1)
    def checkout(self):
        response = self.client.get('/checkout/overview', cookies=self.cookies)
        data = json.loads(response.text)
        
        if response.status_code == 200 and len(data['items']):
            time.sleep(1)
            
            self.handleRecommendations(data['recommendations'])

            self.client.post('/checkout/buy', cookies=self.cookies)
    
    
    def handleRecommendations(self, recommendations):
        if len(recommendations) == 0:
            return

        # roll for chance of viewing recommended items
        chanceClick: float = random.random()
        
        chosenItem: str = recommendations[random.randrange(0, len(recommendations))]['itemId']
        
        
        if chanceClick <= self.chanceClickRec:
            self.getInfoRecItem(chosenItem)

        # roll for a chance of adding extra recommended items
        chancePurchase = random.random()
        
        if chancePurchase <= self.chanceBuyRec:
            self.buyRecItem(chosenItem)
            
    
    
    def getInfoRecItem(self, itemId: str) -> None:
        self.client.get(f'/recommendation/info/{itemId}', cookies=self.cookies, name='/recommendation/info') # type: ignore
        time.sleep(1)
        
    
    
    def buyRecItem(self, itemId: str) -> None:
        amount = random.randrange(1, 20)
        self.client.get(f'/recommendation/buy/{itemId}?{amount}', cookies=self.cookies, name='/recommendation/buy') # type: ignore
        time.sleep(1)



