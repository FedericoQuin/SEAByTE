
#!/bin/bash
source ./venv/bin/activate
locust --headless --users $2 --spawn-rate $3 -H http://localhost:8080 -f $1


# e.g. ./run-headless.sh RegularUser 20 10
# --> spawn 20 regular users (with a spawn rate of 10 per second)

