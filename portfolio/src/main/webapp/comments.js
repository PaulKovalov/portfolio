
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
  const commentAuthorUsername = document.createElement('h5');
  commentAuthorUsername.innerText = comment.username;
  const commentText = document.createElement('p');
  commentText.innerText = comment.text;
  commentDOMElement.style = 'margin-left: ' + Math.sqrt(Number(depth * 10)) + 'px';
  commentDOMElement.appendChild(commentAuthorUsername);
  commentDOMElement.appendChild(commentText);
  commentDOMElement.classList.add('comment')
  // next I need a form for posting a reply to this comment
  const replyForm = document.createElement('form');
  replyForm.action = '/comments';
  replyForm.method = 'POST';
  // attach created comment to the comments div
  document.getElementById('comments').appendChild(commentDOMElement);
}
