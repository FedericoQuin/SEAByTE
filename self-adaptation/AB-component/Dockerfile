FROM nginx:latest
ARG AB_PASSWORD

RUN apt update
RUN apt install -y sudo openssh-server sshpass python3 nodejs npm
RUN useradd -rm -d /home/adaptation -s /bin/bash -g root -G sudo -u 1001 adaptation
RUN echo "adaptation:$AB_PASSWORD" | chpasswd
RUN service ssh start

WORKDIR /app/
RUN npm i performance-now express node-fetch@2

# COPY ./.env /app/
COPY ./scripts/start_nginx.sh /app/
COPY ./scripts/start_ab_component.sh /app/
COPY ./scripts/generate_nginx.sh /app/
COPY ./scripts/wait-for-it.sh /app/
COPY ./nginx_template.conf /app/
COPY ./adaptation_server.js /app/

RUN ["chmod", "+x", "/app/start_nginx.sh", "/app/start_ab_component.sh", "/app/generate_nginx.sh", "/app/wait-for-it.sh"]

SHELL ["/bin/bash", "-c"]
CMD ./start_ab_component.sh

EXPOSE 80
EXPOSE 22
# Expose port 5000 for the internal adaptation requests
EXPOSE 5000

# ssh-clean adaptation@$(docker-ip2 nginx-toplevel-service)

