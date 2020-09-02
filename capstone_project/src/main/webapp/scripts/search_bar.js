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
 * Sends a search query to the server and retrieves a list of all posts that
 * contain the search query.
 */
async function searchPosts() { // eslint-disable-line no-unused-vars
  // Only run when the user presses enter.
  if (event.keyCode !== 13) {
    return;
  }

  await resetSearchResults();
  const currentQuery = document.getElementById('post-search-bar').value;
  const postsFound =
      await fetch(`/filter-posts?search=${encodeURIComponent(currentQuery)}`)
          .then((res) => res.json());
  await loadPosts(postsFound);
}

/** Removes all current posts to allow the new posts to be loaded. */
async function resetSearchResults() {
  Array.from(document.querySelectorAll('div[data-school]'))
      .forEach((elem) => elem.remove());
}
