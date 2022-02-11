import Login from './components/Login.vue';
import OAuthLogin from './components/OAuthLogin.vue';

const components = {
  'portal-login': Login,
  'portal-oauth-login': OAuthLogin,
};

for (const key in components) {
  Vue.component(key, components[key]);
}
