# !/bin/sh

source .env
export $(cut -d= -f1 .env)

docker build --build-arg ML_PASSWORD=${ML_PASSWORD} -t ml-filter-image .


