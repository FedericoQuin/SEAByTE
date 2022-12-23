FROM python:3.12.0a3-bullseye
ARG ML_PASSWORD

RUN apt update
RUN apt install -y sudo openssh-server sshpass
RUN useradd -rm -d /home/mlfilter -s /bin/bash -g root -G sudo -u 1001 mlfilter
RUN echo "mlfilter:$ML_PASSWORD" | chpasswd
RUN service ssh start

WORKDIR /app/

COPY ./scripts/start_ml_component.sh /app/
# copy the requirements file into the image
COPY ./requirements.txt /app/

# install the dependencies and packages in the requirements file
RUN pip install -r requirements.txt

COPY ./ML_server.py /app/

RUN ["chmod", "+x", "/app/start_ml_component.sh"]

SHELL ["/bin/bash", "-c"]
CMD ./start_ml_component.sh

EXPOSE 80
EXPOSE 22
