
import locust
import random
import uuid
import time
import json
import os
from Static import AVAILABLE_CLIENT_IDS, ITEMS


class RegularUser(locust.HttpUser):
    wait_time = locust.between(5, 15)

    def on_start(self):
        self.clientId = AVAILABLE_CLIENT_IDS.pop()
        self.cookies = {'client-id': str(self.clientId), 'user-id': str(uuid.uuid4())}
        
        version = 'A' if self.clientId <= int(os.environ.get('UserIdLimitA')) else 'B'
            
        self.chanceClickRec = float(os.environ.get(f'clickChance{version}'))
        self.chanceBuyRec = float(os.environ.get(f'purchaseChance{version}'))

        if len(ITEMS) == 0:
            raise RuntimeError('No items are present in the system, aborting...')
        self.items = [i['id'] for i in random.sample(ITEMS, min(len(ITEMS), 10))]
        

    @locust.task(3)
    def addItemToBasket(self):
        itemId = self.items[random.randrange(0, len(self.items))]
        amount = random.randrange(1, 50)
        
        self.client.post('/basket/add', cookies=self.cookies, 
                         json={'itemId': str(itemId), 'amount': str(amount)})

    
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
        chanceClick = random.random()
        
        chosenItem = recommendations[random.randrange(0, len(recommendations))]
        
        
        if chanceClick <= self.chanceClickRec:
            self.getInfoRecItem(chosenItem)

        # roll for a chance of adding extra recommended items
        chancePurchase = random.random()
        
        if chancePurchase <= self.chanceBuyRec:
            self.buyRecItem(chosenItem)
            
    
    
    def getInfoRecItem(self, itemId):
        self.client.get(f'/recommendation/info/{itemId}', cookies=self.cookies, name='/recommendation/info')
        time.sleep(1)
        
    
    
    def buyRecItem(self, itemId):
        amount = random.randrange(1, 20)
        self.client.get(f'/recommendation/buy/{itemId}?{amount}', cookies=self.cookies, name='/recommendation/buy')
        time.sleep(1)



