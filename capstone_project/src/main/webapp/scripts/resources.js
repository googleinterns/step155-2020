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

/** Displays user submissions of resources */
function displaySubmissions() { // eslint-disable-line no-unused-vars
  fetch('/get-resources').then(response => response.json()).then((submissions) => {
    const submissionContainer = document.getElementById("submissions-container");
    for (const submission of submissions) {
        const resourceSubmission = createResourceSubmission(submission);
        submissionContainer.appendChild(resourceSubmission);
    }
  });
}

/** Creates an <li> element */
function createResourceSubmission(resourceSubmission) { // eslint-disable-line no-unused-vars
  const submissionMap = resourceSubmission.propertyMap;
  var node = document.createElement('li');
  const text = `
  <a href="${submissionMap.resourceURL}">${submissionMap.resourceName}</a>
   (${submissionMap.category})
  `.trim();
  node.innerHTML = text;
  return node;
}
