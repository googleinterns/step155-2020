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
function addRandomGreeting() {
  const greetings =
      ['Hello world!', '¡Hola Mundo!', '你好，世界！', 'Bonjour le monde!'];

  // Pick a random greeting.
  const greeting = greetings[Math.floor(Math.random() * greetings.length)];

  // Add it to the page.
  const greetingContainer = document.getElementById('greeting-container');
  greetingContainer.innerText = greeting;
}

/**
 * Adds a random quote to the page
 */
function getRandomQuote() {
  const quotes = ["It is what it is.", "With great power, comes great responsibility.", "What once was once is now"];

  const quote = quotes[Math.floor(Math.random() * quotes.length)];

  const quoteContainer = document.getElementById('quote-container');
  quoteContainer.innerText = quote;
}

function getComments() {
  const responsePromise = fetch("/data");
  const commentContainer = document.getElementById("comments-container");

  responsePromise.then((response) => response.json()).then((comments) => {
    for (let commentInformation of comments) {
      commentContainer.appendChild(createComment(commentInformation));
    }
  });
}

function createComment(commentInformation) {
  const commentBox = document.createElement("div");
  const commentText = document.createElement("p");
  const barrier = document.createElement("hr");
  const userInformationElement = document.createElement("p");

  commentBox.setAttribute("class", "comment-content-box");
  userInformationElement.setAttribute("class", "user-information");

  commentText.innerText = commentInformation.comment;
  const readableDate = new Date(commentInformation.timestamp).toLocaleString();
  userInformationElement.innerText = commentInformation.name + " on " + readableDate;

  commentBox.appendChild(commentText);
  commentBox.appendChild(barrier);
  commentBox.appendChild(userInformationElement);

  return commentBox;
}
