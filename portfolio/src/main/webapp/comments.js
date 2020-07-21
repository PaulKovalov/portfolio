
async function fetchComments() {
  const response = await fetch('/comments');
  const commentsJson = await response.json();
  // create a hash table of type "comment id" -> "comment" to build a comment tree
  const commentsMap = {};
  for (const c in commentsJson) {
    commentsMap[c.key] = c;
  }
  console.log(commentsMap);
  for (const c in commentsMap) {
    if (!c.replyTo) {
      // means comment is a root of the comment branch, start DFS from here
      buildCommentsTree(commentsMap, comment, 0);
    }
  }
}

function buildCommentsTree(commentsMap, comment, depth) {
  // add current comment to dom
  addCommentToDOM(comment, depth)
  // iterate over all replies in the map, add them as the children of the current comment
  for (const replyId in comment.replies) {
    const reply = commentsMap[replyId]
    buildCommentsTree(commentsMap, reply, Number(depth + 1));
  }
}

function addCommentToDOM(comment, depth) {
  // creates a div with two paragraph, one for username and one for comment text
  const commentDOMElement = document.createElement("div");
  const commentAuthorUsername = document.createElement("p");
  commentAuthorUsername.innerText = comment.username;
  const commentText = document.createElement("p");
  commentText.innerText = comment.text;
  commentDOMElement.style = "margin-left: " + sqrt(Number(depth * 10)) + "px";
  commentDOMElement.appendChild(commentAuthorUsername);
  commentDOMElement.appendChild(commentText);
  // next I need a form for posting a reply to this comment
  const replyForm = document.createElement("form");
  replyForm.action = "/comments"
  replyForm.method = "POST"

}
