import * as groupService from './js/GroupService';
import * as groupMembersService from './js/GroupMembersService';


window.Object.defineProperty(Vue.prototype, '$groupService', {
  value: groupService,
});

window.Object.defineProperty(Vue.prototype, '$groupMembersService', {
  value: groupMembersService,
});
