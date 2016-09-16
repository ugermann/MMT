FROM ubuntu:16.04
MAINTAINER Ulrich Germann <ulrich.germann@gmail.com>

RUN apt-get update
RUN apt-get install -y software-properties-common
RUN apt-get install -y python-software-properties
RUN apt-get install -y libbz2-dev libboost-all-dev 
RUN apt-get install -y libgoogle-perftools-dev
RUN apt-get install -y libsparsehash-dev cmake
RUN apt-get install -y openjdk-8-jdk git maven
RUN apt-get install -y python-requests python-psutil 

RUN git clone http://github.com/ugermann/MMT -b tutorial-at-mt-marathon-2016
RUN cd MMT && ./util/full-recompile.sh

