
FROM nginx:latest
ARG ML_PASSWORD

RUN apt update
RUN apt install -y sudo openssh-server sshpass python3 nodejs npm
RUN apt install -y sudo python3
RUN apt install -y sudo python3-pip
RUN pip install --upgrade pip
RUN useradd -rm -d /home/mlfilter -s /bin/bash -g root -G sudo -u 1001 mlfilter
RUN echo "mlfilter:$ML_PASSWORD" | chpasswd
RUN service ssh start

WORKDIR /app/
RUN npm i performance-now express

# COPY ./.env /app/
COPY ./scripts/start_nginx.sh /app/
COPY ./scripts/start_ab_component.sh /app/
COPY ./scripts/start_ml_component.sh /app/
COPY ./scripts/generate_nginx.sh /app/
COPY ./scripts/wait-for-it.sh /app/
COPY ./nginx_template.conf /app/
# copy the requirements file into the image
COPY ./requirements.txt /app/

# install the dependencies and packages in the requirements file
RUN pip install -r requirements.txt

COPY ./ML_server.py /app/
COPY ./adaptation_server.js /app/

SHELL ["/bin/bash", "-c"]
CMD ./start_ml_component.sh

EXPOSE 80
EXPOSE 22
# Expose port 5000 for the internal adaptation requests
EXPOSE 5000
EXPOSE 5001

# ssh-clean adaptation@$(docker-ip2 nginx-toplevel-service)

