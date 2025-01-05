FROM clojure:tools-deps-bullseye
# FROM node:17-bullseye
# FROM debian:bullseye

RUN apt update
# RUN apt install -y git curl vim less

# # clojure
# RUN apt update
# RUN apt install -y rlwrap openjdk-17-jre
# RUN curl -O https://download.clojure.org/install/linux-install-1.10.3.1075.sh
# RUN bash ./linux-install-1.10.3.1075.sh

# nodejs
RUN apt install -y npm wget
RUN npm install -g n
RUN n stable
RUN hash -r
RUN npm install -g yarn

# RUN useradd 1000
# RUN mkdir /home/1000
# RUN chown -R 1000 /home/1000
# RUN usermod -a -G root 1000

# USER 1000
# USER node
# USER root
WORKDIR /usr/src/app/front
