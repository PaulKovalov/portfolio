/* this file is a set of methods for fetching/creating comments */

async function fetchComments() {
  const response = await fetch('/comments');
  const commentsJson = await response.json();
  // create a hash table of type "comment id" -> "comment" to build a comment tree
  const commentsMap = {};
  for (const i in commentsJson) {
    const comment = commentsJson[i];
    commentsMap[comment.key] = comment;
  }
  if (commentsJson.length) {
    // if there are some comments, remove label 'No comments yet'
    document.getElementById('no-comments').remove();
  }
  // build a tree, starting DFS from the vertex (comments) that have no incoming vertexes (are not replies)
  for (const i in commentsMap) {
    const comment = commentsMap[i];
    if (!comment.replyTo) {
      buildCommentsTree(commentsMap, comment, 0);
    }
  }
}

function buildCommentsTree(commentsMap, comment, depth) {
  // add current comment to dom
  addCommentToDOM(comment, depth)
  // iterate over all replies in the map, add them as the children of the current comment
  for (const i in comment.replies) {
    const replyId = comment.replies[i];
    const reply = commentsMap[replyId]
    buildCommentsTree(commentsMap, reply, Number(depth + 1));
  }
}

function addCommentToDOM(comment, depth) {
  // creates a div with two paragraph, one for username and one for comment text
  const commentDOMElement = document.createElement('div');
  commentDOMElement.classList.add('comment')
  commentDOMElement.style = 'margin-left: ' + Math.sqrt(Number(depth * 100)) + 'px';
  commentDOMElement.id = comment.key;
  const commentText = document.createElement('p');
  commentText.innerText = comment.text;
  const showReplyFormButton = document.createElement('button');
  showReplyFormButton.innerText = 'Reply';
  showReplyFormButton.id = comment.key + '_button'; // this button must have unique id as I will change it's text
                                                    // depending on form state
  showReplyFormButton.onclick = function() {
    toggleReplyField(comment.key);
  };
  const replyForm = getCommentReplyForm(comment);
  const commentHeader = getCommentHeader(comment);
  // append everything to the comment div
  commentDOMElement.appendChild(commentHeader);
  commentDOMElement.appendChild(commentText);
  commentDOMElement.appendChild(replyForm);
  commentDOMElement.appendChild(showReplyFormButton);
  // attach created comment to the comments div
  document.getElementById('comments').appendChild(commentDOMElement);
}

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

function getCommentReplyForm(comment) {
  // next I need a form for posting a reply to this comment
  const replyForm = document.createElement('form');
  replyForm.action = '/comments';
  replyForm.method = 'POST';
  // create form elements
  replyForm.appendChild(createFormInput('text', 'username', 'Your username'));
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
  input.name = name;
  input.placeholder = placeholder;
  if (value !== null) {
    input.value = value;
  }
  input.hidden = hidden;
  return input;
}

// toggles visibility of the reply form
function toggleReplyField(commentId) {
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

function addDeleteButton() {
  const button = document.createElement('button');
  button.innerText = 'Delete comments';
  button.onclick = function () {
    const request = new Request('/delete-data', {method: 'POST'});
    fetch(request); // redirect is handled by server
  };
  document.getElementById('comments').appendChild(button);
}
