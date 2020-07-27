#!/bin/bash

cp src/main/webapp/pages/mapsTemplate.html src/main/webapp/pages/maps.html

sed -i "s|mapsTemplate.html|maps.html|g" src/main/webapp/index.html
sed -i "s|CAPSTONE_API_KEY|$CAPSTONE_API_KEY|g" src/main/webapp/pages/maps.html
