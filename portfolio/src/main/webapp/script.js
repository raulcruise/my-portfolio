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
    `I've built 8 computers so far!`,
    'I interned at Google last summer.',
    'I spent my last summer in New York City.',
    'I speak Spanish.',
    'I love broccoli.',
    `I can solve a Rubik's Cube.`
  ];

  // Pick a random greeting.
  let funFact = funFacts[Math.floor(Math.random() * funFacts.length)];

  // Add it to the page.
  const funFactContainer = document.getElementById('fun-fact-container');
  while (funFactContainer.innerText == funFact) {
    funFact = funFacts[Math.floor(Math.random() * funFacts.length)];
  }
  funFactContainer.innerText = funFact;
  funFactContainer.style.backgroundColor = 'whitesmoke';
}

function getComments() {
  const selectElement = document.getElementById('limit');
  const commentLimit = selectElement.options[selectElement.selectedIndex].value;
  fetch('/data?limit=' + commentLimit).then(response => response.json()).then((comments) => {
    clearComments();
    comments.forEach(addComment);
  });
}

function addComment(comment, index) {
  const commentContainer = document.getElementById('comment-container');
  const node = document.createElement('li');
  node.setAttribute('class', 'comment-node');
  node.setAttribute('id', 'comment' + index);
  const textNode = document.createTextNode(comment.text);
  node.appendChild(textNode);
  commentContainer.appendChild(node);
}

function clearComments() {
  const commentContainer = document.getElementById('comment-container');
  commentContainer.innerHTML = '';
}

function deleteComments() {
  fetch('/delete-data', {method: 'POST'}).then(() => getComments());
}

function createMap() {
  const map = new google.maps.Map(
      document.getElementById('map'), {
        center: {lat: 33.8366, lng: -117.9143},
        zoom: 10,
        styles: [
          {elementType: 'geometry', stylers: [{color: '#292929'}]},
          {elementType: 'labels.text.stroke', stylers: [{color: '#242f3e'}]},
          {elementType: 'labels.text.fill', stylers: [{color: '#ff6347'}]},
          {
            featureType: 'administrative.locality',
            elementType: 'labels.text.fill',
            stylers: [{color: '#d59563'}]
          },
          {
            featureType: 'poi',
            elementType: 'labels.text.fill',
            stylers: [{color: '#087E8B'}]
          },
          {
            featureType: 'poi.park',
            elementType: 'geometry',
            stylers: [{color: '#263c3f'}]
          },
          {
            featureType: 'poi.park',
            elementType: 'labels.text.fill',
            stylers: [{color: '#6b9a76'}]
          },
          {
            featureType: 'road',
            elementType: 'geometry',
            stylers: [{color: '#38414e'}]
          },
          {
            featureType: 'road',
            elementType: 'geometry.stroke',
            stylers: [{color: '#212a37'}]
          },
          {
            featureType: 'road',
            elementType: 'labels.text.fill',
            stylers: [{color: '#9ca5b3'}]
          },
          {
            featureType: 'road.highway',
            elementType: 'geometry',
            stylers: [{color: '#616163'}]
          },
          {
            featureType: 'road.highway',
            elementType: 'geometry.stroke',
            stylers: [{color: '#1f2835'}]
          },
          {
            featureType: 'road.highway',
            elementType: 'labels.text.fill',
            stylers: [{color: '#f3d19c'}]
          },
          {
            featureType: 'transit',
            elementType: 'geometry',
            stylers: [{color: '#2f3948'}]
          },
          {
            featureType: 'transit.station',
            elementType: 'labels.text.fill',
            stylers: [{color: '#d59563'}]
          },
          {
            featureType: 'water',
            elementType: 'geometry',
            stylers: [{color: '#17263c'}]
          },
          {
            featureType: 'water',
            elementType: 'labels.text.fill',
            stylers: [{color: '#515c6d'}]
          },
          {
            featureType: 'water',
            elementType: 'labels.text.stroke',
            stylers: [{color: '#17263c'}]
          }
        ]
      });

  const shabuContentString = '<div id="shabu-content">'+
    '<h1 class="firstHeading">House of Shabu Shabu II</h1>'+
    '<div>'+
      '<p><b>House of <a href="https://en.wikipedia.org/wiki/Shabu-shabu">Shabu Shabu</a> II</b> is a great place to  ' +
      'go to with a friend or a special someone, especially if you are into  '+
      'the interactive experience of <a href="https://en.wikipedia.org/wiki/Korean_barbecue">Korean barbecue</a>.'+
      'The experience at House of Shabu Shabu II is similar in that '+
      'you choose from a selection of menus which include a multitude '+
      'of meats and vegetables and can order as much as you want! </p> '+
      '</div>'+
      '</div>';

  const boilingContentString = '<div id="boiling-content">'+
    '<h1 class="firstHeading">Boiling Point</h1>'+
    '<div>'+
      '<p><b>Boiling Point</b> is an establishment that specializes in <a href="https://en.wikipedia.org/wiki/Hot_pot">Taiwanese hot soup</a> cuisine. ' +
      'I fell in love with this restaurant simply because of the delicious flavors that I '+
      'experienced here, and the fact that you get to eat this hot soup'+
      'while it is sitting over a flame so it stay nice and hot. '+
      'I recommend ordering a green tea to balance out the heat '+
      'with a splash of freshness and sweetness. </p> '+
      '</div>'+
      '</div>';

  const shabuInfoWindow = new google.maps.InfoWindow({
    content: shabuContentString
  });

  const boilingInfoWindow = new google.maps.InfoWindow({
    content: boilingContentString
  });

  // Coordinates to House of Shabu Shabu II
  const shabuLatLng = new google.maps.LatLng(33.840585, -117.942175);
  const boilingLatLng = new google.maps.LatLng(33.761388, -117.953333);

  const shabuMarker = new google.maps.Marker({
    position: shabuLatLng,
    map: map,
    title:"House of Shabu Shabu II!"
  });

  const boilingMarker = new google.maps.Marker({
    position: boilingLatLng,
    map: map,
    title:"Boiling Point!"
  });

  shabuMarker.addListener('click', function() {
    shabuInfoWindow.open(map, shabuMarker);
  });

  boilingMarker.addListener('click', function() {
    boilingInfoWindow.open(map, boilingMarker);
  })
} 
