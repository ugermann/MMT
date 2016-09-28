FROM ubuntu:12.04
MAINTAINER Ulrich Germann <ulrich.germann@gmail.com>

# The primary purpose of this Dockerfile is to test installation with
# conda on legacy systems that do not meet the prerequisites for MMT
# installation and where the user has no root access.

# use bash, not dash
RUN rm /bin/sh && ln -s /bin/bash /bin/sh

# Install packages necessary to install miniconda
RUN apt-get update
RUN apt-get install -y wget bzip2 build-essential 

# Download Miniconda installer; install
RUN wget --no-check-certificate -q \
    https://repo.continuum.io/miniconda/Miniconda2-latest-Linux-x86_64.sh 
RUN bash ./Miniconda2-latest-Linux-x86_64.sh -b
RUN echo "export PATH=/root/miniconda2/bin:${PATH}" >> ~/.bashrc
RUN . ~/.bashrc
ENV PATH=/root/miniconda2/bin:${PATH}
RUN export PATH=${PATH}

# set up conda environment
RUN conda create --name mmt \
    -c https://conda.anaconda.org/cidermole \
    cmake boost zlib gperftools python=2.7 requests jdk8 apache-maven patchelf

RUN apt-get install -y git
RUN source activate mmt
RUN conda install -y cloog 
RUN conda install -y -f gcc
RUN git clone http://github.com/ugermann/MMT -b tutorial-at-mt-marathon-2016
WORKDIR MMT
RUN ./util/full-recompile.sh
# RUN ./mmt create en it examples/data/train
# RUN ./mmt start
# RUN echo "This is a test." | ./util/mmt.demo-client.py
