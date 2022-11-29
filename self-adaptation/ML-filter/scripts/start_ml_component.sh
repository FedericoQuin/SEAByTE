# !/bin/bash

/usr/sbin/sshd
cd /app && /usr/bin/nohup flask  --app ML_server run --port=5001 &

./wait-for-it.sh -t 60 localhost:5001 -- /bin/bash ./start_nginx.sh
