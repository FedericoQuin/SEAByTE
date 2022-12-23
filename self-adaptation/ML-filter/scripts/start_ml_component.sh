# !/bin/bash

/usr/sbin/sshd
cd /app && flask --app ML_server run --host=0.0.0.0 --port=80
