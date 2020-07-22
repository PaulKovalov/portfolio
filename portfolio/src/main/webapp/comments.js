
async function fetchComments() {
  const response = await fetch('/comments');
  const commentsJson = await response.json();
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
  const commentAuthorUsername = document.createElement('h5');
  commentAuthorUsername.innerText = comment.username;
  const commentText = document.createElement('p');
  commentText.innerText = comment.text;
  const showReplyFormButton = document.createElement('button');
  showReplyFormButton.innerText = 'Reply';
  showReplyFormButton.id = comment.key + '_button'; // this button must have unique id as I will change it's text
                                                    // depending on form state
  showReplyFormButton.onclick =
      function() {
    toggleReplyField(comment.key);
  }
  // next I need a form for posting a reply to this comment
  const replyForm = document.createElement('form');
  replyForm.action = '/comments';
  replyForm.method = 'POST';
  const formReplyUsername = document.createElement('input');
  formReplyUsername.type = 'text';
  formReplyUsername.name = 'username';
  formReplyUsername.placeholder = 'Your username';
  const formReplyText = document.createElement('input');
  formReplyText.type = 'text';
  formReplyText.name = 'text';
  formReplyText.placeholder = 'Write reply here';
  // this element is invisible but it holds the replyTo id
  const formReplyToId = document.createElement('input');
  formReplyToId.type = 'text';
  formReplyToId.name = 'replyTo';
  formReplyToId.value = comment.key;
  formReplyToId.hidden = true;
  const formReplySubmit = document.createElement('input');
  formReplySubmit.type = 'submit';
  // now add all this form elements to the form
  replyForm.appendChild(formReplyUsername);
  replyForm.appendChild(formReplyText);
  replyForm.appendChild(formReplyToId);
  replyForm.appendChild(formReplySubmit);
  replyForm.classList.add('hidden');
  replyForm.id = comment.key + '_form';
  // append everything to the comment div
  commentDOMElement.appendChild(commentAuthorUsername);
  commentDOMElement.appendChild(commentText);
  commentDOMElement.appendChild(replyForm);
  commentDOMElement.appendChild(showReplyFormButton);
  // attach created comment to the comments div
  document.getElementById('comments').appendChild(commentDOMElement);
}

// toggles visibility of the reply form
function toggleReplyField(commentId) {
  const showReplyFormButtonId = commentId + '_button'; // generate button's id
  const replyFormId = commentId + '_form'; // generate reply form id
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
