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
 * Adds a random fun fact to the page.
 */
function addRandomFact() {
  const funFacts = [
    "I've built 8 computers so far!",
    "I interned at Google last summer.",
    "I spent my last summer in New York City.",
    "I speak Spanish.",
    "I love broccoli.",
    "I can solve a Rubik's Cube."
  ];

  // Pick a random greeting.
  let funFact = funFacts[Math.floor(Math.random() * funFacts.length)];

  // Add it to the page.
  const funFactContainer = document.getElementById('fun-fact-container');
  while (funFactContainer.innerText == funFact) {
    funFact = funFacts[Math.floor(Math.random() * funFacts.length)];
  }
  funFactContainer.innerText = funFact;
  funFactContainer.style.backgroundColor = "whitesmoke";
}

function getComments() {
  // const commentContainer = document.getElementById('comment-container');
  fetch("/data").then(response => response.json()).then((comments) => {
    console.log(comments);
    comments.forEach(addComment);
  });
}

function addComment(comment) {
    const commentContainer = document.getElementById('comment-container');
    const node = document.createElement('li');
    const textNode = document.createTextNode(comment.text);
    node.appendChild(textNode);
    commentContainer.appendChild(node);
}