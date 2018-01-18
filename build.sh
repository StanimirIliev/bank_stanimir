#!/bin/bash

shellDir=$( cd "$(dirname "${BASH_SOURCE[0]}")" ; pwd -P )
calledFrom=$PWD
cd $shellDir/backend
gradle clean build
cd ..
mkdir tmpFolder
mv backend/build/libs/Bank.jar tmpFolder/
rm -rf build/
mkdir build/
rm -rf backend/build/
mv tmpFolder/Bank.jar build/
rm -rf tmpFolder/
cd frontend/
npm run build
mv build/{index.html,static/} ../build/
cp -r index/ ../build/static
rm -rf build/
cd $calledFrom
