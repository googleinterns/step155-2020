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
