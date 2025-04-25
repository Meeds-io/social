import ProfileAboutMe from './components/ProfileAboutMe.vue';
import ProfileAboutMeDrawer from './components/ProfileAboutMeDrawer.vue';

const components = {
  'profile-about-me': ProfileAboutMe,
  'profile-about-me-drawer': ProfileAboutMeDrawer,
};

for (const key in components) {
  Vue.component(key, components[key]);
}
