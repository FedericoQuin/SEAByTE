

# SEAByTE

Artifact website: [https://people.cs.kuleuven.be/danny.weyns/software/SEAByTE/](https://people.cs.kuleuven.be/danny.weyns/software/SEAByTE/)

## Contents

SEAByTE consists of two main parts: a microservice-based Web Store application and a Self-Adaptation Managing System equipped with a Dashboard, supporting the setup and conduct of A/B experiments in the underlying Web Store. In the following sections, we will give concise descriptions on how to build and run the artifact.

If you run into errors during or after the installation process, please check the [Troubleshooting Section](#troubleshooting) first to check if your issue is listed there.


## Prerequisites

### Network ports

Both parts of the artifact provide API's exposed to the local machine the artifact is running on. Concretely, the Web Store uses network port `8080`, and the Managing System uses network port `8070`. Both these ports should be available on the system you want to run the artifact on.

The setup of an A/B experiment also exposes a network port in order to directly communicate with an A/B component that manages A/B routing in the application. This network port is randomly chosen at runtime, without requiring input from the user.


### Required packages/libraries

Running the artifact first requires the installation of a few libraries. The following libraries are required:

- Java Runtime (17 or higher)
- Maven (tested with version 3.8.4)
- Docker (tested with version 20.10.20)
- Docker-compose v2 (tested with version 2.12.0)
- Python 3 (tested with 3.9.7)
- Python 3 Virtual Environments (tested with 3.9.7)


Ensure that no root privileges are required to run docker on your machine (cfr. [Linux docker post installation steps](https://docs.docker.com/engine/install/linux-postinstall/)).

Docker compose v2 has not been tested for this artifact. Therefore, for people using Docker Desktop (in Mac/Windows), we advise to turn of docker compose v2 features (see [here (bottom option)](https://docs.docker.com/desktop/mac/images/menu/prefs-general.png)).



## Building


In order to build both parts of the artifact, run the following shell script in the top level directory of this package (note: it might be necessary to add executable permissions to the file): `./build.sh`

Running the build script builds the Web Store microservices as separate docker images, builds a docker image for the A/B component and sets up the python 3 virtual environment used to simulate end-users in the Web Store application.


**NOTE**: to run the default experiments (upgrading the recommendation service from version 1.0.0 to version 1.1.0) you have to import the docker image which contains version 1.1.0. This docker image can be downloaded separately on the artifact website and imported into your local docker image repository with the following command:  
`docker load --input ./recommendation-1.1.0-image.tar`


## Running


To run the application, two scripts are provided: `./start_web_store.sh` and `./start_dashboard.sh`. Run both scripts sequentially one after another (first the Web Store script, then the dashboard script).

The Web Store script deploys the microservice images as a docker stack on a local docker swarm node. Currently, the artifact has only been tested in a local setting. In future versions, we aim to test and support deploying the stack on multiple swarm nodes.

After deplying the Web Store, run the dashboard script. This script boots the Managing System alongside the dashboard used to monitor and configure A/B experiments in the Web Store. The dashboard is made available to the user under the following url: `http://localhost:8070`.  
The dashboard script runs continuously and thus has to be stopped manually (e.g. via kill signal with `Ctrl+c`). **Make sure that you always stop the feedback loop via the dashboard (if it has been started beforehand) prior to completely shutting down the managing system with a kill signal.** If the feedback loop is not stopped before shutdown, some docker services can remain deployed and have to be removed manually. See the [Troubleshooting Section](#troubleshooting) for further information if necessary.

_NOTE_: Execute permission similarly might have to be set for the scripts.



## Running default scenario (recommendation upgrade scenario)

_NOTE: make sure the recommendation image with version 1.1.0 has been imported into your local docker image repository (see the [Build Section](#building))._

To run the default scenario where two versions of the recommendation microservice are tested, first open up the dashboard. 

The first tab - `Home` - contains information about the (running feedback loop). Initially, the feedback loop is not running and it thus does not contain much information yet.

The following four tabs (`Setup` - `Profile` - `Experiment` - `Rule`) contain input forms which can be used to specify setups, user profiles, experiments and transition rules respectively. In each of these tabs we have also provided a button on top to add the default setup, user profiles, ... to be able to run the default scenario.
For each tab, **in the respective order**, add the default objects.

The last tab - `Run` - lists the objects present in the system and contains a small form to setup an experimental pipeline. For the default scenario:
- Choose the `Recommendation_upgrade` setup
- Select all experiments
- Select all transition rules
- Choose the `Upgrade v1.0.0 - v1.1.0` experiment as the starting experiment of the pipeline

After doing the above, start the feedback loop. This will deploy the specified A and B microservice instances as well as the A/B component in the Web Store. The status bar at the top of the page displays the progress of setup up the experimental pipeline. Once the pipeline is ready, the status bar displays that the feedback loop is ready.

To monitor the pipeline after starting the feedback loop, navigate back to the `Home` tab. Here you can find information about the currently running experiment and a history of steps taken in the experiment pipeline.



## Stopping and cleanup

After testing the artifact, very little cleanup is necessary. First, make sure you stop the feedback loop using the Dashboard web interface (bottom of the `Run` tab). Afterwards you can safely stop the Managing System (stopping the running process).

Lastly, run the `./stop_web_store.sh` script. This script removes the deployed docker stack which contains the Web Store microservices.




## Troubleshooting


### I stopped the dashboard without stopping a running feedback loop in the web interface first

The feedback loop automatically deploys three docker services: a service for each A/B variant and a service for the A/B (routing) component. Stopping the feedback loop in the dashboard removes these three services before shutting down the feedback loop.

In order to manually remove the containers, run the following command for each deployed docker container as specified in the setup of your experiments (replace `$name` with the name of each container):
`docker service rm $name`

When running the default scenario we would for example have to run the following commands:
```
docker service rm ws-recommendation-service
docker service rm ws-recommendation-service-1-0-0
docker service rm ws-recommendation-service-1-1-0
```


### I am getting errors that the `source` command has not been found

Most likely, the reason you are seeing this error is because you are running the scripts with sudo rights. Make sure you run the scripts without sudo rights instead.

If this causes issues with your docker installation (i.e. docker needs sudo rights), you can create a user group specifically for docker and add the current user to it (cfr. [docker post-installation steps for linux](https://docs.docker.com/engine/install/linux-postinstall/)).


### Ubuntu 20.04 only has Java 16 available in its Apt package manager. Can I run with Java 16 as well?

Yes, with a few minor adjustments. The Web Store application runs completely in docker containers - no adjustments are needed there. The Managing System with its Dashboard do not run in a docker container, and thus rely on the Java installation and version of the system you are running the artifact on.  
To adjust the java version, edit the maven configuration file `self-adaptation/pom.xml` and adjust the compiler version from `17` to `16` (line 23).

On a very similar note: the default maven version available on the ubuntu 20.04 image does not seem to support java 16 or 17. A required upgrade of the maven version might thus be unavoidable.


