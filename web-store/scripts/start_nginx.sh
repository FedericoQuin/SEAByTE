#!/bin/bash

# /usr/sbin/sshd


CONF_FILE=/app/nginx.conf
if [ ! -f "$CONF_FILE" ]; then
    ./generate_nginx.sh
fi

nginx -g 'daemon off;'
