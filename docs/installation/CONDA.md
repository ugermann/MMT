# Installation in user space with conda

This page explains how to install MMT in user space in a conda environment. 

__You need to do this ONLY if your environment does not offer all the required
packages and versions, and you do not have root access or do not want to muck
with your system setup!__

1. Check if all necessary tools for conda installation are available.
   If they are not, ask your local system administrator to install them. These are
   standard packages that should be available on most systems. It may be necessary
   to run `apt-get update` before installing package `build-essential`.

   ```
   apt-get install -y wget bzip2
   apt-get update 
   apt-get install -y build-essential
   ```
1. Download and install the Miniconda installation script:

   ```
   wget https://repo.continuum.io/miniconda/Miniconda2-latest-Linux-x86_64.sh
   bash ./Miniconda2-latest-Linux-x86_64.sh
   ```

1. Add miniconda binary path to PATH
   ``` 
   echo "export PATH=${HOME}/miniconda2/bin:"${PATH} >> ${HOME}/.bashrc
   export PATH=${HOME}/miniconda2/bin:${PATH}
   ```

1. Set up a new conda environment:

   ```
   conda create --name mmt \
   	 -c https://conda.anaconda.org/cidermole \
    	 cmake gperftools python=2.7 requests jdk8 \
    	 apache-maven patchelf cloog git zlib boost
   ```

1. Activate the new environment and install a few more packages

   ```
   source activate mmt
   conda install -f gcc
   pip install psutil requests
   ```

1. We need to adjust `LD_LIBRARY_PATH` for compiling MMT:

   ```
   export LD_LIBRARY_PATH=${CONDA_HOME}/lib:${LD_LIBRARY_PATH}
   ```

1. At last we clone the MMT repo and compile. In this example, we clone the
   version for the MT Marathon 2016 lab sessopm from Ulrich Germann's repository.

   ```
   git clone http://github.com/ugermann/MMT -b tutorial-at-mt-marathon-2016
   cd MMT && ./util/full-recompile.sh

   ```






* note to self: full anaconda installation script is at
[https://repo.continuum.io/archive/Anaconda2-4.1.1-Linux-x86_64.sh]

