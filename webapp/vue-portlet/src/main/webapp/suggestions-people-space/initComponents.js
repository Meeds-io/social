import ExoSuggestionsPeopleAndSpace from './components/ExoSuggestionsPeopleAndSpace.vue';

const components = {
  'exo-suggestions-people-and-space': ExoSuggestionsPeopleAndSpace,
};

for(const key in components) {
  Vue.component(key, components[key]);
}
