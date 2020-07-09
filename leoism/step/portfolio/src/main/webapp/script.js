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
  const quotes = [
    'It is what it is.', 'With great power, comes great responsibility.',
    'What once was once is now'
  ];

  const quote = quotes[Math.floor(Math.random() * quotes.length)];

  const quoteContainer = document.getElementById('quote-container');
  quoteContainer.innerText = quote;
}

function createComment(commentInformation) {
  const commentBox = document.createElement('div');
  const commentText = document.createElement('p');
  const barrier = document.createElement('hr');
  const userInformationElement = document.createElement('p');

  commentBox.setAttribute('class', 'comment-content-box');
  userInformationElement.setAttribute('class', 'user-information');

  commentText.innerText = commentInformation.comment;
  const readableDate = new Date(commentInformation.timestamp).toLocaleString();
  userInformationElement.innerText =
      commentInformation.name + ' on ' + readableDate;

  commentBox.appendChild(commentText);
  commentBox.appendChild(barrier);
  commentBox.appendChild(userInformationElement);

  return commentBox;
}

function loadComments() {
  const responsePromise = fetch('/data');
  const commentsContainer = document.getElementById('comments-container');
  const amtCurrentlyDisplayed =
      parseInt(commentsContainer.getAttribute('data-display'));

  const nextTenComments = amtCurrentlyDisplayed + 10;

  // Remains true if the next iteration of 10 comments included 10 comments.
  let nextTenCommentsLoaded = true;
  responsePromise.then((response) => response.json()).then((comments) => {
    for (let i = amtCurrentlyDisplayed; i < nextTenComments; i++) {
      // Make load more button not appear when all comments have been loaded.
      if (i === comments.length - 1) {
        const loadMoreButton = document.getElementById('load-more-button');
        loadMoreButton.style.display = 'none';
      }


      // comments[i] is undefined when this iteration of 10 comments falls
      // short. For example, the list had 6 comments not the full 10.
      if (comments[i] === undefined) {
        commentsContainer.setAttribute('data-display', i);
        nextTenCommentsLoaded = false;
        break;
      }

      commentsContainer.appendChild(createComment(comments[i]));
    }

    if (nextTenCommentsLoaded) {
      commentsContainer.setAttribute('data-display', nextTenComments);
    }
  });
}

function selectAll() {
  const commentsTable = document.getElementById('comments-table');
  const areAllSelected =
      commentsTable.getAttribute('data-all-selected') == 'true';
  const allInputBoxes = commentsTable.querySelectorAll('tbody>tr>td>input');

  allInputBoxes.forEach((input) => {
    input.checked = !areAllSelected;
  });

  commentsTable.setAttribute('data-all-selected', !areAllSelected);
}

function submitButton() {
  const url = '/admin';
  const commentKeys = createParameters();
  const xhr = new XMLHttpRequest();
  xhr.open('POST', url, true);

  if (commentKeys.length === 0) {
    alert('There are no comments to delete!');
    return;
  }

  xhr.setRequestHeader('Content-type', 'application/json');

  xhr.send(JSON.stringify(commentKeys));
  alert('Refresh the Page to see the changes!');
}

function createParameters() {
  let commentKeys = [];

  const table = document.getElementById('comments-table')
  const commentKeyIdx = 6;

  for (let i = 1, row; row = table.rows[i]; i++) {
    const inputBox = row.cells[0].firstElementChild;
    const commentKeyBox = row.cells[commentKeyIdx];
    if (inputBox && inputBox.checked && commentKeyBox) {
      commentKeys.push(commentKeyBox.innerText);
    }
  }

  return commentKeys;
}
