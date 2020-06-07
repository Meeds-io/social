import ProfileAboutMe from './components/ProfileAboutMe.vue';

const components = {
  'profile-about-me': ProfileAboutMe,
};

for (const key in components) {
  Vue.component(key, components[key]);
}
