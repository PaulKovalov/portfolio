/* this file is a set of methods for fetching/creating comments */

async function fetchComments() {
  const commentsResponse = await fetch('/comments');
  const commentsJson = await commentsResponse.json();
  const authResponse = await fetch('/auth');
  const authState = await authResponse.json();
  // create a hash table of type "comment id" -> "comment" to build a comment tree
  const commentsMap = {};
  for (const i in commentsJson) {
    const comment = commentsJson[i];
    commentsMap[comment.key] = comment;
  }
  // build a tree, starting DFS from the vertex (comments) that have no incoming vertexes (are not replies)
  for (const i in commentsMap) {
    const comment = commentsMap[i];
    if (!comment.replyTo) {
      buildCommentsTree(commentsMap, comment, 0, authState);
    }
  }
  if (!commentsJson.length) {
    // if there are no comments, add tag 'no comments'
    createNoCommentsTag();
  }
  // if client is authenticated, add new comment form
  if (authState.authenticated) {
    createNewCommentForm();
    if (commentsJson.length) {
      // if there are some comments, add button to delete all of them
      // only authenticated user can delete comments (maybe will change later to administrator only feature)
      addDeleteButton();
    }
  }
  addActionButtonsBasedOnAuthState(authState);
}

// traverses the comments tree and calls 'addCommentToDom' with the correct offset
function buildCommentsTree(commentsMap, comment, depth, authState) {
  // add current comment to dom
  addCommentToDOM(comment, depth, authState)
  // iterate over all replies in the map, add them as the children of the current comment
  for (const i in comment.replies) {
    const replyId = comment.replies[i];
    const reply = commentsMap[replyId]
    buildCommentsTree(commentsMap, reply, Number(depth + 1), authState);
  }
}

// builds a DOM element from the given comment and puts the created DOM element
// in the comments diff with the offset = sqrt(depth * 100)
function addCommentToDOM(comment, depth, authState) {
  // creates a div with two paragraph, one for username and one for comment text
  const commentDiv = document.createElement('div');
  commentDiv.classList.add('comment')
  commentDiv.style = 'margin-left: ' + Math.sqrt(Number(depth * 100)) + 'px';
  commentDiv.id = comment.key;
  const commentText = document.createElement('p');
  commentText.innerText = comment.text;
  const showReplyFormButton = document.createElement('button');
  showReplyFormButton.innerText = 'Reply';
  showReplyFormButton.id = comment.key + '_button'; // this button must have unique id as I will change it's text
                                                    // depending on form state
  showReplyFormButton.onclick = function() {
    toggleReplyField(comment.key, authState);
  };
  const replyForm = getCommentReplyForm(comment);
  const commentHeader = getCommentHeader(comment);
  // append everything to the comment div
  commentDiv.appendChild(commentHeader);
  commentDiv.appendChild(commentText);
  commentDiv.appendChild(replyForm);
  commentDiv.appendChild(showReplyFormButton);
  // attach created comment to the comments div
  document.getElementById('comments').appendChild(commentDiv);
}

// creates a comment header which includes the comment's author username
// and date comment's creation date
function getCommentHeader(comment) {
  const commentAuthorUsername = document.createElement('h5');
  commentAuthorUsername.innerText = comment.username;
  const commentDate = document.createElement('p');
  commentDate.innerText = humanReadableDateFromTimestamp(comment.timestamp);
  // header for both username and date
  const commentHeader = document.createElement('div');
  commentHeader.classList.add('comment-header');
  commentHeader.appendChild(commentAuthorUsername);
  commentHeader.appendChild(commentDate);
  return commentHeader;
}

// creates a reply form as a DOM element for the given comment
function getCommentReplyForm(comment) {
  // next I need a form for posting a reply to this comment
  const replyForm = document.createElement('form');
  replyForm.action = '/comments';
  replyForm.method = 'POST';
  // create form elements
  replyForm.appendChild(createFormInput('text', 'text', 'Write reply here'));
  // this element is invisible but it holds the replyTo id
  replyForm.appendChild(createFormInput('text', 'replyTo', '', comment.key, true));
  replyForm.appendChild(createFormInput('submit', '', ''));
  replyForm.classList.add('hidden');
  replyForm.id = comment.key + '_form';
  return replyForm;
}

// creates input element with the given params
function createFormInput(type, name, placeholder, value = null, hidden = false) {
  const input = document.createElement('input');
  input.type = type;
  // if type of the input is 'text', then it must have value before submitted
  if (input.type === 'text') {
    input.required = true;
    input.addEventListener('input', function(event) {
      // if value is spaces or empty, set an error
      if (input.value.trim().length === 0) {
        input.setCustomValidity('Field has only spaces, please add some text instead');
      } else {
        // otherwise field is considered valid
        input.setCustomValidity('');
      }
    });
  }
  input.name = name;
  input.placeholder = placeholder;
  if (value !== null) {
    input.value = value;
  }
  input.hidden = hidden;
  return input;
}

// toggles visibility of the reply form
function toggleReplyField(commentId, authState) {
  if (authState.authenticated === false) {
    alert('Log in before writing comments');
    return;
  }
  const showReplyFormButtonId = commentId + '_button'; // generate button's id
  const replyFormId = commentId + '_form';             // generate reply form id
  const button = document.getElementById(showReplyFormButtonId);
  button.innerText = (button.innerText === 'Cancel' ? 'Reply' : 'Cancel'); // change text depending on the mode
  const replyForm = document.getElementById(replyFormId);
  if (replyForm.classList.contains('hidden')) {
    // if the form is hidden, show it
    replyForm.classList.remove('hidden');
    replyForm.classList.add('shown');
  } else {
    // otherwis hide the form
    replyForm.classList.remove('shown');
    replyForm.classList.add('hidden');
  }
}

// adds a delete button which deletes all comments on the page
function addDeleteButton() {
  const button = document.createElement('button');
  button.innerText = 'Delete comments';
  button.onclick = function() {
    const request = new Request('/delete-data', {method : 'POST'});
    fetch(request).then(() => {
      window.location.reload(false);
    });
  };
  button.classList.add('delete-comments-btn');
  document.getElementById('comments').appendChild(button);
}

// either adds log in button, or log out button. if user is
// authenticated, displays user's email in DOM
function addActionButtonsBasedOnAuthState(authState) {
  const authDiv = document.getElementById('auth');
  if (authState.authenticated === true) {
    const logOutLink = document.createElement('a');
    const userEmailElement = document.createElement('p');
    userEmailElement.innerText = 'Hello, ' + authState.email + ' !';
    logOutLink.href = authState.logoutUrl;
    logOutLink.innerText = 'Log out';
    authDiv.appendChild(userEmailElement);
    authDiv.appendChild(logOutLink);
  } else {
    const infoText = document.createElement('p');
    infoText.innerText = 'Log in to leave a comment';
    const logInLink = document.createElement('a');
    logInLink.href = authState.loginUrl;
    logInLink.innerText = 'Log in';
    authDiv.appendChild(infoText);
    authDiv.appendChild(logInLink);
  }
}

// creates form for adding new comments
function createNewCommentForm() {
  const formElement = document.createElement('form');
  formElement.method = 'POST';
  formElement.action = '/comments';
  formElement.id = 'add-comment-form';
  formElement.appendChild(createFormInput('text', 'text', 'Write your comment here'));
  formElement.appendChild(createFormInput('submit', '', ''));
  document.getElementById('form-add-comment-div').appendChild(formElement);
}

// creates tag 'No comments yet' and adds it to DOM
function createNoCommentsTag() {
  const noCommentsTag = document.createElement('p');
  noCommentsTag.innerText = 'No comments yet';
  document.getElementById('comments').appendChild(noCommentsTag);
}
