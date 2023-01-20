
from typing import Dict
from UserTemplate import UserTemplate
import locust
import random
import uuid
from Static import AVAILABLE_CLIENT_IDS


class FeedbackUser(UserTemplate):
    wait_time = locust.between(5, 15)

    MIN_SCORE: int = 0
    MAX_SCORE: int = 10
    
    @staticmethod
    def clamp(value: float):
        return max(min(value, FeedbackUser.MAX_SCORE), FeedbackUser.MIN_SCORE)

    def on_start(self):
        self.clientId: int = AVAILABLE_CLIENT_IDS.pop()
        self.cookies: Dict[str, str] = {'client-id': str(self.clientId), 'user-id': str(uuid.uuid4())}
        
        version: str = 'A' if self.clientId <= int(self.getEnvironmentVariable('UserIdLimitA')) else 'B'
        
        
        
        if version == 'A':
            self.mu = float(self.getEnvironmentVariable('feedbackRatingMuA'))
            self.std = float(self.getEnvironmentVariable('feedbackRatingStdA'))
        else:
            self.mu = float(self.getEnvironmentVariable('feedbackRatingMuB'))
            self.std = float(self.getEnvironmentVariable('feedbackRatingStdB'))
        

    @locust.task(10)
    def index(self):
        self.client.get('/index', cookies=self.cookies)
        
        
        
    @locust.task(1)
    def leave_feedback(self):
        score: float = FeedbackUser.clamp(random.normalvariate(self.mu, self.std))
        self.client.post(f'/feedback/{score}', cookies=self.cookies, name=f'/feedback/{score:.0f}') # type: ignore
    
    

