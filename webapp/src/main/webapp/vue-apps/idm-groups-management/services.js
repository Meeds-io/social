import * as groupService from './js/GroupService';

window.Object.defineProperty(Vue.prototype, '$groupService', {
  value: groupService,
});
