#!/bin/bash

shellDir=$( cd "$(dirname "${BASH_SOURCE[0]}")" ; pwd -P )
calledFrom=$PWD
cd $shellDir
npm run build
rm -rf ../build/static/
mv build/{index.html,static/} ../build/
rm -rf build/
cd $calledFrom
