
import urllib.request
import urllib.parse

# from scripts.load_default_items import BASE_URL



BASE_URL = "http://localhost:8080"


def addAccount(accountName, accountHash):
    req = urllib.request.Request(f'{BASE_URL}/accounts/{accountName}', 
                                 data=accountHash.encode('utf-8'), 
                                 method="PUT")
    req.add_header('Content-Type', 'application/json; charset=utf-8')
    _ = urllib.request.urlopen(req)



if __name__ == '__main__':
    urllib.request.urlopen(urllib.request.Request(f"{BASE_URL}/accounts/", method="DELETE"))
    
    
    with open('./scripts-test-data/starter_accounts.tsv', 'r') as f:
        for line in f.read().split('\n')[1:]:
            addAccount(*line.split('\t'))

                
    print('Done importing accounts into databases.')
