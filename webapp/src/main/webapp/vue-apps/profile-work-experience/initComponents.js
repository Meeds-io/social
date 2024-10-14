import ProfileWorkExperiences from './components/ProfileWorkExperiences.vue';
import ProfileWorkExperienceItem from './components/ProfileWorkExperienceItem.vue';
import ProfileWorkExperienceDrawer from './components/ProfileWorkExperienceDrawer.vue';
import ProfileWorkExperienceEditItem from './components/ProfileWorkExperienceEditItem.vue';

const components = {
  'profile-work-experiences': ProfileWorkExperiences,
  'profile-work-experience-item': ProfileWorkExperienceItem,
  'profile-work-experience-drawer': ProfileWorkExperienceDrawer,
  'profile-work-experience-edit-item': ProfileWorkExperienceEditItem,
};

for (const key in components) {
  Vue.component(key, components[key]);
}
