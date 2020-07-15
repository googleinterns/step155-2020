#!/bin/bash

cp src/main/webapp/placesTemplate.html src/main/webapp/places.html

sed -i "s|placesTemplate.html|places.html|g" src/main/webapp/index.html
sed -i "s|MY_API_KEY|$MY_API_KEY|g" src/main/webapp/places.html
