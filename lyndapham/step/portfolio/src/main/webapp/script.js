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

/**
 * Adds a random greeting to the page.
 */
function addFunFact() {
  const facts =
      [
        'I am the youngest of 6!',
        'I like to sing!',
        'I am Vietnamese-Chinese American.',
        'I have lived in the Greater Seattle Area my entire life.',
        'I am a Cancer (July birthday... can you guess the day?)',
        'I love to thrift.',
        'It\'s July 12th :)'
      ];

  // Pick a random greeting.
  const fact = facts[Math.floor(Math.random() * facts.length)];

  // Add it to the page.
  const factContainer = document.getElementById('fact-container');
  factContainer.innerText = fact;
}

function getJson() {
  fetch('/data').then(response => response.text()).then((quote) => {
    const jsonContainer = document.getElementById('json-container');
    jsonContainer.innerText = quote;
  });
}
