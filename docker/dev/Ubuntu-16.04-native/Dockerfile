FROM ubuntu:16.04
MAINTAINER Ulrich Germann <ulrich.germann@gmail.com>

RUN apt-get update
RUN apt-get install -y software-properties-common
RUN apt-get install -y python-software-properties
RUN apt-get install -y build-essential cmake git maven libbz2-dev zlib1g-dev
RUN apt-get install -y libboost-all-dev libgoogle-perftools-dev
RUN apt-get install -y libsparsehash-dev openjdk-8-jdk 
RUN apt-get install -y python-requests python-psutil 

RUN git clone http://github.com/ugermann/MMT -b tutorial-at-mt-marathon-2016
WORKDIR /MMT/vendor
RUN make clean
RUN git submodule init
RUN git submodule update
RUN make res 
RUN make -j
RUN make install
WORKDIR /MMT/src
RUN mvn clean install


