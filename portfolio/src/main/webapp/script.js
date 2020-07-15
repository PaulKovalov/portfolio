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

const totalImages = 8;

// accepts id of the image to be displayed next
// hides currently displayed image and displays the one with the passed id
function updateDisplayedImage(imageId) {
  const visibleImg = document.getElementsByClassName('visible')[0]; // get currently displayed image
  const nextVisibleImg = document.getElementById(imageId);
  if (nextVisibleImg) {
    // if next image is present on the page, remove class 'visible' from the current image and add it to the next
    visibleImg.classList.remove('visible');
    nextVisibleImg.classList.add('visible');
    // update buttons' states - enabled/disabled
    updateButtonsState(Number(nextVisibleImg.id));
  } else {
    alert('Error! Can\'t find the requested image');
  }
}

// returns the id of the currently displayed image
function currentImageId() {
  return document.getElementsByClassName('visible')[0].id; // get currently displayed image
}

// disables or enables navigation buttons based on the currently displayed image
function updateButtonsState(visibleImgId) {
  const prevButton = document.getElementById('btn-prev');
  const nextButton = document.getElementById('btn-next');
  if (visibleImgId === 1) {
    // disable "Prev" button if there are no images left before current
    prevButton.disabled = true;
  } else if (prevButton.disabled) {
    // if there are a few images before and "prev" is disabled, enable it
    prevButton.disabled = false;
  }
  if (visibleImgId === totalImages) {
    // disable "Next" button if the last image is currently displayed
    nextButton.disabled = true;
  } else if (nextButton.disabled) {
    // if there are a few images after and "next" is disabled, enable it
    nextButton.disabled = false;
  }
}

function toggleTheme() {
  var toggler = document.getElementById('theme-toggler');
  var body = document.getElementsByTagName('body')[0];
  if (toggler.checked) {
    body.classList.remove('light-theme');
    body.classList.add('dark-theme');
  } else {
    body.classList.remove('dark-theme');
    body.classList.add('light-theme');
  }
}

function scrollUp() { window.scrollTo({top : 0, behavior : "smooth"}); }
