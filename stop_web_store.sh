# !/bin/bash


for SERVICE in $(docker service ls --format {{.Name}})
do
    if [[ $SERVICE == WS_* || $SERVICE == ws-* ]];
    then
        docker service rm ${SERVICE}
    fi
done

docker stack rm WS




