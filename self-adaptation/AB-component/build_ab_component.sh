# !/bin/sh

source .env
export $(cut -d= -f1 .env)

docker build --build-arg AB_PASSWORD=${AB_PASSWORD} -t ab-component-image .


