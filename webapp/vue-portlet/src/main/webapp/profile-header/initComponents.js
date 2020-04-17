import ProfileHeader from './components/ProfileHeader.vue';

const components = {
  'profile-header': ProfileHeader,
};

for (const key in components) {
  Vue.component(key, components[key]);
}
