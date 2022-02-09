import Login from './components/Login.vue';

const components = {
  'portal-login': Login,
};

for (const key in components) {
  Vue.component(key, components[key]);
}
