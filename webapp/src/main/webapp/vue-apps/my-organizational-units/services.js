import * as myOrganizationalUnitsService from './js/MyOrganizationalUnitsService.js';
import * as organizationalUnitMembersService from './js/OrganizationalUnitMembersService.js';

window.Object.defineProperty(Vue.prototype, '$myOrganizationalUnitsService', {
  value: myOrganizationalUnitsService,
});

window.Object.defineProperty(Vue.prototype, '$organizationalUnitMembersService', {
  value: organizationalUnitMembersService,
});
