FROM clojure:openjdk-18-bullseye
WORKDIR /usr/src/app/front

RUN apt update && apt install -y npm
RUN npm install -g npm
RUN npm install -g n
RUN n stable
RUN npm install -g yarn
