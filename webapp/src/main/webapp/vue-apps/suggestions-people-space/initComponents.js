import ExoSuggestionsPeopleAndSpace from './components/ExoSuggestionsPeopleAndSpace.vue';
import ExoSuggestionsSpaceListItem from './components/ExoSuggestionsSpaceListItem.vue';
import ExoSuggestionsPeopleListItem from './components/ExoSuggestionsPeopleListItem.vue';

const components = {
  'exo-suggestions-people-and-space': ExoSuggestionsPeopleAndSpace,
  'exo-suggestions-space-list-item': ExoSuggestionsSpaceListItem,
  'exo-suggestions-people-list-item': ExoSuggestionsPeopleListItem
};

for (const key in components) {
  Vue.component(key, components[key]);
}
