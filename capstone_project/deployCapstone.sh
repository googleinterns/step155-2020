#!/bin/bash

chmod +x ./APIsub.sh
./APIsub.sh
mvn package appengine:deploy 
