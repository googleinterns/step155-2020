// Copyright 2020 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the 'License');
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an 'AS IS' BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

/**
 * Loads a preview of the image to be uploaded.
 * @param {Event} event current state of the image tag
 */
function previewImage(event) { // eslint-disable-line no-unused-vars
  const preview = document.getElementById('image-preview');
  preview.src = URL.createObjectURL(event.target.files[0]);
}

/** Retrieves all posts from the server and adds them to the DOM. */
async function loadPosts() { // eslint-disable-line no-unused-vars
  const posts =
    await fetch('/post-process')
        .then((response) => response.json())
        .then((json) => json);

  renderPosts(posts);
}

/**
 * Increases the upvote count of a post locally and server side.
 * @param {HTMLButtonElement} upvoteBtn the button element of the post to
 *                                      upvote.
 */
async function upvotePost(upvoteBtn) { // eslint-disable-line no-unused-vars
  const interactionsBar = upvoteBtn.parentElement;
  const id = interactionsBar.parentElement.getAttribute('data-id');
  const postContainer = document.getElementById('user-posts');
  const sortedBy = postContainer.getAttribute('data-sort');

  // Makes the POST request to get the new upvote count on the serverside to
  // later update it on the client side.
  const newUpvoteCount = await fetch('/upvote', {
    headers: {
      'Content-Type': 'application/x-www-form-urlencoded',
    },
    method: 'POST',
    body: `id=${id}&sort-type=${sortedBy}`,
  }).then((response) => response.json());

  if (newUpvoteCount != -1) {
    interactionsBar.innerHTML = `${upvoteBtn.outerHTML} ${newUpvoteCount}`;
  }
}

/**
 * Sends a post request to the server to sort the posts by the given sort type.
 * Then, it displays them on the page.
 * @param {String} sortType the name of the sorting method to use.
 */
async function sortPosts(sortType) { // eslint-disable-line no-unused-vars
  const sortedPosts = await fetch('/sort-posts', {
    headers: {
      'Content-Type': 'application/x-www-form-urlencoded',
    },
    method: 'POST',
    body: `sort-type=${sortType}`,
  }).then((response) => response.json());

  const postContainer = document.getElementById('user-posts');
  postContainer.setAttribute('data-sort', sortType);
  postContainer.innerHTML = '';
  renderPosts(sortedPosts);
}

/**
 * Renders the posts onto the page.
 * @param {Array<Entity>} posts an array of the post entities to render.
 */
function renderPosts(posts) {
  const postContainer = document.getElementById('user-posts');

  for (const post of posts) {
    const postProperties = post.propertyMap;

    const HTML = `
      ${postProperties.imageURL ?
        `<img class='post-image' src='${postProperties.imageURL}'>` : ''}
      <p>${postProperties.text}</p>
      <div class='interactions'>
        <button class='upvote-button'
                onclick='upvotePost(this)'>
            <i class='upvote'></i></button> ${postProperties.upvotes} 
      </div>
    `.trim();

    const postElement = document.createElement('div');
    postElement.setAttribute('class', 'post-container');
    postElement.setAttribute('data-id', post.key.id);
    postElement.innerHTML = HTML;
    postContainer.appendChild(postElement);
  }
}
