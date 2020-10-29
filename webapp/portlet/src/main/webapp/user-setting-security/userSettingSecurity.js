import { spacesConstants } from '../js/spacesConstants.js';

export function getCurrentUser() {
  return fetch(`${spacesConstants.SOCIAL_USER_API}${eXo.env.portal.userName}`, {
    headers: {
      'Content-Type': 'application/json'
    },
    credentials: 'include',
    method: 'GET'
  }).then(resp => {
    if(resp && resp.ok) {
      return resp.json();
    } else {
      throw new Error ('Error to check user information');
    }
  });
}
