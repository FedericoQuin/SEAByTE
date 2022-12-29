# !/bin/bash


# Add executable permissions for the used shell scripts during building
chmod u+x ./web-store/scripts/swarm_build.sh ./self-adaptation/AB-component/build_ab_component.sh ./self-adaptation/ML-filter/build_ml_component.sh


# Build docker images for all microservices

echo $'\n\n\n\n==========================================='
echo "Building all the microservice docker images"
echo $'===========================================\n\n'
cd ./web-store && ./scripts/swarm_build.sh && cd - > /dev/null

# Build the AB component docker image
echo $'\n\n\n\n======================================'
echo "Building the AB component docker image"
echo $'======================================\n\n'
cd ./self-adaptation/AB-component && ./build_ab_component.sh && cd - > /dev/null

# Build the ML component docker image
echo $'\n\n\n\n======================================'
echo "Building the ML component docker image"
echo $'======================================\n\n'
cd ./self-adaptation/ML-filter && ./build_ml_component.sh && cd - > /dev/null

# Create the python virtual environment used to run the end-user profiles
echo $'\n\n\n\n========================================================================================'
echo "Setting up Python 3 virtual environment used to run the end-user profiles in experiments"
echo $'========================================================================================\n\n'
cd ./self-adaptation/Locust \
	&& python3 -m venv ./venv \
	&& ./venv/bin/python3 -m pip install -r requirements.txt \
	&& cd - > /dev/null

