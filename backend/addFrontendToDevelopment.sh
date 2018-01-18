#!/bin/bash

shellDir=$( cd "$(dirname "${BASH_SOURCE[0]}")" ; pwd -P )
calledFrom=$PWD
cd $shellDir/../frontend/
cp -r index/ ../backend/out/production/static
npm run build
rm -rf ../backend/out/production/static/
mv  build/{index.html,static/} ../backend/out/production/
rm -rf build/
cd $calledFrom
