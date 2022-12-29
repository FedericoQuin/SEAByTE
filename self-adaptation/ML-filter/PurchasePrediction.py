

from sklearn.linear_model import LinearRegression




class PurchasePrediction:
    def __init__(self) -> None:
        self.model = LinearRegression()
        
        
    def train(self, x, y):
        self.model.fit(x, y)
        
    def predict(self, x):
        return self.model.predict(x)
    
    
