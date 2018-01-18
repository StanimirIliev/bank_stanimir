#!/bin/bash

shellDir=$( cd "$(dirname "${BASH_SOURCE[0]}")" ; pwd -P )
calledFrom=$PWD
cd $shellDir/backend
gradle clean build
cd ..
mkdir tmpFolder
mv backend/build/libs/Bank.jar tmpFolder/
rm -rf build/*
rm -rf backend/build/
mv tmpFolder/Bank.jar build/
rm -rf tmpFolder/
cd frontend/
cp -r index/ ../build/static
npm run build
mv build/{index.html,static/} ../build/
rm -rf build/
cd $calledFrom
