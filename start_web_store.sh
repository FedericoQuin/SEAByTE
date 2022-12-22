# !/bin/bash


cd ./web-store


# Add executable permissions for the scripts used during setup of the web store
chmod u+x ./scripts/swarm_run.sh ./scripts/wait-for-it.sh ./scripts/generate_test_data.sh


if [ "$(docker info --format '{{.Swarm.LocalNodeState}}')" != "active" ]; then
	docker swarm init --advertise-addr 127.0.0.1 > /dev/null
fi

./scripts/swarm_run.sh


./scripts/wait-for-it.sh -t 120 localhost:8080 -- \
	./scripts/generate_test_data.sh

