FROM node:17
WORKDIR /usr/src/app/front

RUN apt update && apt install -y rlwrap openjdk-17-jre
RUN curl -O https://download.clojure.org/install/linux-install-1.10.3.1075.sh
RUN bash ./linux-install-1.10.3.1075.sh
