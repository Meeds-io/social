export function getOnlineUsers(spaceId){
  if (spaceId) {
    return fetch(`${Vue.prototype.$spacesConstants.SOCIAL_USER_API}?status=online&spaceId=${spaceId}`, {credentials: 'include'}).then(resp => resp.json());
  }
  return fetch(`${Vue.prototype.$spacesConstants.SOCIAL_USER_API}?status=online`, {credentials: 'include'}).then(resp => resp.json());
}

