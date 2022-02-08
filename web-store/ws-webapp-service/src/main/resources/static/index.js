




function getCookie(name) {
    let allCookies = document.cookie;

    for (let cookie of allCookies.split('; ')) {
        [key, value] = cookie.split('=');

        if (key === name) {
            return value;
        }
    }

    throw `No cookie with key ${name}`;
}




function onLoadBodyIndex() {
    let table = document.getElementById("inventoryTable");
    table.innerHTML = '<div class="flex-item-c1"><b><u>Item</u></b></div>' + 
        '<div class="flex-item-c2"><b><u>Stock</u></b></div>' +
        '<div class="flex-item-c3"><b><u>Price / piece</u></b></div>';

    const userAction = async () => {
        const response_stock = await fetch('/stock/');
        const response_price = await fetch('/prices/');
        const response_stock_js = await response_stock.json();
        const response_price_js = await response_price.json();

        for (let item of response_stock_js) {
            addInventoryItem(table, item["item"]["name"], item["amount"], searchPrice(item["item"]["id"], response_price_js));
        }
    }

    userAction.apply();


    fillInUsername();
}

function onLoadBodyLogin() {
    fillInUsername();
}


function fillInUsername() {
    try {
        const username = getCookie("sessionUsername");
        document.getElementById("user").innerHTML = username;
    } catch (err) {
        document.getElementById("user").innerHTML = 'Not logged in.';
    }
}




function searchPrice(id, prices, default_price="N/A") {
    for (let price of prices) {
        // MongoDB replaces the actual Id field name with the name 'id'
        if (price["id"] === id) {
            return `€ ${Number(price["price"]).toFixed(2)}`;
        }
    }
    return default_price;
}


function addInventoryItem(table, name, amount, price) {
    table.innerHTML += `<div class="flex-item-c1">${name}</div>`;
    table.innerHTML += `<div class="flex-item-c2">${amount}</div>`;
    table.innerHTML += `<div class="flex-item-c3">${price}</div>`;
}







function login() {
    let login_result = async () => {
        const username = document.getElementById("username").value;
        const password = document.getElementById("password").value;
    
        return await fetch(`/login?username=${username}&hash=${password}`, {
            method: "POST"
        });
    }
    
    login_result.apply()
        .then(res => {
            console.log(res.status);
            document.getElementById("username").value = '';
            document.getElementById("password").value = '';
            location.href = "/";
        })
        .catch(err => document.getElementById("login-status").value="Invalid username or password.");
}

