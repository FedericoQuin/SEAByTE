#! /bin/sh



if [ $# -gt 0 ]; then
    cp $1 ./.env
fi


if [ ! -f ".env" ]; then
    cp scripts/default.env ./.env
fi

export $(grep -v '^#' .env | xargs -d '\n')


echo $VERSION

docker-compose -f docker-swarm.yml build 
docker stack deploy -c docker-swarm.yml $(echo WS-$VERSION | sed "s/\./\-/g")


