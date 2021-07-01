import MultifactorAuthenticationApp from './components/MultifactorAuthenticationApp.vue';

const components = {
  'multifactor-authentication-app': MultifactorAuthenticationApp,
};

for (const key in components) {
  Vue.component(key, components[key]);
}
