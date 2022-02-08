
import urllib.request
import urllib.parse
import random
import json


BASE_URL = "http://localhost:8080"



def addItem(itemId, itemName, itemPrice):
    
    amt = random.randint(1, 1000)
    
    req = urllib.request.Request(f'{BASE_URL}/items/{itemId}', 
                                 data=itemName.encode('utf-8'), 
                                 method="PUT")
    req.add_header('Content-Type', 'application/json; charset=utf-8')
    _ = urllib.request.urlopen(req)

    req = urllib.request.Request(f'{BASE_URL}/stock/{itemId}', 
                                 data=str(amt).encode('utf-8'), 
                                 method="PUT")
    req.add_header('Content-Type', 'application/json; charset=utf-8')
    _ = urllib.request.urlopen(req)


    req = urllib.request.Request(f'{BASE_URL}/prices/{itemId}', 
                                 data=str(itemPrice).encode('utf-8'), 
                                 method="PUT")
    req.add_header('Content-Type', 'application/json; charset=utf-8')
    _ = urllib.request.urlopen(req)



if __name__ == '__main__':
    urllib.request.urlopen(urllib.request.Request(f"{BASE_URL}/items/", method="DELETE"))
    urllib.request.urlopen(urllib.request.Request(f"{BASE_URL}/stock/", method="DELETE"))
    urllib.request.urlopen(urllib.request.Request(f"{BASE_URL}/prices/", method="DELETE"))
    
    
    with open('./scripts-test-data/starter_items.tsv', 'r') as f:
        for line in f.read().split('\n')[1:]:
            # Kind of hacky --> ignore lines with just UUIDs (only there for potential future items)
            try:
                addItem(*line.split('\t'))
            except: 
                pass

                
    print('Done importing items into databases.')