#!/bin/bash

shellDir=$( cd "$(dirname "${BASH_SOURCE[0]}")" ; pwd -P )
calledFrom=$PWD
cd $shellDir
gradle clean build
mkdir tmpFolder
mv build/libs/Bank.jar tmpFolder/
rm -rf build/
mv tmpFolder/Bank.jar ../build/
rm -rf tmpFolder/
cd $calledFrom
