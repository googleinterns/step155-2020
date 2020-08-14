#!/bin/bash

cp src/main/webapp/pages/mapsTemplate.html src/main/webapp/pages/maps.html
cp src/main/webapp/scripts/mapsScriptTemplate.js src/main/webapp/scripts/mapsScript.js

sed -i "s|mapsTemplate.html|maps.html|g" src/main/webapp/index.html
sed -i "s|CAPSTONE_API_KEY|$CAPSTONE_API_KEY|g" src/main/webapp/pages/maps.html

sed -i "s|mapsScriptTemplate.js|mapsScript.js|g" src/main/webapp/pages/maps.html

sed -i "s|CAPSTONE_API_KEY|$CAPSTONE_API_KEY|g" src/main/webapp/scripts/mapsScript.js
sed -i "s|CAPSTONE_SEARCH_ENG_ID|$CAPSTONE_SEARCH_ENG_ID|g" src/main/webapp/scripts/mapsScript.js
