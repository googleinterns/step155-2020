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
 * Loads a preview of the file to be uploaded and determines the proper file
 * type.
 * @param {Event} event - current state of the image tag
 */
function previewImage(event) { // eslint-disable-line no-unused-vars
  const preview = document.getElementById('image-preview');
  const file = event.target.files[0];
  preview.src = URL.createObjectURL(file);
  const fileType = getInputFileType(file);
  fetchBlobstoreURL(fileType);
}

/**
 * Determines whether the uploaded file is a video, image, or neither.
 * Returns a string representation of the file type name.
 * @param {File} file - the file to be uploaded.
 * @return {String} fileType - a string represention of the file type.
 */
function getInputFileType(file) {
  if (file.type.match('video/.*')) {
    return 'video';
  }

  if (file.type.match('image/.*')) {
    return 'image';
  }

  return 'none';
}

/**
 * Fetchs a url for the file to upload with the appropriate file type
 * parameter.
 * @param {String} fileType - the file should be 'image' | 'video' | 'none'
 */
function fetchBlobstoreURL(fileType) {
  const form = document.getElementById('post-input');
  fetch(`/fetch-blobstore-url?file-type=${fileType}`)
      .then((response) => response.text())
      .then((url) => {
        form.action = url;
      });
}

/** Retrieves all posts from the server and adds them to the DOM. */
async function loadPosts() { // eslint-disable-line no-unused-vars
  const posts =
    await fetch('/post-process')
        .then((response) => response.json())
        .then((json) => json);

  await renderPosts(posts);
}

/**
 * Retrieves all schools from datastore and adds them to a selectable dropdown
 * menu.
 */
async function loadSchools() { // eslint-disable-line no-unused-vars
  const schools = await fetch('/school-data')
      .then((response) => response.json());
  const schoolsList = document.getElementById('schools');

  let schoolsHTML = '';
  for (const school of schools) {
    schoolsHTML += `<option value='${school.name}'>${school.name}</option>`;
  }
  schoolsList.innerHTML += schoolsHTML;
  const selectElems = document.querySelectorAll('select');
  M.FormSelect.init(selectElems);
}

/**
 * Increases the upvote count of a post locally and server side.
 * @param {HTMLButtonElement} upvoteBtn - the button element of the post to
 *                                        upvote.
 */
async function upvotePost(upvoteBtn) { // eslint-disable-line no-unused-vars
  const interactionsBar = upvoteBtn.parentElement;
  const id = interactionsBar.parentElement.getAttribute('data-id');

  // Makes the POST request to get the new upvote count on the serverside to
  // later update it on the client side.
  const newUpvoteCount = await fetch('/upvote', {
    headers: {
      'Content-Type': 'application/x-www-form-urlencoded',
    },
    method: 'POST',
    body: `id=${id}`,
  }).then((response) => response.json());

  if (newUpvoteCount.length !== 0) {
    interactionsBar.innerHTML =
      `${upvoteBtn.outerHTML} ${newUpvoteCount} ${ await getReactionsHTML(id)}`;
  }
}

/**
 * Sends a post request to the server to sort the posts by the given sort type.
 * Then, it displays them on the page.
 * @param {String} sortType - the name of the sorting method to use.
 */
async function sortPosts(sortType) { // eslint-disable-line no-unused-vars
  const postContainer = document.getElementById('user-posts');
  postContainer.setAttribute('data-sort', sortType);
  postContainer.innerHTML = '<h1>Loading...</h1>';

  const sortedPosts = await fetch('/sort-posts', {
    headers: {
      'Content-Type': 'application/x-www-form-urlencoded',
    },
    method: 'POST',
    body: `sort-type=${sortType}`,
  }).then((response) => response.json());

  postContainer.innerHTML = '';
  renderPosts(sortedPosts);
}

/**
 * Renders the posts onto the page.
 * @param {Array<Entity>} posts - an array of the post entities to render.
 */
async function renderPosts(posts) {
  const postContainer = document.getElementById('user-posts');

  for (const post of posts) {
    const postElement = await createPost(post);
    postContainer.appendChild(postElement);
  }
}

/**
 * Returns an HTML Div Element representing the post.
 * @param {Entity} post - an entity representing the post.
 */
async function createPost(post) {
  const postProperties = post.propertyMap;
  const postHTML = `
    <div class="post-card-title">${postProperties.title}</div>
    ${getProperFileTag(postProperties.fileType, postProperties.fileBlobKey)}
    <div class='card-content'>${postProperties.text.value.value}</div>
    <div class='card-action interactions'>
      <button class='btn-flat upvote-button'
              onclick='upvotePost(this)'>
      <p class='upvote'></p></button> ${postProperties.upvotes}
      ${await getReactionsHTML(post.key.id)}
    </div>
      `.trim();

  const postElement = document.createElement('div');
  postElement.setAttribute('class', 'post-container card');
  postElement.setAttribute('data-id', post.key.id);
  postElement.innerHTML = postHTML;
  return postElement;
}

/**
 * Loads a single post. Must be called from a url containing the 'post-id'
 * parameter.
 */
async function loadSinglePost() { // eslint-disable-line no-unused-vars
  const postUrl = new URL(location.href);
  const postId = postUrl.searchParams.get('post-id');
  const post = await fetch(`/serve-post?post-id=${postId}`)
      .then((response) => response.json());
  const postElement = await createPost(post);
  const postContainer = document.getElementById('post');
  postContainer.appendChild(postElement);
}

/**
 * Returns an HTML string representation of all reactions for a post.
 * @param {number} postID - the unique id of the post
 */
async function getReactionsHTML(postID) {
  const reactionsCount = await getReactionCounts(postID);
  const reactionsHTML = `
    <a class='btn-floating btn-small reaction-btn'>
      <div class='reaction-box'>
        <div class='reaction-icon emoji-think'
             data-reaction='think'
             onclick='reactToPost(this)'>
          <label class='reaction-label'>Think</label>
          <span class='reaction-counter'>${reactionsCount.think}</span>
        </div>
        <div class='reaction-icon emoji-wow'
             data-reaction='wow'
             onclick='reactToPost(this)'>
          <label class='reaction-label'>Wow</label>
          <span class='reaction-counter'>${reactionsCount.wow}</span>
        </div>
        <div class='reaction-icon emoji-love'
             data-reaction='love'
             onclick='reactToPost(this)'>
          <label class='reaction-label'>Love</label>
          <span class='reaction-counter'>${reactionsCount.love}</span>
        </div>
        <div class='reaction-icon emoji-sad'
             data-reaction='sad'
             onclick='reactToPost(this)'>
          <label class='reaction-label'>Sad</label>
          <span class='reaction-counter'>${reactionsCount.sad}</span>
        </div>
        <div class='reaction-icon emoji-laugh'
             data-reaction='laugh'
             onclick='reactToPost(this)'>
          <label class='reaction-label'>Laugh</label>
          <span class='reaction-counter'>${reactionsCount.laugh}</span>
        </div>
        <div class='reaction-icon emoji-yikes'
             data-reaction='yikes'
             onclick='reactToPost(this)'>
          <label class='reaction-label'>Yikes</label>
          <span class='reaction-counter'>${reactionsCount.yikes}</span>
        </div>
      </div>
    </a>
  `.trim();
  return reactionsHTML;
}

/**
 * Determings the proper tag to use depending on whether the file
 * is an image, a video or neither. Returns the string represenation
 * of that tag.
 * @param {String} fileType - the type of file to render
 * @param {String} fileBlobKey - the files blobkey string
 * @return {String} fileTag -  a string representation of the html tag for the
 *                             file
 */
function getProperFileTag(fileType, fileBlobKey) {
  let fileTag = '';
  const src = `/serve-file?blob-key=${fileBlobKey}`;
  if (fileType === 'video') {
    fileTag = `
      <video class='post-file' controls>
        <source src='${src}'>
      </video>
    `.trim();
  } else if (fileType === 'image') {
    fileTag = `
    <div class='card-image'>
      <img src='${src}'>
    </div>
    `.trim();
  }

  return fileTag;
}

/**
 * Disables the submit button to prevent multiple uploads while upload is
 * processing.
 * @param {Event} event - current state of the input button
 */
function disableInput(event) { // eslint-disable-line no-unused-vars
  // the submit is the last element in the form.
  const inputButton = event.target.lastElementChild;
  inputButton.disabled = true;
}

/**
 * Increments the count of the chosen reaction locally and on the server side.
 * @param {HTMLDivElement} element - the requested reaction to send.
 */
async function reactToPost(element) { // eslint-disable-line no-unused-vars
  const postID = element.closest('.post-container[data-id]').dataset.id;
  const reaction = element.dataset.reaction;
  if (reaction === undefined || reaction === '') {
    return;
  }

  const response = await fetch('/post-react', {
    headers: {
      'Content-Type': 'application/x-www-form-urlencoded',
    },
    method: 'POST',
    body: `reaction=${reaction}&post-id=${postID}`,
  });
  const newCount = await response.json();
  if (newCount.length !== 0) {
    element.querySelector('span.reaction-counter').innerText = newCount;
  }
}

/**
 * Given a post id, retrieves a map of the reactions of that post and returns
 * that map as a JavaScript object.
 * @param {number} postID - the unique id of the post
 * @return {Object} reactions - return an object of reactions with each
 *                              reactions count.
 */
async function getReactionCounts(postID) {
  const reactionRequest = await fetch(`/post-react?post-id=${postID}`, {
    headers: {
      'Accept': 'application/json',
    },
  });
  const reactionPropertyMap = await reactionRequest.json();
  return reactionPropertyMap.propertyMap;
}
