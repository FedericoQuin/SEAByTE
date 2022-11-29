# !/bin/bash

/usr/sbin/sshd
/usr/bin/nohup /usr/bin/node ./adaptation_server.js &

./wait-for-it.sh -t 60 localhost:5000 -- /bin/bash ./start_nginx.sh
