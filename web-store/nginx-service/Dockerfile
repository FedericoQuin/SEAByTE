FROM nginx:latest


RUN apt update
RUN apt install -y sudo openssh-server sshpass python3
RUN useradd -rm -d /home/adaptation -s /bin/bash -g root -G sudo -u 1001 adaptation
RUN echo "adaptation:debug" | chpasswd
# For debugging
# RUN service ssh start

COPY ./.env /app/
COPY ./scripts/start_nginx.sh /app/
COPY ./scripts/generate_nginx.sh /app/
COPY ./nginx-service/nginx_template.conf /app/
COPY ./scripts/wait-for-it.sh /app/
# COPY ./scripts/wait-for-it-multiple.sh /app/

WORKDIR /app/
SHELL ["/bin/bash", "-c"]

# Temporarily use the following until multiple hosts script works
CMD ./wait-for-it.sh -t 60 ws-webapp-service:80 -- \
	./wait-for-it.sh -t 10 ws-registration-service:80 -- \
	./wait-for-it.sh -t 10 ws-inventory-service:80 -- \
	./wait-for-it.sh -t 10 ws-login-service:80 -- \
	./wait-for-it.sh -t 10 ws-pricing-service:80 -- \
	./wait-for-it.sh -t 10 ws-user-service:80 -- \
	./wait-for-it.sh -t 10 ws-basket-service:80 -- \
	./wait-for-it.sh -t 10 ws-basket-service:80 -- \
	./wait-for-it.sh -t 10 ws-checkout-service:80 -- \
	./wait-for-it.sh -t 10 ws-history-service:80 -- \
	./wait-for-it.sh -t 10 ws-recommendation-service:80 -- \
	./start_nginx.sh

EXPOSE 80
# EXPOSE 22
