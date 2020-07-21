
async function fetchComments() {
  const response = await fetch('/comments');
  const commentsJson = await response.json();
  // create a hash table of type "comment id" -> "comment" to build a comment tree
  const commentsArrayMap = {};
  for (const c in commentsJson) {
    commentsArray[c.key] = c;
  }
  console.log(commentsArray);
}

function buildCommentsTree(commentsMap) {
  
}
