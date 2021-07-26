import MultifactorAuthenticationApp from './components/MultifactorAuthenticationApp.vue';
import MultifactorAuthenticationComponent from './components/MultifactorAuthenticationComponent.vue';
import ProtectedResourceComponent from './components/ProtectedResourceComponent.vue';
import ProtectedResouceDrawer from './components/ProtectedResouceDrawer.vue';
import ProtectedGroupsUsersDrawer from './components/ProtectedGroupsUsersDrawer.vue';

const components = {
  'multifactor-authentication-app': MultifactorAuthenticationApp,
  'multifactor-authentication-component': MultifactorAuthenticationComponent,
  'protected-resource-component': ProtectedResourceComponent,
  'protected-resource-drawer': ProtectedResouceDrawer,
  'protected-groups-users-drawer': ProtectedGroupsUsersDrawer,
};

for (const key in components) {
  Vue.component(key, components[key]);
}
