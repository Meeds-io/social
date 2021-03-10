import MfaAccess from './components/MfaAccess.vue';

const components = {
  'mfa-access': MfaAccess,
};

for(const key in components) {
  Vue.component(key, components[key]);
}
