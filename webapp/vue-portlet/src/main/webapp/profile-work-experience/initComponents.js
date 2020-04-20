import ProfileWorkExperience from './components/ProfileWorkExperience.vue';

const components = {
  'profile-work-experience': ProfileWorkExperience,
};

for (const key in components) {
  Vue.component(key, components[key]);
}
