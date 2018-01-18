#!/bin/bash

shellDir=$( cd "$(dirname "${BASH_SOURCE[0]}")" ; pwd -P )
calledFrom=$PWD
cd $shellDir/../frontend/
npm run build
rm -rf ../backend/out/production/static/
mv  build/{index.html,static/} ../backend/out/production/
cp -r index/ ../backend/out/production/static/
rm -rf build/
cd $calledFrom
