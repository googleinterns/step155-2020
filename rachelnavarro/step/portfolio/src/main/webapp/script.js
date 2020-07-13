// Copyright 2019 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

/** Simulates drawing a tarot card from a deck. */
function drawCardOfTheDay() {
  const deck =
      ['The Fool', 'The Magician', 'The High Priestess', 'The Empress'];

  // Draw a random card.
  const card = deck[Math.floor(Math.random() * deck.length)];

  // Add it to the page.
  const tarotDeck = document.getElementById('tarot-reading');
  tarotDeck.innerText = card;
}

/** Fetches and loads all previous comments in an unordered list. */
function displayComments() {
  fetch('/data').then(response => response.json()).then((allComments) => {
    var i;
    for (i = 0; i < allComments.length; i++) {
      var datetime = getDateTime();
      var commentString = `${allComments[i].name} said "${allComments[i].body}" at ${datetime}`;
      createCommentEntry(commentString);
    }
  });
}

/** Creates an <li> element containing the string "[user] said "[comment]" at [datetime]". */
function createCommentEntry(text) {
  var node = document.createElement("LI");
  var textnode = document.createTextNode(text);
  node.appendChild(textnode);
  document.getElementById("comments-container").appendChild(node);
}

/** Gets date/time in YYYY-MM-DD HH:MM:SS format. */
function getDateTime() {
  var today = new Date();
  var datetime = `${today.getFullYear()}-${today.getMonth()+1}-${today.getDate()} ${today.getHours()}:${today.getMinutes()}:${today.getSeconds()}`;
  return datetime;
}

/** Creates and loads maps for all the locations featured in places.html. 
 * TODO: fetch this info from a java servlet instead of hardcoding */
function loadMaps() {
  var placesMap = new Map();
  placesMap.set("nyc", [40.704381, -73.990588]); // DUMBO Waterfront- Brooklyn, New York
  placesMap.set("tokyo", [35.685028, 139.709880]); // Shinjuku Gyoen National Garden- Tokyo, Japan
  placesMap.set("la", [33.983814, -118.467551]); // Venice Beach Canals- Los Angeles, California
  placesMap.set("niagara", [43.079886, -79.075159]); // Niagara Falls- Ontario, Canada
  placesMap.set("santorini", [36.458416, 25.371504]); // Santorini- Ia, Greece
  placesMap.set("singapore", [1.281364, 103.863677]); // Gardens by the Bay- Marina Bay, Singapore
  placesMap.set("hawaii", [21.263983, -157.805827]); // Diamond Head Trail- Honolulu, Hawaii

  for (const [key, value] of placesMap.entries()) {
    createMap(value[0], value[1], key);
  }

}

/** Creates a map of a location and adds it to the page. */
function createMap(latitude, longitude, name) {
  console.log("creating " + name);
  const map = new google.maps.Map(
      document.getElementById(name),
      {center: {lat: latitude, lng: longitude}, zoom: 15});
}
