#!/bin/bash
set -e
git submodule init
git submodule update
cd vendor && make res && make -j && make install
cd ../src
mvn clean install

