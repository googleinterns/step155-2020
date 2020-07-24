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

/** Adds the user post to the DOM. */
function postComment() {
  const postText = document.getElementById('post-entry').value.trim();
  const userPosts = document.getElementById('user-posts');
  const imageURL = document.getElementById('image-preview').src;

  const newPost = document.createElement('div');
  if (imageURL) {
    newPost.innerHTML = `<img src="${imageURL}">`;
  }

  newPost.innerHTML += `<p>${postText}</p>`;

  userPosts.appendChild(newPost);
  document.getElementById('image-preview').src = '';
  document.getElementById('image-upload').value = '';
}

/**
 * Loads a preview of the image to be uploaded.
 * @param {Event} event current state of the image tag
 */
function previewImage(event) {
  const preview = document.getElementById('image-preview');
  preview.src = URL.createObjectURL(event.target.files[0]);
}
