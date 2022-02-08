
source .env
export $(cut -d= -f1 .env)
cp -f nginx_template.conf /etc/nginx/nginx.conf
