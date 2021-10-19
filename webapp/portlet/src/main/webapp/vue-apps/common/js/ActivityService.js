export function getActivities(spaceId, limit, expand) {
  return fetch(`${eXo.env.portal.context}/${eXo.env.portal.rest}/v1/social/activities?spaceId=${spaceId || ''}&limit=${limit}&expand=${expand || ''}`, {
    method: 'GET',
    credentials: 'include',
  }).then(resp => {
    if (!resp || !resp.ok) {
      throw new Error('Response code indicates a server error', resp);
    } else {
      return resp.json();
    }
  });
}

export function getActivityById(id, expand) {
  return fetch(`${eXo.env.portal.context}/${eXo.env.portal.rest}/v1/social/activities/${id}?expand=${expand || ''}`, {
    method: 'GET',
    credentials: 'include',
  }).then(resp => {
    if (!resp || !resp.ok) {
      throw new Error('Response code indicates a server error', resp);
    } else {
      return resp.json();
    }
  });
}

export function getActivityComments(id, sortDescending, offset, limit, expand) {
  return fetch(`${eXo.env.portal.context}/${eXo.env.portal.rest}/v1/social/activities/${id}/comments?returnSize=true&sortDescending=${sortDescending || false}&offset=${offset || 0}&limit=${limit || 10}&expand=${expand || ''}`, {
    method: 'GET',
    credentials: 'include',
  }).then(resp => {
    if (!resp || !resp.ok) {
      throw new Error('Response code indicates a server error', resp);
    } else {
      return resp.json();
    }
  }).then(data => {
    data.comments = computeParentCommentsList(data.comments);
    return data;
  });
}

export function createActivity(message, activityType, attachments, spaceId, templateParams) {
  return fetch(`${eXo.env.portal.context}/${eXo.env.portal.rest}/v1/social/activities?spaceId=${spaceId || ''}`, {
    headers: {
      'Content-Type': 'application/json'
    },
    credentials: 'include',
    method: 'POST',
    body: JSON.stringify({
      'title': message,
      'type': activityType,
      'templateParams': templateParams || {},
      'files': attachments
    })
  }).then(resp => {
    if (!resp || !resp.ok) {
      throw new Error('Response code indicates a server error', resp);
    } else {
      return resp.json();
    }
  });
}

export function shareActivity(activityId, message, templateParams, spaces) {
  return fetch(`${eXo.env.portal.context}/${eXo.env.portal.rest}/v1/social/activities/${activityId}/share`, {
    headers: {
      'Content-Type': 'application/json'
    },
    credentials: 'include',
    method: 'POST',
    body: JSON.stringify({
      title: message,
      templateParams: templateParams,
      targetSpaces: spaces
    })
  }).then(resp => {
    if (!resp || !resp.ok) {
      throw new Error('Response code indicates a server error', resp);
    } else {
      return resp.json();
    }
  });
}

export function updateActivity(activityId, message, activityType, attachments, templateParams) {
  return fetch(`${eXo.env.portal.context}/${eXo.env.portal.rest}/v1/social/activities/${activityId}`, {
    headers: {
      'Content-Type': 'application/json'
    },
    credentials: 'include',
    method: 'PUT',
    body: JSON.stringify({
      'title': message,
      'type': activityType,
      'templateParams': templateParams || {},
      'files': attachments
    })
  }).then(resp => {
    if (!resp || !resp.ok) {
      throw new Error('Response code indicates a server error', resp);
    } else {
      return resp.json();
    }
  });
}

export function deleteActivity(id, hide) {
  return fetch(`${eXo.env.portal.context}/${eXo.env.portal.rest}/v1/social/activities/${id}?hide=${hide || false}`, {
    method: 'DELETE',
    credentials: 'include',
  }).then(resp => {
    if (!resp || !resp.ok) {
      throw new Error('Response code indicates a server error', resp);
    } else {
      return resp.json();
    }
  });
}

export function getActivityLikers(id, offset, limit) {
  return fetch(`${eXo.env.portal.context}/${eXo.env.portal.rest}/v1/social/activities/${id}/likes?offset=${offset || 0}&limit=${limit || 50}`, {
    method: 'GET',
    credentials: 'include',
  }).then(resp => {
    if (!resp || !resp.ok) {
      throw new Error('Response code indicates a server error', resp);
    } else {
      return resp.json();
    }
  });
}

export function likeActivity(id) {
  return fetch(`${eXo.env.portal.context}/${eXo.env.portal.rest}/v1/social/activities/${id}/likes`, {
    method: 'POST',
    credentials: 'include',
  }).then(resp => {
    if (!resp || !resp.ok) {
      throw new Error('Response code indicates a server error', resp);
    } else {
      return resp.json();
    }
  });
}

export function unlikeActivity(id) {
  return fetch(`${eXo.env.portal.context}/${eXo.env.portal.rest}/v1/social/activities/${id}/likes`, {
    method: 'DELETE',
    credentials: 'include',
  }).then(resp => {
    if (!resp || !resp.ok) {
      throw new Error('Response code indicates a server error', resp);
    } else {
      return resp.json();
    }
  });
}

export function createComment(id, parentCommentId, message, templateParams, expand) {
  return fetch(`${eXo.env.portal.context}/${eXo.env.portal.rest}/v1/social/activities/${id}/comments?expand=${expand || ''}`, {
    headers: {
      'Content-Type': 'application/json'
    },
    credentials: 'include',
    method: 'POST',
    body: JSON.stringify({
      title: message,
      body: message,
      parentCommentId,
      templateParams: templateParams,
    })
  }).then(resp => {
    if (resp && resp.ok) {
      return resp.json();
    } else {
      throw new Error('Response code indicates a server error', resp);
    }
  });
}

export function updateComment(id, parentCommentId, commentId, message, templateParams, expand) {
  return fetch(`${eXo.env.portal.context}/${eXo.env.portal.rest}/v1/social/activities/${id}/comments?expand=${expand || ''}`, {
    headers: {
      'Content-Type': 'application/json'
    },
    credentials: 'include',
    method: 'PUT',
    body: JSON.stringify({
      id: commentId,
      title: message,
      body: message,
      parentCommentId,
      templateParams: templateParams,
    })
  }).then(resp => {
    if (resp && resp.ok) {
      return resp.json();
    } else {
      throw new Error('Response code indicates a server error', resp);
    }
  });
}

export function computeParentCommentsList(comments) {
  const commentsList = [];
  if (comments && comments.length) {
    const commentsPerId = {};
    comments.forEach(comment => {
      if (comment.parentCommentId) {
        let parentComment = commentsPerId[comment.parentCommentId];
        if (parentComment) {
          parentComment.subCommentsSize++;
          if (!parentComment.subComments) {
            parentComment.subComments = [];
          }
          parentComment.subComments.push(comment);
        } else {
          parentComment = commentsList.find(tmpComment => tmpComment.id === comment.parentCommentId);
          if (!parentComment) {
            return;
          }
          commentsPerId[comment.parentCommentId] = parentComment;
          parentComment.subCommentsSize = 1;
          parentComment.subComments = [comment];
        }
      } else {
        commentsPerId[comment.parentCommentId] = comment;
        if (!comment.subCommentsSize) {
          comment.subCommentsSize = 0;
          comment.subComments = [];
        }
        commentsList.push(comment);
      }
    });
    if (commentsList.length) {
      commentsList.forEach(comment => {
        if (comment.subComments && comment.subComments.length) {
          comment.subComments.sort(sortComments);
        }
      });
      commentsList.sort(sortComments);
    }
  }
  return commentsList;
}

function sortComments(comment1, comment2) {
  return new Date(comment1.createDate).getTime() - new Date(comment2.createDate).getTime();
}