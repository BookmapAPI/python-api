# This Dockerfile is used to run the GitLab CI pipeline.
# If you change the below file, see here how to build and push the new image:
#     https://gitlab.dev.bookmap.com/help/user/packages/container_registry/index#build-and-push-images
#   but basically just run this in the root directory:
#     (1) docker build -t gitlab.dev.bookmap.com:5050/connectivity/developer-addon .
#     (2) docker push gitlab.dev.bookmap.com:5050/connectivity/developer-addon
#   The image is listed here after pushing it:
#     https://gitlab.dev.bookmap.com/connectivity/developer-addon/container_registry/131

FROM ubuntu:20.04

#to avoid tzdata configuring during JDK installation
ARG DEBIAN_FRONTEND=noninteractive

RUN apt-get update && apt-get install -y \
    curl \
    nodejs  \
    npm \
    openjdk-16-jdk \
  && rm -rf /var/lib/apt/lists/*

RUN npm install -g prettier@2.1.1 prettier-plugin-java@0.8.3
