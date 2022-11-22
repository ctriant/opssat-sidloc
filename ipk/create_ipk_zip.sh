#!/bin/bash

# ---- INIT

# app info
exp_id="exp202"
exp_name="opssat-sidloc"
exp_version=$(git describe)
exp_author="Libre Space Foundation"

if [ "$#" -ne 1 ]; then
    echo "Please pass as argument the location of the built NMF app folder"
    exit
fi

# get built NMF app
nmf_app_built=$1

cd ../ipk

# ---- Create required structure

# copy NMF app and it's configuration
cp -r "$nmf_app_built"/home .
cp ../nmf/space-app/conf/opssat-sidloc.properties home/"$exp_id"

# ---- ZIP

zip -r "$exp_name"_"$exp_version"_"$exp_author".zip home
rm -r home
