

from typing import Sequence, List, Dict
from UserTemplate import UserTemplate
import locust
import random
import uuid
import time
import json
from Static import AVAILABLE_CLIENT_IDS, ITEMS, PURCHASING_USER_IDS



class PurchasingUser(UserTemplate):
    wait_time = locust.between(4, 10)
    
    

    def on_start(self):
        assignment: int = AVAILABLE_CLIENT_IDS.pop() - 1

        self.clientIds: List[int] = list(range(assignment * 3400, (assignment + 1) * 3400))
        random.shuffle(self.clientIds)
        self.userIds: Dict[int, str] = {k: str(uuid.uuid4()) for k in self.clientIds}
        
        self.index: int = 0
        self.isPurchasingUser = False
        self.userSwitchCounter = 0
        self.update_cookies()
        
        
        self.chanceAddRecommendationA = float(self.getEnvironmentVariable(f'addRecommendationChanceA'))
        self.chanceAddRecommendationB = float(self.getEnvironmentVariable(f'addRecommendationChanceB'))
        
        self.chanceBuyA = float(self.getEnvironmentVariable(f'purchaseChanceA'))
        self.chanceBuyB = float(self.getEnvironmentVariable(f'purchaseChanceB'))
        
        self.abComponentPort = self.getEnvironmentVariable(f'abComponentPort')

        

        if len(ITEMS) == 0:
            raise RuntimeError('No items are present in the system, aborting...')
        self.items: Sequence[str] = [i['id'] for i in random.sample(ITEMS, min(len(ITEMS), 10))]
        
        
    def update_cookies(self) -> None:
        self.client_id = self.clientIds[self.index % len(self.clientIds)]
        self.cookies = {'client-id': str(self.client_id), 'user-id': self.userIds[self.client_id]}
        self.isPurchasingUser = self.client_id in PURCHASING_USER_IDS
        self.index += 1
        

    @locust.task(3)
    def addItemToBasket(self):
        itemId: str = self.items[random.randrange(0, len(self.items))]
        amount: int = random.randrange(1, 50)
        
        self.client.post('/basket/add', cookies=self.cookies, 
                         json={'itemId': str(itemId), 'amount': str(amount)})

    
    @locust.task(1)
    def checkout(self):
        response = self.client.get('/checkout/overview', cookies=self.cookies)
        data = json.loads(response.text)
        
        if response.status_code == 200 and len(data['items']):
            time.sleep(1)
            
            variant: str = 'A' if self.isPurchasingUser else \
                self.client.get(f'http://localhost:{self.abComponentPort}/adaptation/checkVariant?client-id={self.client_id}').text
            
            self.handleRecommendations(data['recommendations'], variant)

            # roll for a chance of adding extra recommended items
            chancePurchase: float = random.random()
            chance: float = self.chanceBuyA if variant == 'A' else self.chanceBuyB 
                
            if chancePurchase <= chance:
                self.client.post('/checkout/buy', cookies=self.cookies)
            else:
                self.client.post('/checkout/clear', cookies=self.cookies)
            
            
            
            self.userSwitchCounter += 1
            
            if self.userSwitchCounter >= 3:
                self.update_cookies()
                self.userSwitchCounter = 0
    
    
    def handleRecommendations(self, recommendations: Sequence[str], variant: str):
        if len(recommendations) == 0:
            return

        # roll for a chance of adding extra recommended items
        chanceExtraPurchase: float = random.random()
        chance: float = self.chanceAddRecommendationA if variant == 'A' else self.chanceAddRecommendationB
            
        if chanceExtraPurchase <= chance:
            self.buyRecItem(recommendations[random.randrange(0, len(recommendations))])
            
            
    def buyRecItem(self, itemId: str):
        amount: int = random.randrange(1, 20)
        self.client.get(f'/recommendation/buy/{itemId}?{amount}', cookies=self.cookies, 
                        name='/recommendation/buy') # type: ignore        
        time.sleep(1)



