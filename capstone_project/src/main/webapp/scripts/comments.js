/**
 * 
 */
function postComment() {
  const postText = document.getElementById('post-entry').value.trim();
  const userPosts = document.getElementById('user-posts');
  const imageURL = document.getElementById('image-preview').src;

  let newPost = document.createElement('div');
  if(imageURL) {
    newPost.innerHTML = `<img src="${imageURL}">`;
  }
  
  newPost.innerHTML += `<p>${postText}</p>`;

  userPosts.appendChild(newPost);
  document.getElementById('image-preview').src = ''
  document.getElementById('image-upload').value = '';
}
/**
 * Loads a preview of the image to be uploaded.
 * @param {Event} event  
 */
function previewImage(event) {
  console.log(typeof event);
  const preview = document.getElementById('image-preview');
  preview.src = URL.createObjectURL(event.target.files[0]);
}