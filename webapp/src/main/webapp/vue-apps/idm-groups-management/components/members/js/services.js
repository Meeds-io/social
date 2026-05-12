import * as groupMembersService from '../js/GroupMembersService';

window.Object.defineProperty(Vue.prototype, '$groupMembersService', {
  value: groupMembersService,
});
