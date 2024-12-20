import SearchApplication from './components/SearchApplication.vue';
import SearchToolbar from './components/SearchToolbar.vue';
import SearchResults from './components/SearchResults.vue';
import SearchResultCard from './components/SearchResultCard.vue';
import SearchTagSelector from './components/SearchTagSelector.vue';
import SearchTagList from './components/SearchTagList.vue';

const components = {
  'search-application': SearchApplication,
  'search-toolbar': SearchToolbar,
  'search-results': SearchResults,
  'search-result-card': SearchResultCard,
  'search-tag-selector': SearchTagSelector,
  'search-tag-list': SearchTagList,
};

for (const key in components) {
  Vue.component(key, components[key]);
}
