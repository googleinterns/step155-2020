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
  fetch('/data').then(response => response.json()).then((allcomments) => {
    var i;
    for (i = 0; i < allcomments.length; i++) {
      var datetime = getDateTime();
      createCommentEntry(allcomments[i].name + ' said "' + allcomments[i].body + "' at " + datetime);
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
  var datetime = today.getFullYear() + '-' + (today.getMonth()+1) + '-' + today.getDate() + " " + today.getHours() + ":" + today.getMinutes() + ":" + today.getSeconds();
  return datetime;
}
